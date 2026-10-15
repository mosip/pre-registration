package io.mosip.testrig.apirig.prereg.testscripts;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.mosip.testrig.apirig.dto.OutputValidationDto;
import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.prereg.utils.PreRegConfigManager;
import io.mosip.testrig.apirig.prereg.utils.PreRegUtil;
import io.mosip.testrig.apirig.testrunner.HealthChecker;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.apirig.utils.GlobalConstants;
import io.mosip.testrig.apirig.utils.GlobalMethods;
import io.mosip.testrig.apirig.utils.OutputValidationUtil;
import io.mosip.testrig.apirig.utils.ReportUtil;

/**
 * Validates state that no API returns, and compares the result JSON with the YAML output:
 * <ul>
 * <li>{@code {"db": "prereg" | "audit", "query": "<SQL>"}} - first result row, or {@code {"updated_rows": n}}</li>
 * <li>{@code {"check": "actuatorProperty", "key": "<property>"}} - effective {@code {"value"}} in application-service</li>
 * <li>{@code {"check": "mailbox", "recipient": "<address>"}} - {@code {"received"}} from the mock SMTP listener</li>
 * </ul>
 */
public class PreRegDbValidator extends PreRegUtil implements ITest {
	private static final Logger logger = Logger.getLogger(PreRegDbValidator.class);
	protected String testCaseName = "";

	/**
	 * get current testcaseName
	 */
	@Override
	public String getTestName() {
		return testCaseName;
	}

	@BeforeClass
	public static void setLogLevel() {
		if (PreRegConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	/**
	 * Data provider class provides test case list
	 *
	 * @return object of data provider
	 */
	@DataProvider(name = "testcaselist")
	public Object[] getTestCaseList(ITestContext context) {
		String ymlFile = context.getCurrentXmlTest().getLocalParameters().get("ymlFile");
		logger.info("Started executing yml: " + ymlFile);
		return getYmlTestData(ymlFile);
	}

	@Test(dataProvider = "testcaselist")
	public void test(TestCaseDTO testCaseDTO) throws AdminTestException {
		testCaseName = testCaseDTO.getTestCaseName();
		testCaseName = PreRegUtil.isTestCaseValidForExecution(testCaseDTO);
		if (HealthChecker.signalTerminateExecution) {
			throw new SkipException(
					GlobalConstants.TARGET_ENV_HEALTH_CHECK_FAILED + HealthChecker.healthCheckFailureMapS);
		}

		JSONObject input = new JSONObject(inputJsonKeyWordHandeler(testCaseDTO.getInput(), testCaseName));
		JSONObject result = input.has("check") ? runCheck(input) : runQuery(input);
		String actualJson = result.toString();
		GlobalMethods.reportResponse(null, "", actualJson, true);

		String expectedJson = inputJsonKeyWordHandeler(testCaseDTO.getOutput(), testCaseName);
		Map<String, List<OutputValidationDto>> ouputValid = OutputValidationUtil.doJsonOutputValidation(actualJson,
				expectedJson, testCaseDTO, 200);
		Reporter.log(ReportUtil.getOutputValidationReport(ouputValid));

		if (!OutputValidationUtil.publishOutputResult(ouputValid))
			throw new AdminTestException("Failed at output validation");
	}

	private JSONObject runQuery(JSONObject input) throws AdminTestException {
		String db = input.optString("db", "prereg");
		String query = input.getString("query");
		GlobalMethods.reportRequest(null, new JSONObject().put("query", query).toString(), "db:mosip_" + db);
		return runDbStatement(db, query, testCaseName);
	}

	private JSONObject runCheck(JSONObject input) throws AdminTestException {
		String check = input.getString("check");
		JSONObject result = new JSONObject();
		if ("actuatorProperty".equals(check)) {
			String url = getActuatorEnvUrl();
			GlobalMethods.reportRequest(null, input.toString(), url);
			result.put("value", readActuatorProperty(url, input.getString("key")));
		} else if ("mailbox".equals(check)) {
			if (ConfigManager.getUsePreConfiguredOtp().equalsIgnoreCase("yes")) {
				throw new SkipException("Mailbox check needs the mock SMTP listener (usePreConfiguredOtp=no)");
			}
			GlobalMethods.reportRequest(null, input.toString(), "mock SMTP mailbox");
			result.put("received", String.valueOf(receivedNonOtpMail(input.getString("recipient"),
					input.optString("contains", ""))));
		} else {
			throw new AdminTestException("Unknown check: " + check);
		}
		return result;
	}

	/** Drops this run's fault-injection trigger after the suite, in case the teardown case did not run. */
	@AfterSuite(alwaysRun = true)
	public void dropFaultInjectionTriggerAfterSuite() {
		dropFaultInjectionTrigger();
	}

	/**
	 * The method ser current test name to result
	 *
	 * @param result
	 */
	@AfterMethod(alwaysRun = true)
	public void setResultTestName(ITestResult result) {
		result.setAttribute("TestCaseName", testCaseName);
	}
}
