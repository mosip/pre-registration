package io.mosip.testrig.apirig.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.testng.Reporter;

import io.restassured.response.Response;

/**
 * Local override of apitest-commons {@code GlobalMethods}.
 * <p>
 * Commons hard-codes {@code https://} in the endpoint regexes used by
 * {@link #getUpdatedEndPointURL(String)} and {@link #getComponentDetails()}.
 * Local runs use {@code http://localhost:...}, so "End Points used" in the
 * EmailableReport stayed empty. This copy accepts {@code http} and {@code https}.
 * Remove once apitest-commons ships {@code https?://} support.
 */
public class GlobalMethods {
	private static final Logger logger = Logger.getLogger(GlobalMethods.class);
	/** Server-side failures (code to message) collected during the run, for the report summary. */
	public static Map<Object, Object> serverFailuresMapS = Collections.synchronizedMap(new HashMap<>());
	/** Captcha status flags reported by modules; key {@code Captcha_enabled} is read by {@link #getCaptchaStatus()}. */
	public static Map<Object, Object> captchaStatusMap = Collections.synchronizedMap(new HashMap<>());
	/** Every endpoint URL reported by {@link #reportRequest(String, String, String)}, after base-URL rewriting. */
	public static Set<String> serverEndpoints = new HashSet<>();
	private static String module_name = "(mimoto|certify|signup|partnermanager|preregistration|resident|residentmobileapp|masterdata|esignet|idgenerator|policymanager|idauthentication|idrepository|auditmanager|authmanager|keymanager|mock-identity-system|credentialservice|credentialrequest)";
	// https?:// so local http://localhost endpoints appear in the report
	private static final String URL_SCHEME = "https?://";
	private static final Pattern URL_HOST = Pattern.compile(URL_SCHEME + "([^/]+)");
	/** Booking-native APIs (cluster gateway path). HTTP proxy is /applications/appointment/**. */
	private static final String BOOKING_NATIVE_PATH = "/preregistration/v1/appointment";
	private static String regex_1 = URL_SCHEME + "([^/]+)/(v[0-9]+)?/" + module_name + "/([^,]+)";
	private static String regex_2 = URL_SCHEME + "([^/]+)/" + module_name + "/(v[0-9]+)/([^,]+)";
	private static Pattern pattern_1 = Pattern.compile(regex_1);
	private static Pattern pattern_2 = Pattern.compile(regex_2);
	/** Three-character run prefix (for example {@code a1b_}) set by {@link #getRunContext()}. */
	public static String runContext = null;

	/** @return true when the {@code xssProtectionCheck} property is {@code yes} */
	public static boolean isXSSProtectionCheckEnabled() {
		return ConfigManager.getproperty("xssProtectionCheck").equalsIgnoreCase("yes");
	}

	/**
	 * When the XSS check is enabled, requires the response header {@code X-Xss-Protection: 1; mode=block}.
	 *
	 * @param response HTTP response to inspect
	 * @param url      called URL, used for reporting only
	 * @throws SecurityXSSException when the check is on and the header is missing or different;
	 *                              the response headers are logged to the report first
	 */
	public static void checkXSSProtectionHeader(Response response, String url) throws SecurityXSSException {
		String xssHeader = response.getHeader("X-Xss-Protection");
		if (isXSSProtectionCheckEnabled() && (xssHeader == null || !xssHeader.equalsIgnoreCase("1; mode=block"))) {
			reportResponseHeader(response.getHeaders().asList().toString(), url);
			throw new SecurityXSSException("Response Header does not have X-XSS-Protection");
		}
	}

