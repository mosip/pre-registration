package io.mosip.testrig.apirig.prereg.testscripts;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.mosip.testrig.apirig.dto.OutputValidationDto;
import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.prereg.utils.PreRegConfigManager;
import io.mosip.testrig.apirig.prereg.utils.PreRegUtil;
import io.mosip.testrig.apirig.testrunner.HealthChecker;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.utils.GlobalConstants;
import io.mosip.testrig.apirig.utils.GlobalMethods;
import io.mosip.testrig.apirig.utils.KernelAuthentication;
import io.mosip.testrig.apirig.utils.NotificationListener;
import io.mosip.testrig.apirig.utils.OutputValidationUtil;
import io.mosip.testrig.apirig.utils.ReportUtil;
import io.restassured.response.Response;

/**
 * Sends a request as any logged-in user, for every method including multipart (token from input "cookie").
 *
 * <p>Input keys: {@code cookie}, {@code _template: "prereg-create" | "prereg-update"}, {@code _idKeyName},
 * {@code _multipart: "document" | "notification"} (with {@code _filePath}, {@code _fileKeyName}),
 * {@code _parallel: n}.
 * Output keys: {@code _mustContain}, {@code _mustNotContain}, {@code _emptyArrays} (field paths that must be
 * an empty array) - only for what the standard validation cannot express;
 * other keys use the standard output validation. {@code {name}} endpoint placeholders are filled from the input.
 */
public class PreRegPiiRequest extends PreRegUtil implements ITest {
	private static final Logger logger = Logger.getLogger(PreRegPiiRequest.class);
	private static final Pattern PATH_PARAM = Pattern.compile("\\{([^}]+)\\}");
	protected String testCaseName = "";
	private String idKeyName = null;

