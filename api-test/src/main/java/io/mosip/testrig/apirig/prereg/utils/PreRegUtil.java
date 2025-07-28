package io.mosip.testrig.apirig.prereg.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.testng.SkipException;

import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.utils.GlobalConstants;
import io.mosip.testrig.apirig.utils.SkipTestCaseHandler;

public class PreRegUtil extends AdminTestUtil {

	private static final Logger logger = Logger.getLogger(PreRegUtil.class);
	
	public static List<String> testCasesInRunScope = new ArrayList<>();
	
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

		return testCaseName;
	}
	
}