	/**
	 * Replaces the module-name alternation used by the endpoint regexes (for example
	 * {@code (idrepository|credentialservice)}) and recompiles them. Blank values are ignored and the
	 * built-in list is kept.
	 *
	 * @param value regex group of module names, normally from {@code moduleNamePattern}
	 */
	public static void setModuleNameAndReCompilePattern(String value) {
		if (value == null || value.trim().isEmpty()) {
			return;
		}
		module_name = value;
		regex_1 = URL_SCHEME + "([^/]+)/(v[0-9]+)?/" + module_name + "/([^,]+)";
		regex_2 = URL_SCHEME + "([^/]+)/" + module_name + "/(v[0-9]+)/([^,]+)";
		pattern_1 = Pattern.compile(regex_1);
		pattern_2 = Pattern.compile(regex_2);
	}

	/**
	 * Generates a new random three-character run prefix (plus {@code _}), stores it in
	 * {@link #runContext} and returns it. Each call creates a new value.
	 *
	 * @return the new run context
	 */
	public static String getRunContext() {
		runContext = UUID.randomUUID().toString().replaceAll("-", "").toLowerCase().substring(0, 3) + "_";
		logger.info("RUN_CONTEXT set to: " + runContext);
		return runContext;
	}

	/** No-op; kept for parity with the commons class. */
	public static void main(String[] arg) {
	}

	/**
	 * Rewrites the host of {@code url} to the module's configured base URL
	 * ({@link ConfigManager#getComponentBaseURL(String)}) when one is set.
	 * <p>
	 * Two layouts are recognised: {@code http(s)://host/vN/module/...} and
	 * {@code http(s)://host/module/vN/...}. An unmatched URL, or a module with no override, is
	 * returned unchanged.
	 *
	 * @param url absolute endpoint URL
	 * @return the URL with the host replaced, or the original URL
	 */
	public static String getUpdatedEndPointURL(String url) {
		String bookingNativeUrl = rewriteBookingNativeUrl(url);
		if (bookingNativeUrl != null) {
			return bookingNativeUrl;
		}
		Matcher matcher = pattern_1.matcher(url);
		if (matcher.find()) {
			String domain = matcher.group(1);
			String module = matcher.group(3);
			String newBaseURL = ConfigManager.getComponentBaseURL(module);
			if (newBaseURL != null && !newBaseURL.isEmpty()) {
				return url.replace(domain, newBaseURL);
			}
			return url;
		}
		Matcher matcher2 = pattern_2.matcher(url);
		if (matcher2.find()) {
			String domain = matcher2.group(1);
			String module = matcher2.group(2) != null ? matcher2.group(2) : "";
			String newBaseURL = ConfigManager.getComponentBaseURL(module);
			if (newBaseURL != null && !newBaseURL.isEmpty()) {
				return url.replace(domain, newBaseURL);
			}
			return url;
		}
		logger.debug("Needs RegEx revisit...url is:" + url);
		return url;
	}

	/**
	 * Local compose has no cluster gateway. Frozen booking-native paths stay
	 * {@code /preregistration/v1/appointment/**}; only the host is switched when
	 * {@code mosip_components_base_urls} has a {@code booking=} entry. Cluster runs
	 * leave that unset, so ApplnURI is unchanged.
	 */
	private static String rewriteBookingNativeUrl(String url) {
		if (url == null || !url.contains(BOOKING_NATIVE_PATH)) {
			return null;
		}
		String bookingHost = ConfigManager.getComponentBaseURL("booking");
		if (bookingHost == null || bookingHost.isBlank()) {
			return null;
		}
		Matcher hostMatcher = URL_HOST.matcher(url);
		if (!hostMatcher.find()) {
			return null;
		}
		return url.replace(hostMatcher.group(1), bookingHost);
	}

	/**
	 * Rewrites {@code url} with {@link #getUpdatedEndPointURL(String)} and records it in
	 * {@link #serverEndpoints}.
	 *
	 * @param url called endpoint
	 * @return the rewritten URL
	 */
	public static String addToServerEndPointMap(String url) {
		String updatedURL = getUpdatedEndPointURL(url);
		serverEndpoints.add(updatedURL);
		return updatedURL;
	}

