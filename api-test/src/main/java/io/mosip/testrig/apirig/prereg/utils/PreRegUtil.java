package io.mosip.testrig.apirig.prereg.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Reporter;
import org.testng.SkipException;

import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.apirig.utils.GlobalConstants;
import io.mosip.testrig.apirig.utils.NotificationListener;
import io.mosip.testrig.apirig.utils.SkipTestCaseHandler;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class PreRegUtil extends AdminTestUtil {

	private static final Logger logger = Logger.getLogger(PreRegUtil.class);

	public static List<String> testCasesInRunScope = new ArrayList<>();

	// Second applicant, and the run user's email in upper case.
	public static final String OTHER_USER_KEYWORD = "$PREREGOTHERUSER$";
	public static final String OTHER_USER_TOKEN_KEYWORD = "$PREREGOTHERUSERTOKEN$";
	public static final String UPPERCASE_USER_TOKEN_KEYWORD = "$PREREGUSERUPPERCASETOKEN$";
	public static final String PII_COMPAT_ON_MARKER = "_PiiCompatOn_";
	public static final String PII_COMPAT_OFF_MARKER = "_PiiCompatOff_";

	// Dedicated test identities: this run's phone number (used by send OTP), fault-injection, hash-collision and
	// never-logged-in users.
	public static final String PHONE_USER_KEYWORD = "$PREREGPHONEUSER$";
	public static final String FAULT_USER_KEYWORD = "$PREREGFAULTUSER$";
	public static final String FAULT_USER_TOKEN_KEYWORD = "$PREREGFAULTUSERTOKEN$";
	public static final String COLLISION_USER_KEYWORD = "$PREREGCOLLISIONUSER$";
	public static final String COLLISION_USER_TOKEN_KEYWORD = "$PREREGCOLLISIONUSERTOKEN$";
	public static final String RACE_USER_KEYWORD = "$PREREGRACEUSER$";
	// Value of piiBackwardCompatibility in prereg.properties.
	public static final String PII_COMPAT_FLAG_KEYWORD = "$PIIBACKWARDCOMPATIBILITY$";
	// Run start (UTC, 5 minutes early), so DB checks on the reused run user see only this run's rows.
	public static final String PII_RUN_START_KEYWORD = "$PIIRUNSTART$";
	private static final String PII_RUN_START = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(5)
			.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	// runContext has only 3 random characters, so the dedicated users get a longer per-run id.
	private static final String PII_RUN_ID = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
	// Fault-injection cases use a trigger named per run, so parallel runs on one environment do not interfere;
	// they are reported as Ignored when the DB user cannot create triggers.
	public static final String PII_FAULT_INJECTION_MARKER = "_PiiFaultInjection_";
	private static final String DEFAULT_ACTUATOR_ENDPOINT = "/preregistration/v1/actuator/env";
	public static final String PII_RUN_ID_KEYWORD = "$PIIRUNID$";

	protected static final String preRegOtherUser = "PreregOther_" + PII_RUN_ID + "@mosip.net";
	protected static final String preRegFaultUser = "PreregFault_" + PII_RUN_ID + "@mosip.net";
	protected static final String preRegCollisionUser = "PreregCollision_" + PII_RUN_ID + "@mosip.net";
	protected static final String preRegRaceUser = "PreregRace_" + PII_RUN_ID + "@mosip.net";
	private static String preRegPhoneUser = null;
	private static String loginPhoneRegex = null;
	private static final Map<String, String> userTokens = new ConcurrentHashMap<>();
	// Users whose login already failed in this run, so later cases fail fast instead of waiting for an OTP again.
	private static final Set<String> failedLogins = ConcurrentHashMap.newKeySet();

	public static void setLogLevel() {
		if (PreRegConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	public static String isTestCaseValidForExecution(TestCaseDTO testCaseDTO) {
		String testCaseName = testCaseDTO.getTestCaseName();
		currentTestCaseName = testCaseName;

		int indexof = testCaseName.indexOf("_");
		String modifiedTestCaseName = testCaseName.substring(indexof + 1);

		addTestCaseDetailsToMap(modifiedTestCaseName, testCaseDTO.getUniqueIdentifier());

		if (!testCasesInRunScope.isEmpty()
				&& testCasesInRunScope.contains(testCaseDTO.getUniqueIdentifier()) == false) {
			throw new SkipException(GlobalConstants.NOT_IN_RUN_SCOPE_MESSAGE);
		}
		currentTestCaseName = testCaseName;
		// Handle extra workflow dependencies
		if (testCaseDTO != null && testCaseDTO.getAdditionalDependencies() != null
				&& AdminTestUtil.generateDependency == true) {
			addAdditionalDependencies(testCaseDTO);
		}

		if (SkipTestCaseHandler.isTestCaseInSkippedList(testCaseName)) {
			throw new SkipException(GlobalConstants.KNOWN_ISSUES);
		}

		JSONArray postalCodeArray = new JSONArray(getValueFromAuthActuator("json-property", "postal_code"));

		if (testCaseName.startsWith("Prereg_")
				&& (testCaseName.contains("_Invalid_PostalCode_")
						|| testCaseName.contains("_SpacialCharacter_PostalCode_"))
				&& (globalRequiredFields != null && !globalRequiredFields.toList().contains(postalCodeArray))) {
			throw new SkipException(GlobalConstants.FEATURE_NOT_SUPPORTED_MESSAGE);
		}

		// Legacy-record cases apply to one value of piiBackwardCompatibility only.
		// Reported as Ignored: the environment runs the other compatibility mode.
		if (testCaseName.contains(PII_COMPAT_ON_MARKER) && !isPiiBackwardCompatibilityEnabled()) {
			throw new SkipException("Test case applies only when PII backward compatibility is enabled; "
					+ GlobalConstants.FEATURE_NOT_SUPPORTED
					+ " as piiBackwardCompatibility=false (mosip.prereg.pii.backward.compatibility). Hence skipping the testcase");
		}
		if (testCaseName.contains(PII_COMPAT_OFF_MARKER) && isPiiBackwardCompatibilityEnabled()) {
			throw new SkipException("Test case applies only when PII backward compatibility is disabled; "
					+ GlobalConstants.FEATURE_NOT_SUPPORTED
					+ " as piiBackwardCompatibility=true (mosip.prereg.pii.backward.compatibility). Hence skipping the testcase");
		}
		if (testCaseName.contains(PII_FAULT_INJECTION_MARKER) && !testCaseName.contains("_Install")
				&& !testCaseName.endsWith("_Teardown")) {
			checkFaultInjectionActive(!testCaseName.endsWith("_NothingWritten"));
		}

		return testCaseName;
	}

	public static boolean isPiiBackwardCompatibilityEnabled() {
		String value = PreRegConfigManager.getproperty("piiBackwardCompatibility");
		return value == null || value.isBlank() || Boolean.parseBoolean(value.trim());
	}

	/**
	 * Skips a fault-injection case unless the trigger is installed and, when {@code requireNoRow}, the fault
	 * user has no user_details row yet; otherwise the case would run without the fault in place.
	 */
	private static void checkFaultInjectionActive(boolean requireNoRow) {
		String sql = "SELECT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = '" + faultTriggerName() + "' AND NOT tgisinternal)"
				+ " AS trigger_installed, (SELECT count(*) FROM prereg.user_details WHERE upper(identifier_hash) = upper(encode("
				+ "sha256(convert_to(lower(trim('" + preRegFaultUser + "')),'UTF8')),'hex'))) AS fault_user_rows";
		Map<String, String> row;
		try {
			row = executeDbStatement("prereg", sql);
		} catch (SQLException e) {
			throw new SkipException("DB fault injection " + GlobalConstants.FEATURE_NOT_SUPPORTED_MESSAGE
					+ ". The fault-injection trigger cannot be checked: " + e.getMessage());
		}
		if (!"true".equals(row.get("trigger_installed"))) {
			throw new SkipException("DB fault injection " + GlobalConstants.FEATURE_NOT_SUPPORTED_MESSAGE
					+ ". This run's trigger on prereg.user_details is not installed (the DB user cannot create triggers,"
					+ " or TC_prereg_PiiScenario_06/_07 are not in the run)");
		}
		if (requireNoRow && !"0".equals(row.get("fault_user_rows"))) {
			throw new SkipException("The fault user already has a user_details row, so the fault cannot take effect");
		}
		Reporter.log("<b><u>Precondition</u></b><pre>Fault injection active: trigger " + faultTriggerName()
				+ " installed on prereg.user_details, user_details rows for the fault user = "
				+ row.get("fault_user_rows") + ", so resolving its user id fails</pre>");
	}

	/** Trigger and function names for this run's fault injection. */
	public static String faultTriggerName() {
		return "pii_fault_trg_" + PII_RUN_ID;
	}

	public static String faultFunctionName() {
		return "pii_fault_fn_" + PII_RUN_ID;
	}

	@Override
	public String inputJsonKeyWordHandeler(String jsonString, String testCaseName) {
		if (jsonString != null) {
			if (jsonString.contains(OTHER_USER_TOKEN_KEYWORD)) {
				jsonString = jsonString.replace(OTHER_USER_TOKEN_KEYWORD,
						requireUserToken(preRegOtherUser, OTHER_USER_TOKEN_KEYWORD));
			}
			if (jsonString.contains(UPPERCASE_USER_TOKEN_KEYWORD)) {
				jsonString = jsonString.replace(UPPERCASE_USER_TOKEN_KEYWORD,
						requireUserToken(preRegUser.toUpperCase(Locale.ROOT), UPPERCASE_USER_TOKEN_KEYWORD));
			}
			if (jsonString.contains(FAULT_USER_TOKEN_KEYWORD)) {
				jsonString = jsonString.replace(FAULT_USER_TOKEN_KEYWORD,
						requireUserToken(preRegFaultUser, FAULT_USER_TOKEN_KEYWORD));
			}
			if (jsonString.contains(COLLISION_USER_TOKEN_KEYWORD)) {
				jsonString = jsonString.replace(COLLISION_USER_TOKEN_KEYWORD,
						requireUserToken(preRegCollisionUser, COLLISION_USER_TOKEN_KEYWORD));
			}
			jsonString = jsonString.replace(OTHER_USER_KEYWORD, preRegOtherUser)
					.replace(FAULT_USER_KEYWORD, preRegFaultUser)
					.replace(COLLISION_USER_KEYWORD, preRegCollisionUser)
					.replace(RACE_USER_KEYWORD, preRegRaceUser);
			// Before the framework's own $PHONENUMBER$ (ID schema regex), which login may reject.
			if (jsonString.contains("$PHONENUMBER$")) {
				jsonString = jsonString.replace("$PHONENUMBER$", generateLoginPhoneNumber());
			}
			if (jsonString.contains(PHONE_USER_KEYWORD)) {
				jsonString = jsonString.replace(PHONE_USER_KEYWORD, getPhoneUser());
			}
			jsonString = jsonString.replace(PII_COMPAT_FLAG_KEYWORD, String.valueOf(isPiiBackwardCompatibilityEnabled()))
					.replace(PII_RUN_START_KEYWORD, PII_RUN_START).replace(PII_RUN_ID_KEYWORD, PII_RUN_ID);
		}
		return super.inputJsonKeyWordHandeler(jsonString, testCaseName);
	}

	public static String getOtherUserToken() {
		return getUserToken(preRegOtherUser);
	}

	public static String getUpperCaseUserToken() {
		return getUserToken(preRegUser.toUpperCase(Locale.ROOT));
	}

	/** Login token for the user id, cached per run; "" when the login fails. */
	public static synchronized String getUserToken(String userId) {
		if (failedLogins.contains(userId))
			return "";
		String token = userTokens.get(userId);
		if (!AdminTestUtil.isValidToken(token)) {
			token = loginPreRegUser(userId);
			if (token == null || token.isEmpty()) {
				logger.error("Pre registration login returned no token for a PII test user");
				failedLogins.add(userId);
				return "";
			}
			userTokens.put(userId, token);
		}
		return token;
	}

	/** Like getUserToken, but a failed login fails the test. */
	private static String requireUserToken(String userId, String keyword) {
		String token = getUserToken(userId);
		if (token.isEmpty()) {
			throw new IllegalStateException("Login failed for the test user behind " + keyword
					+ "; check OTP delivery (mock SMTP or usePreConfiguredOtp)");
		}
		return token;
	}

	/** This run's phone number: sent an OTP by TC_prereg_SendOtp_01 and checked by the PII phone cases. */
	public static synchronized String getPhoneUser() {
		if (preRegPhoneUser == null) {
			preRegPhoneUser = generateLoginPhoneNumber();
		}
		return preRegPhoneUser;
	}

	/**
	 * Mobile number that pre registration login accepts: generated from its own phone regex
	 * (mosip.id.validation.identity.phone in GET /login/config), which can be stricter than the ID schema one.
	 */
	public static String generateLoginPhoneNumber() {
		String phone = "";
		try {
			String regex = getLoginPhoneRegex();
			if (regex != null && !regex.isEmpty())
				phone = genStringAsperRegex(regex);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		phone = phone.replaceFirst("^\\++", ""); // a regex can generate leading '+' signs
		if (phone.isEmpty())
			phone = "9" + String.format("%09d", ThreadLocalRandom.current().nextInt(1_000_000_000));
		return phone;
	}

	private static synchronized String getLoginPhoneRegex() {
		if (loginPhoneRegex == null) {
			loginPhoneRegex = phoneSchemaRegex;
			try {
				Response config = RestAssured.given().relaxedHTTPSValidation()
						.get(ApplnURI + getPropertyOrDefault("preregLoginConfig", "/preregistration/v1/login/config"));
				String value = new JSONObject(config.asString()).getJSONObject("response")
						.optString("mosip.id.validation.identity.phone", "");
				if (!value.isBlank())
					loginPhoneRegex = value;
			} catch (Exception e) {
				logger.error("Could not read the login phone regex, using the ID schema one: " + e.getMessage());
			}
		}
		return loginPhoneRegex;
	}

	/** Send-OTP / validate-OTP login for any user id; returns the Authorization cookie. */
	private static String loginPreRegUser(String userId) {
		JSONObject sendOtpRequest = new JSONObject();
		sendOtpRequest.put("id", "mosip.pre-registration.login.sendotp");
		sendOtpRequest.put("version", "1.0");
		sendOtpRequest.put(GlobalConstants.REQUESTTIME, AdminTestUtil.getCurrentUTCTime());
		sendOtpRequest.put(GlobalConstants.REQUEST,
				new JSONObject().put("langCode", BaseTestCase.getLanguageList().get(0)).put("userId", userId));
		Response sendOtp = AdminTestUtil.postWithJson(
				getPropertyOrDefault("preregSendOtp", "/preregistration/v1/login/sendOtp/langcode"),
				sendOtpRequest.toString());
		// No OTP is sent when send-OTP is rejected (e.g. phone login disabled), so do not wait for one.
		JSONArray sendOtpErrors = new JSONObject(sendOtp.asString()).optJSONArray("errors");
		if (sendOtpErrors != null && !sendOtpErrors.isEmpty()) {
			logger.error("Send OTP rejected for a PII test user: " + sendOtpErrors);
			return null;
		}

		String otp;
		if (ConfigManager.getUsePreConfiguredOtp().equalsIgnoreCase("yes")) {
			otp = "111111";
		} else {
			otp = NotificationListener.getOtp(userId);
			// The mail relay may deliver an upper-case login's OTP to the lower-case address.
			if ((otp == null || otp.isBlank()) && !userId.equals(userId.toLowerCase(Locale.ROOT))) {
				otp = NotificationListener.getOtp(userId.toLowerCase(Locale.ROOT));
			}
			// ...or to the address as registered.
			if ((otp == null || otp.isBlank()) && userId.equalsIgnoreCase(preRegUser) && !userId.equals(preRegUser)) {
				otp = NotificationListener.getOtp(preRegUser);
			}
		}

		JSONObject validateOtpRequest = new JSONObject();
		validateOtpRequest.put("id", "mosip.pre-registration.login.useridotp");
		validateOtpRequest.put("version", "1.0");
		validateOtpRequest.put(GlobalConstants.REQUESTTIME, AdminTestUtil.getCurrentUTCTime());
		validateOtpRequest.put(GlobalConstants.REQUEST, new JSONObject().put("otp", otp).put("userId", userId));
		Response otpValidate = AdminTestUtil.postWithJson(
				getPropertyOrDefault("preregValidateOtp", "/preregistration/v1/login/validateOtp"),
				validateOtpRequest.toString());
		return otpValidate.getCookie(GlobalConstants.AUTHORIZATION);
	}

	private static String getPropertyOrDefault(String key, String defaultValue) {
		String value = ConfigManager.getproperty(key);
		return value == null || value.isBlank() ? defaultValue : value.trim();
	}

	/** Runs SQL on mosip_&lt;db&gt;: a query returns its first row, any other statement {"updated_rows": n}. */
	public static Map<String, String> executeDbStatement(String db, String sql) throws SQLException {
		String url = "jdbc:postgresql://" + ConfigManager.getDbServer() + ":" + ConfigManager.getDbPort() + "/mosip_"
				+ db;
		Map<String, String> row = new LinkedHashMap<>();
		try (Connection connection = DriverManager.getConnection(url, ConfigManager.getAuditDbUser(),
				ConfigManager.getAuditDbPass()); Statement statement = connection.createStatement()) {
			if (statement.execute(sql)) {
				try (ResultSet rs = statement.getResultSet()) {
					ResultSetMetaData md = rs.getMetaData();
					if (rs.next()) {
						for (int i = 1; i <= md.getColumnCount(); i++) {
							row.put(md.getColumnLabel(i), String.valueOf(rs.getObject(i)));
						}
					}
				}
			} else {
				row.put("updated_rows", String.valueOf(statement.getUpdateCount()));
			}
		}
		return row;
	}

	/**
	 * Runs SQL on mosip_&lt;db&gt; for a test case. A fault-injection case is skipped when the DB user cannot
	 * create the trigger (Postgres 42501 = insufficient_privilege).
	 */
	public static JSONObject runDbStatement(String db, String sql, String testCaseName) throws AdminTestException {
		logger.info("Executing on mosip_" + db + ": " + sql);
		try {
			return new JSONObject(executeDbStatement(db, sql));
		} catch (SQLException e) {
			if (testCaseName.contains(PII_FAULT_INJECTION_MARKER) && "42501".equals(e.getSQLState())) {
				throw new SkipException("DB fault injection " + GlobalConstants.FEATURE_NOT_SUPPORTED_MESSAGE
						+ ". The DB user cannot create triggers on prereg.user_details: " + e.getMessage());
			}
			throw new AdminTestException("DB statement failed on mosip_" + db + ": " + e.getMessage());
		}
	}

	/** Drops this run's fault-injection trigger and function. */
	public static void dropFaultInjectionTrigger() {
		try {
			executeDbStatement("prereg", "DROP TRIGGER IF EXISTS " + faultTriggerName() + " ON prereg.user_details; "
					+ "DROP FUNCTION IF EXISTS prereg." + faultFunctionName() + "()");
		} catch (SQLException e) {
			logger.info("Fault-injection trigger not dropped: " + e.getMessage());
		}
	}

	/** application-service actuator env URL (preregActuatorEndpoint in prereg.properties). */
	public static String getActuatorEnvUrl() {
		String endpoint = PreRegConfigManager.getproperty("preregActuatorEndpoint");
		return ApplnURI + (endpoint == null || endpoint.isBlank() ? DEFAULT_ACTUATOR_ENDPOINT : endpoint.trim());
	}

	/**
	 * Effective value of a property from the actuator env; the first source defining the key wins. Skips the case
	 * when the endpoint is not readable or values are masked.
	 */
	public static String readActuatorProperty(String url, String key) {
		Response response = RestAssured.given().relaxedHTTPSValidation().get(url).then().extract().response();
		logger.info("GET " + url + " -> " + response.getStatusCode());
		JSONArray sources;
		try {
			sources = new JSONObject(response.asString()).getJSONArray("propertySources");
		} catch (Exception e) {
			throw new SkipException("application-service actuator env is not readable at " + url);
		}
		for (int i = 0; i < sources.length(); i++) {
			JSONObject properties = sources.getJSONObject(i).optJSONObject(GlobalConstants.PROPERTIES);
			if (properties != null && properties.has(key)) {
				String value = properties.getJSONObject(key).get(GlobalConstants.VALUE).toString().trim();
				// Masked unless management.endpoint.env.show-values is enabled.
				if (value.matches("\\*+"))
					throw new SkipException("actuator env values are masked (management.endpoint.env.show-values)");
				return value;
			}
		}
		return "";
	}

	/** True when a non-OTP mail reaches the recipient; OTP mails are skipped. */
	public static boolean receivedNonOtpMail(String recipient, String contains) {
		while (true) {
			String message = NotificationListener.getNotification(recipient, contains);
			if (message == null || message.isEmpty())
				return false;
			if (NotificationListener.parseOtp(message).isEmpty())
				return true;
		}
	}

}