	@BeforeClass
	public static void setLogLevel() {
		if (PreRegConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public String getTestName() {
		return testCaseName;
	}

	@DataProvider(name = "testcaselist")
	public Object[] getTestCaseList(ITestContext context) {
		String ymlFile = context.getCurrentXmlTest().getLocalParameters().get("ymlFile");
		idKeyName = context.getCurrentXmlTest().getLocalParameters().get("idKeyName");
		logger.info("Started executing yml: " + ymlFile);
		return getYmlTestData(ymlFile);
	}

	@Test(dataProvider = "testcaselist")
	public void test(TestCaseDTO testCaseDTO) throws Exception {
		testCaseName = testCaseDTO.getTestCaseName();
		testCaseName = PreRegUtil.isTestCaseValidForExecution(testCaseDTO);
		if (HealthChecker.signalTerminateExecution) {
			throw new SkipException(
					GlobalConstants.TARGET_ENV_HEALTH_CHECK_FAILED + HealthChecker.healthCheckFailureMapS);
		}

		JSONObject input = new JSONObject(inputJsonKeyWordHandeler(testCaseDTO.getInput(), testCaseName));
		boolean ownCookie = input.has(GlobalConstants.COOKIE);
		String token = ownCookie ? input.remove(GlobalConstants.COOKIE).toString()
				: new KernelAuthentication().getTokenByRole(testCaseDTO.getRole());
		Reporter.log("<b><u>Token user</u></b><pre>" + getTokenUserId(token)
				+ (ownCookie ? " (cookie from input)" : " (role " + testCaseDTO.getRole() + ")") + "</pre>");
		String template = input.has("_template") ? input.remove("_template").toString() : null;
		String caseIdKeyName = input.has("_idKeyName") ? input.remove("_idKeyName").toString() : idKeyName;
		String multipart = input.has("_multipart") ? input.remove("_multipart").toString() : null;
		String filePath = input.has("_filePath") ? input.remove("_filePath").toString() : null;
		String fileKeyName = input.has("_fileKeyName") ? input.remove("_fileKeyName").toString() : "file";
		int parallel = input.has("_parallel") ? Integer.parseInt(input.remove("_parallel").toString()) : 1;

		Map<String, String> pathParams = new HashMap<>();
		Matcher matcher = PATH_PARAM.matcher(testCaseDTO.getEndPoint());
		while (matcher.find()) {
			pathParams.put(matcher.group(1), input.optString(matcher.group(1)));
		}
		String url = ApplnURI + testCaseDTO.getEndPoint();
		for (Map.Entry<String, String> param : pathParams.entrySet()) {
			url = url.replace("{" + param.getKey() + "}", param.getValue());
		}
		String body = buildBody(input, template, testCaseDTO.getInputTemplate(), pathParams);

		String method = testCaseDTO.getRestMethod().toUpperCase(Locale.ROOT);
		File file = filePath == null ? null : new File(getResourcePath() + filePath);
		GlobalMethods.reportRequest(null, body, url);

		if ("notification".equals(multipart)) {
			// The mailbox check then ignores older mail.
			NotificationListener.markRequestStart();
		}
		List<Response> responses = new ArrayList<>();
		if (parallel > 1) {
			responses.addAll(
					sendRequestsInParallel(parallel, method, url, body, token, multipart, file, fileKeyName));
		} else {
			responses.add(sendRequestWithCookie(method, url, body, token, multipart, file, fileKeyName));
		}
		Response first = responses.get(0);
		GlobalMethods.reportResponse(first.getHeaders().asList().toString(), url, first);

		if (caseIdKeyName != null && testCaseName.toLowerCase(Locale.ROOT).contains("_sid")) {
			try {
				writeAutoGeneratedId(first, caseIdKeyName, testCaseName);
			} catch (Exception e) {
				logger.info("No " + caseIdKeyName + " to store for " + testCaseName + ": " + e.getMessage());
			}
		}

		validate(testCaseDTO, responses);
	}

	private String buildBody(JSONObject input, String template, String inputTemplate, Map<String, String> pathParams) {
		String body;
		if ("prereg-create".equals(template) || "prereg-update".equals(template)) {
			String generated = AdminTestUtil.generateHbsForPrereg("prereg-update".equals(template));
			body = replaceIdentityContactKeywords(getJsonFromTemplate(input.toString(), generated, false),
					testCaseName);
		} else if (inputTemplate != null && !inputTemplate.isBlank()) {
			body = getJsonFromTemplate(input.toString(), inputTemplate);
		} else {
			body = input.toString();
		}
		body = inputJsonKeyWordHandeler(body, testCaseName);
		if (body.trim().startsWith("{")) {
			JSONObject json = new JSONObject(body);
			pathParams.keySet().forEach(json::remove);
			body = json.toString();
		}
		return body;
	}

	private void validate(TestCaseDTO testCaseDTO, List<Response> responses)
			throws AdminTestException {
		JSONObject expected = new JSONObject(inputJsonKeyWordHandeler(testCaseDTO.getOutput(), testCaseName));
		JSONArray mustContain = expected.has("_mustContain") ? (JSONArray) expected.remove("_mustContain")
				: new JSONArray();
		JSONArray mustNotContain = expected.has("_mustNotContain") ? (JSONArray) expected.remove("_mustNotContain")
				: new JSONArray();
		JSONArray emptyArrays = expected.has("_emptyArrays") ? (JSONArray) expected.remove("_emptyArrays")
				: new JSONArray();
		String expectedJson = null;
		if (!expected.isEmpty()) {
			expectedJson = testCaseDTO.getOutputTemplate() == null ? expected.toString()
					: getJsonFromTemplate(expected.toString(), testCaseDTO.getOutputTemplate());
		} else if (testCaseDTO.isCheckErrorsOnlyInResponse()) {
			expectedJson = "{}"; // the framework then only checks that the response has no errors
		}

		// All checks are reported as rows of the framework's "Output validation: EXPECTED vs ACTUAL" table.
		List<OutputValidationDto> rows = new ArrayList<>();
		boolean failed = false;
		int passedResponses = 0;
		for (int i = 0; i < responses.size(); i++) {
			Response response = responses.get(i);
			boolean single = responses.size() == 1;
			String label = single ? "" : "Response " + (i + 1) + ": ";
			String actual = response.asString();
			Map<String, String> fields = flattenJson(actual);
			boolean responseOk = true;
			for (int j = 0; j < mustContain.length(); j++) {
				String text = mustContain.getString(j);
				List<String> paths = pathsContaining(fields, text);
				responseOk &= !paths.isEmpty();
				if (paths.isEmpty())
					rows.add(outputValidationRow(label + text + " (anywhere in response)", text, "NOT AVAILABLE",
							false));
				else if (single)
					rows.add(outputValidationRow(label + paths.get(0), text, fields.get(paths.get(0)), true));
			}
			for (int j = 0; j < mustNotContain.length(); j++) {
				String text = mustNotContain.getString(j);
				List<String> paths = pathsContaining(fields, text);
				responseOk &= paths.isEmpty();
				if (paths.isEmpty() && single)
					rows.add(outputValidationRow(label + text + " (anywhere in response)", "NOT AVAILABLE",
							"NOT AVAILABLE", true));
				for (String path : paths)
					rows.add(outputValidationRow(label + path, "NOT AVAILABLE", fields.get(path), false));
			}
			for (int j = 0; j < emptyArrays.length(); j++) {
				String path = emptyArrays.getString(j);
				String value = fields.getOrDefault(path, "NOT AVAILABLE");
				boolean empty = "[]".equals(value);
				responseOk &= empty;
				rows.add(outputValidationRow(label + path, "[]", value, empty));
			}
			if (expectedJson != null) {
				Map<String, List<OutputValidationDto>> ouputValid = OutputValidationUtil.doJsonOutputValidation(actual,
						expectedJson, testCaseDTO, response.getStatusCode());
				boolean fieldsOk = OutputValidationUtil.publishOutputResult(ouputValid);
				responseOk &= fieldsOk;
				if (i == 0 || !fieldsOk) {
					for (List<OutputValidationDto> fieldRows : ouputValid.values()) {
						for (OutputValidationDto row : fieldRows) {
							if (!single)
								row.setFieldName(label + row.getFieldName());
							rows.add(row);
						}
					}
				}
			}
			failed |= !responseOk;
			if (responseOk)
				passedResponses++;
		}
		if (responses.size() > 1) {
			rows.add(outputValidationRow("Responses passing every check", String.valueOf(responses.size()),
					String.valueOf(passedResponses), passedResponses == responses.size()));
		}

		Map<String, List<OutputValidationDto>> table = new LinkedHashMap<>();
		table.put(GlobalConstants.EXPECTED_VS_ACTUAL, rows);
		Reporter.log(ReportUtil.getOutputValidationReport(table));
		if (failed)
			throw new AdminTestException("Failed at output validation");
	}

	@AfterMethod(alwaysRun = true)
	public void setResultTestName(ITestResult result) {
		result.setAttribute("TestCaseName", testCaseName);
	}
}