	/**
	 * Generalises a URL for de-duplication by dropping numeric path segments (UINs, VIDs, RIDs)
	 * and {@code mosip_*} segments.
	 *
	 * @param url endpoint path
	 * @return the path without volatile segments
	 */
	public static String removeNumerics(String url) {
		String modifiedString = url;
		modifiedString = Pattern.compile("/\\d+/").matcher(modifiedString).replaceAll("/");
		modifiedString = Pattern.compile("/\\d+$").matcher(modifiedString).replaceAll("/");
		modifiedString = Pattern.compile("/mosip_[a-zA-Z0-9_]+/").matcher(modifiedString).replaceAll("/");
		return modifiedString;
	}

	/**
	 * Builds the "End Points used" section of the emailable report from {@link #serverEndpoints}:
	 * one de-duplicated {@code Domain ---- Module ---- End Point} line per endpoint, with numeric
	 * segments removed. Accepts {@code http} and {@code https}.
	 *
	 * @return newline-separated lines; empty when no endpoint matched
	 */
	public static String getComponentDetails() {
		Pattern localPattern1 = Pattern.compile(URL_SCHEME + "([^/]+)/(v[0-9]+)?/" + module_name + "/([^,]+)");
		Pattern localPattern2 = Pattern.compile(URL_SCHEME + "([^/]+)/" + module_name + "/(v[0-9]+)/([^,]+)");
		Set<String> uniqueResults = new HashSet<>();
		for (String url : serverEndpoints) {
			Matcher matcher1 = localPattern1.matcher(url);
			if (matcher1.find()) {
				String domain = matcher1.group(1);
				String version = matcher1.group(2) != null ? matcher1.group(2) : "";
				String module = matcher1.group(3);
				String endpoint = version + "/" + module + "/" + matcher1.group(4);
				uniqueResults.add("Domain: " + domain + " ---- Module: " + module + " ---- End Point: "
						+ removeNumerics(endpoint));
				continue;
			}
			Matcher matcher2 = localPattern2.matcher(url);
			if (!matcher2.find()) {
				continue;
			}
			String domain = matcher2.group(1);
			String module = matcher2.group(2) != null ? matcher2.group(2) : "";
			String version = matcher2.group(3);
			String endpoint = module + "/" + version + "/" + matcher2.group(4);
			uniqueResults.add("Domain: " + domain + " ---- Module: " + module + " ---- End Point: "
					+ removeNumerics(endpoint));
		}
		List<String> uniqueList = new ArrayList<>(uniqueResults);
		StringBuilder stringBuilder = new StringBuilder();
		for (String result : uniqueList) {
			stringBuilder.append("\n").append(result);
		}
		return stringBuilder.toString();
	}

	/**
	 * Describes which test cases produce ({@code generators}) and consume ({@code consumers}) cached
	 * {@code $ID:...$} variables, with impact summaries per generator and per consumer.
	 *
	 * @return report text; sections without content are left out
	 */
	public static String getTestCaseVariableMapping() {
		VariableDependencyMapper mapper = new VariableDependencyMapper(AdminTestUtil.generators, AdminTestUtil.consumers);
		StringBuilder variableMappingBuilder = new StringBuilder();
		if (!mapper.getConsumerToGeneratorsMap().isEmpty()) {
			variableMappingBuilder.append("Consumer to Generators Mapping:\n");
			mapper.getConsumerToGeneratorsMap()
					.forEach((k, v) -> variableMappingBuilder.append(k + " → " + String.valueOf(v)).append("\n"));
		}
		String impactSummary = mapper.getImpactSummary();
		if (impactSummary != null && !impactSummary.trim().isEmpty()) {
			variableMappingBuilder.append("\nImpact Summary:\n");
			variableMappingBuilder.append(impactSummary);
		}
		String impactByGenerator = mapper.getImpactSummaryBasedOnGenerator();
		if (impactByGenerator != null && !impactByGenerator.trim().isEmpty()) {
			variableMappingBuilder.append("\nImpact Summary Based On Generator:\n");
			variableMappingBuilder.append(impactByGenerator);
		}
		String impactByConsumer = mapper.getImpactSummaryBasedOnConsumer();
		if (impactByConsumer != null && !impactByConsumer.trim().isEmpty()) {
			variableMappingBuilder.append("\nImpact Summary Based On Consumer:\n");
			variableMappingBuilder.append(impactByConsumer);
		}
		return variableMappingBuilder.toString();
	}

