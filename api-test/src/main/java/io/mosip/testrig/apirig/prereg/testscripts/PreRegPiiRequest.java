package io.mosip.testrig.apirig.prereg.testscripts;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

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
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.apirig.testrunner.HealthChecker;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.utils.GlobalConstants;
import io.mosip.testrig.apirig.utils.GlobalMethods;
import io.mosip.testrig.apirig.utils.KernelAuthentication;
import io.mosip.testrig.apirig.utils.NotificationListener;
import io.mosip.testrig.apirig.utils.OutputValidationUtil;
import io.mosip.testrig.apirig.utils.ReportUtil;
import io.mosip.testrig.apirig.utils.RestClient;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Sends a request as any logged-in user, for every method including multipart (token from input "cookie").
 *
 * <p>Input keys: {@code cookie}, {@code _template: "prereg-create" | "prereg-update"}, {@code _idKeyName},
 * {@code _multipart: "document" | "notification"} (with {@code _filePath}, {@code _fileKeyName}),
 * {@code _parallel: n}.
 * Output keys: {@code _mustContain}, {@code _mustNotContain} (only for what the standard validation cannot express);
 * other keys use the standard output validation. {@code {name}} endpoint placeholders are filled from the input.
 */
public class PreRegPiiRequest extends PreRegUtil implements ITest {
	private static final Logger logger = Logger.getLogger(PreRegPiiRequest.class);
	private static final Pattern PATH_PARAM = Pattern.compile("\\{([^}]+)\\}");
	private static final long PARALLEL_REQUEST_TIMEOUT_SECONDS = 120;
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
		Reporter.log("<b><u>Token user</u></b><pre>" + tokenUser(token)
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
			responses.addAll(sendInParallel(parallel, method, url, body, token, multipart, file, fileKeyName));
		} else {
			responses.add(send(method, url, body, token, multipart, file, fileKeyName));
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
			body = getJsonFromTemplate(input.toString(), generated, false);
			body = replaceKeywordWithValue(body, "$PHONENUMBERFORIDENTITY$", identityPhone());
			body = replaceKeywordWithValue(body, "$EMAILVALUE$", testCaseName + "_" + BaseTestCase.runContext + "@mosip.com");
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

	/** Phone number for the identity JSON, matching the ID schema phone format. */
	private String identityPhone() {
		try {
			if (!phoneSchemaRegex.isEmpty())
				return genStringAsperRegex(phoneSchemaRegex);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return "";
	}

	private Response send(String method, String url, String body, String token, String multipart, File file,
			String fileKeyName) {
		if ("document".equals(multipart)) {
			Map<String, String> formParams = new HashMap<>();
			formParams.put("Document request", body);
			return RestClient.postWithFormPathParamAndFile(url, formParams, new HashMap<>(), file, fileKeyName,
					MediaType.MULTIPART_FORM_DATA, token);
		}
		if ("notification".equals(multipart)) {
			Map<String, String> formParams = new HashMap<>();
			formParams.put("NotificationRequestDTO", body.replace("\r\n", ""));
			// The body carries the first language only; the service looks templates up by this code.
			formParams.put(GlobalConstants.LANG_CODE, BaseTestCase.languageList.get(0));
			formParams.put("attachment", "");
			return RestClient.postWithMultipartFormDataAndFile(url, formParams, MediaType.MULTIPART_FORM_DATA, token);
		}
		RequestSpecification spec = given().relaxedHTTPSValidation().cookie(COOKIENAME, token)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
		if (!"GET".equals(method) && !"DELETE".equals(method)) {
			spec = spec.body(body);
		}
		return spec.request(method, url).then().extract().response();
	}

	private List<Response> sendInParallel(int threads, String method, String url, String body, String token,
			String multipart, File file, String fileKeyName) throws Exception {
		ExecutorService pool = Executors.newFixedThreadPool(threads);
		CountDownLatch startGate = new CountDownLatch(1);
		try {
			List<Future<Response>> futures = new ArrayList<>();
			for (int i = 0; i < threads; i++) {
				futures.add(pool.submit(() -> {
					startGate.await();
					return send(method, url, body, token, multipart, file, fileKeyName);
				}));
			}
			// Release all requests at once.
			startGate.countDown();
			List<Response> responses = new ArrayList<>();
			for (Future<Response> future : futures) {
				try {
					responses.add(future.get(PARALLEL_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS));
				} catch (TimeoutException e) {
					throw new AdminTestException("Parallel request timed out after " + PARALLEL_REQUEST_TIMEOUT_SECONDS
							+ " s: " + url);
				}
			}
			return responses;
		} finally {
			pool.shutdownNow();
		}
	}

	private void validate(TestCaseDTO testCaseDTO, List<Response> responses)
			throws AdminTestException {
		JSONObject expected = new JSONObject(inputJsonKeyWordHandeler(testCaseDTO.getOutput(), testCaseName));
		JSONArray mustContain = expected.has("_mustContain") ? (JSONArray) expected.remove("_mustContain")
				: new JSONArray();
		JSONArray mustNotContain = expected.has("_mustNotContain") ? (JSONArray) expected.remove("_mustNotContain")
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
			Map<String, String> fields = flattenResponse(actual);
			boolean responseOk = true;
			for (int j = 0; j < mustContain.length(); j++) {
				String text = mustContain.getString(j);
				List<String> paths = pathsContaining(fields, text);
				responseOk &= !paths.isEmpty();
				if (paths.isEmpty())
					rows.add(checkRow(label + text + " (anywhere in response)", text, "NOT AVAILABLE", false));
				else if (single)
					rows.add(checkRow(label + paths.get(0), text, fields.get(paths.get(0)), true));
			}
			for (int j = 0; j < mustNotContain.length(); j++) {
				String text = mustNotContain.getString(j);
				List<String> paths = pathsContaining(fields, text);
				responseOk &= paths.isEmpty();
				if (paths.isEmpty() && single)
					rows.add(checkRow(label + text + " (anywhere in response)", "NOT AVAILABLE", "NOT AVAILABLE", true));
				for (String path : paths)
					rows.add(checkRow(label + path, "NOT AVAILABLE", fields.get(path), false));
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
			rows.add(checkRow("Responses passing every check", String.valueOf(responses.size()),
					String.valueOf(passedResponses), passedResponses == responses.size()));
		}

		Map<String, List<OutputValidationDto>> table = new LinkedHashMap<>();
		table.put(GlobalConstants.EXPECTED_VS_ACTUAL, rows);
		Reporter.log(ReportUtil.getOutputValidationReport(table));
		if (failed)
			throw new AdminTestException("Failed at output validation");
	}

	/** Flattens the response JSON to field path -> value, e.g. response.allApplications[3].crBy. */
	private static Map<String, String> flattenResponse(String body) {
		Map<String, String> fields = new LinkedHashMap<>();
		try {
			flatten(new JSONObject(body), "", fields);
		} catch (Exception e) {
			fields.put("response", body); // not JSON: treat the whole body as one value
		}
		return fields;
	}

	private static void flatten(Object node, String path, Map<String, String> fields) {
		if (node instanceof JSONObject) {
			JSONObject object = (JSONObject) node;
			for (String key : object.keySet()) {
				String child = path.isEmpty() ? key : path + "." + key;
				Object value = object.get(key);
				if (value instanceof JSONObject || value instanceof JSONArray)
					fields.put(child, value instanceof JSONArray ? "[...]" : "{...}");
				flatten(value, child, fields);
			}
		} else if (node instanceof JSONArray) {
			JSONArray array = (JSONArray) node;
			for (int i = 0; i < array.length(); i++)
				flatten(array.get(i), path + "[" + i + "]", fields);
		} else if (!path.isEmpty()) {
			fields.put(path, String.valueOf(node));
		}
	}

	/** Paths whose value contains the text, or whose field name is the text (case-insensitive). */
	private static List<String> pathsContaining(Map<String, String> fields, String text) {
		String wanted = text.toLowerCase(Locale.ROOT);
		List<String> paths = new ArrayList<>();
		for (Map.Entry<String, String> field : fields.entrySet()) {
			String key = field.getKey().replaceAll("\\[\\d+\\]$", "");
			key = key.substring(key.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
			if (key.equals(wanted) || field.getValue().toLowerCase(Locale.ROOT).contains(wanted))
				paths.add(field.getKey());
		}
		return paths;
	}

	/** The user id inside the JWT (never the token itself), so the report shows who made the call. */
	private static String tokenUser(String token) {
		try {
			String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
			JSONObject claims = new JSONObject(payload);
			for (String claim : new String[] { "userId", "preferred_username", "sub" }) {
				if (claims.has(claim))
					return claims.getString(claim);
			}
		} catch (Exception e) {
			// not a JWT or no token
		}
		return "unknown";
	}

	private static OutputValidationDto checkRow(String field, String expected, String actual, boolean pass) {
		OutputValidationDto row = new OutputValidationDto();
		row.setFieldName(field);
		row.setExpValue(expected);
		row.setActualValue(actual);
		row.setStatus(pass ? "PASS" : GlobalConstants.FAIL_STRING);
		return row;
	}

	@AfterMethod(alwaysRun = true)
	public void setResultTestName(ITestResult result) {
		result.setAttribute("TestCaseName", testCaseName);
	}
}