	/** Records a server failure for the report summary; a repeated code keeps the latest message. */
	public static void reportServerError(Object code, Object errorMessage) {
		serverFailuresMapS.put(code, errorMessage);
	}

	/** @return the recorded server failures, or {@code "No server errors"} */
	public static String getServerErrors() {
		if (serverFailuresMapS.isEmpty()) {
			return "No server errors";
		}
		return serverFailuresMapS.toString();
	}

	/** Records a captcha status flag, for example {@code Captcha_enabled -> true}. */
	public static void reportCaptchaStatus(Object code, Object captchaMessage) {
		captchaStatusMap.put(code, captchaMessage);
	}

	/** @return true when {@code Captcha_enabled} was reported as {@code true} */
	public static boolean getCaptchaStatus() {
		Object value = captchaStatusMap.get("Captcha_enabled");
		return Boolean.parseBoolean(String.valueOf(value));
	}

	/**
	 * Masks secrets and bulky payloads before they are written to the report.
	 * <p>
	 * Outside debug mode, string values whose JSON key contains {@code password}, {@code secret},
	 * {@code token}, {@code key}, {@code private}, {@code client_secret} or {@code authclientsecret}
	 * are replaced, along with {@code individualBiometrics} values. In every mode, {@code value} and
	 * {@code data} strings of 200 or more characters are replaced too.
	 *
	 * @param strInput request or response text; null or blank is returned as is
	 * @return the masked text
	 */
	public static String maskOutSensitiveInfo(String strInput) {
		if (strInput == null || strInput.isBlank()) {
			return strInput;
		}
		String maskedInput = strInput;
		if (!ConfigManager.IsDebugEnabled().booleanValue()) {
			String[] sensitiveKeys = new String[] { "password", "secret", "token", "key", "private", "client_secret",
					"authclientsecret" };
			for (String key : sensitiveKeys) {
				String regex = "(?i)(\"[^\"]*" + key + "[^\"]*\"\\s*:\\s*\")(.*?)(\")";
				maskedInput = maskedInput.replaceAll(regex, "$1***** MASKED *****$3");
			}
			Pattern individualBiometricsPattern = Pattern
					.compile("\"category\"\\s*:\\s*\"individualBiometrics\"\\s*,\\s*\"value\"\\s*:\\s*\"(.*?)\"");
			maskedInput = individualBiometricsPattern.matcher(maskedInput)
					.replaceAll("\"category\": \"individualBiometrics\", \"value\": \"***** MASKED *****\"");
		}
		maskedInput = maskedInput.replaceAll("\"value\"\\s*:\\s*\"([^\"]{200,})\"", "\"value\": \"***** MASKED *****\"");
		maskedInput = maskedInput.replaceAll("\"data\"\\s*:\\s*\"([^\"]{200,})\"", "\"data\": \"***** MASKED *****\"");
		return maskedInput;
	}

	/**
	 * Logs a request and its response to the TestNG report.
	 *
	 * @param formatResponse true to render the response body as formatted JSON
	 */
	public static void ReportRequestAndResponse(String reqHeader, String resHeader, String url, String requestBody,
			String response, boolean formatResponse) {
		reportRequest(reqHeader, requestBody, url);
		reportResponse(resHeader, url, response, formatResponse);
	}

	/** Logs a request and its unformatted response to the TestNG report. */
	public static void ReportRequestAndResponse(String reqHeader, String resHeader, String url, String requestBody,
			String response) {
		reportRequest(reqHeader, requestBody, url);
		reportResponse(resHeader, url, response);
	}

	/** Logs a request with no endpoint URL. */
	public static void reportRequest(String requestHeader, String request) {
		reportRequest(requestHeader, request, "");
	}

	/**
	 * Logs a request (headers plus masked body) to the TestNG report. A non-blank {@code url} is
	 * recorded through {@link #addToServerEndPointMap(String)} so it appears under "End Points used".
	 * A null or {@code {}} body is shown as "No request body".
	 */
	public static void reportRequest(String requestHeader, String request, String url) {
		if (url != null && !url.isBlank()) {
			url = addToServerEndPointMap(url);
		}
		String formattedHeader = ReportUtil.getTextAreaForHeaders(requestHeader);
		if (request != null && !request.equals("{}")) {
			Reporter.log("<b><u>Request: </u></b>(End Point URL: " + url + ") <pre>" + formattedHeader
					+ ReportUtil.getTextAreaJsonMsgHtml(maskOutSensitiveInfo(request)) + "</pre>");
		} else {
			Reporter.log("<b><u>Request: </u></b>(End Point URL: " + url + ") <pre>" + formattedHeader
					+ ReportUtil.getTextAreaJsonMsgHtml("No request body") + "</pre>");
		}
	}

	/** Logs a response; PDF bodies are left out and other bodies are masked and formatted. */
	public static void reportResponse(String responseHeader, String url, Response response) {
		String formattedHeader = ReportUtil.getTextAreaForHeaders(responseHeader);
		String contentType = response.getHeader("Content-Type");
		if (contentType != null && contentType.contains("application/pdf")) {
			Reporter.log("<b><u>Response: </u></b><pre>" + formattedHeader + "</pre>");
		} else {
			Reporter.log("<b><u>Response: </u></b><pre>" + formattedHeader
					+ ReportUtil.getTextAreaJsonMsgHtml(maskOutSensitiveInfo(response.asString())) + "</pre>");
		}
	}

	/** Logs response headers only. */
	public static void reportResponseHeader(String responseHeader, String url) {
		String formattedHeader = ReportUtil.getTextAreaForHeaders(responseHeader);
		Reporter.log("<b><u>Response: </u></b><pre>" + formattedHeader + "</pre>");
	}

	/** Logs an unformatted, masked response body. */
	public static void reportResponse(String responseHeader, String url, String response) {
		reportResponse(responseHeader, url, response, false);
	}

	/**
	 * Logs a masked response body.
	 *
	 * @param formatResponse true to format headers and body as HTML text areas; false to log the
	 *                       raw header string followed by the body
	 */
	public static void reportResponse(String responseHeader, String url, String response, boolean formatResponse) {
		String formattedHeader = ReportUtil.getTextAreaForHeaders(responseHeader);
		String maskedResponse = maskOutSensitiveInfo(response);
		if (formatResponse) {
			Reporter.log("<b><u>Response: </u></b><pre>" + formattedHeader + ReportUtil.getTextAreaJsonMsgHtml(maskedResponse)
					+ "</pre>");
		} else {
			Reporter.log("<b><u>Response: </u></b><pre>" + responseHeader + maskedResponse + "</pre>");
		}
	}

	/**
	 * Lower-case hex SHA-256 of {@code input} in the platform default charset (used for PKCE /
	 * VCI code challenges).
	 *
	 * @return the hex digest, or an empty string if SHA-256 is unavailable
	 */
	public static String sha256(String input) {
		String returnString = "";
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = digest.digest(input.getBytes());
			StringBuilder hexStringBuilder = new StringBuilder(2 * hashBytes.length);
			for (byte hashByte : hashBytes) {
				hexStringBuilder.append(String.format("%02x", hashByte));
			}
			returnString = hexStringBuilder.toString();
		} catch (NoSuchAlgorithmException e) {
			logger.error("Failed while hashing SHA256 for VCI code challenge " + e.getMessage());
		}
		return returnString;
	}
}
