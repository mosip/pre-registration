import { DatePipe, registerLocaleData } from "@angular/common";
import * as appConstants from "./app.constants";
import localeEn from '@angular/common/locales/en';
import { ConfigService } from "./core/services/config.service";

export default class Utils {
  static getCurrentDate() {
    const now = new Date();
    const pipe = new DatePipe("en-US");
    let formattedDate = pipe.transform(now, "yyyy-MM-ddTHH:mm:ss.SSS", "UTC");
    formattedDate = formattedDate + "Z";
    return formattedDate;
  }

  static getURL(currentURL: string, nextRoute: string, numberofPop = 2) {
    if (currentURL) {
      const urlSegments = currentURL.split("/");
      for (let index = 0; index < numberofPop; index++) {
        urlSegments.pop();
      }
      urlSegments.push(nextRoute);
      return urlSegments.join("/");
    }
  }

  static async localeInitializer(localeId: string) {
    const module = await import(`@angular/common/locales/${localeId}.js`);
    registerLocaleData(module.default);
    localStorage.setItem(localeId, JSON.stringify(module.default));
    console.log(`registered localeId: ${localeId}`);
    return true;
  }

  static getBookingDateTime(
    appointment_date: string,
    time_slot_from: string,
    language: string,
    ltrLangs: string[]
  ) {
    let localeId = language.substring(0, 2);
    JSON.parse(localStorage.getItem(appConstants.LANGUAGE_CODE_VALUES)).forEach((element) => {
      if (language === element.code && element.locale) {
        localeId = element.locale;
        if (localeId.indexOf("_") != -1) {
          localeId = localeId.split("_")[0];
        }
      }
    });
    const localeData = localStorage.getItem(localeId);
    let proceed = false;
    if (localeData !== null) {
      registerLocaleData(JSON.parse(localeData));
      proceed = true;
    } else {
      registerLocaleData(localeEn, "en");
      proceed = true;
    }
    if (proceed) {
      const pipe = new DatePipe(localeId);
      const date = appointment_date.split("-");
      let appointmentDateTime =
        date[2] + " " + appConstants.MONTHS[Number(date[1])] + " " + date[0];
      appointmentDateTime = pipe.transform(appointmentDateTime, "MMM");
      date[1] = appointmentDateTime;
      if (ltrLangs.includes(language)) {
        appointmentDateTime = date.reverse().join(" ");
      }  else {
        appointmentDateTime = date.join(" ");
      }
      return appointmentDateTime;
    } else {
      return appointment_date;
    }
  }

  static formatTime(time_slot_from: string) {
    const time = time_slot_from.split(":");
    const appointmentDateTime =
      (Number(time[0]) > 12 ? Number(time[0]) - 12 : Number(time[0])) +
      ":" +
      time[1] +
      (Number(time[0]) >= 12 ? " PM" : " AM");
    return appointmentDateTime;
  }

  static getMandatoryLangs(configService: ConfigService) {
    let mandatoryLanguages = configService
      .getConfigByKey(appConstants.CONFIG_KEYS.mosip_mandatory_languages)
      .split(",");
    mandatoryLanguages = mandatoryLanguages.filter((item) => item != "");
    return mandatoryLanguages;
  }

  static getOptionalLangs(configService: ConfigService) {
    let optionalLanguages = configService
      .getConfigByKey(appConstants.CONFIG_KEYS.mosip_optional_languages)
      .split(",");
    optionalLanguages = optionalLanguages.filter((item) => item != "");
    return optionalLanguages;
  }

  static getMinLangs(configService: ConfigService) {
    let minLanguage = Number(
      configService.getConfigByKey(
        appConstants.CONFIG_KEYS.mosip_min_languages_count
      )
    );
    return minLanguage;
  }

  static getMaxLangs(configService: ConfigService) {
    let maxLanguage = Number(
      configService.getConfigByKey(
        appConstants.CONFIG_KEYS.mosip_max_languages_count
      )
    );
    return maxLanguage;
  }

  static reorderLangsForUserPreferredLang = (
    dataCaptureLanguages: string[],
    userPreferredLangCode: string
  ) => {
    let reorderedArr = [];
    let filteredLangs = dataCaptureLanguages.filter(
      (lang) => lang === userPreferredLangCode
    );
    if (filteredLangs.length > 0) {
      let filteredLangs1 = dataCaptureLanguages.filter(
        (lang) => lang != userPreferredLangCode
      );
      reorderedArr = [userPreferredLangCode, ...filteredLangs1];
    } else {
      reorderedArr = [...dataCaptureLanguages];
    }
    return reorderedArr;
  };

  static getLanguageLabels = (dataCaptureLanguages, languageCodeValues) => {
    let dataAvailableLanguageArr = [];
    if (dataCaptureLanguages) {
      dataAvailableLanguageArr = JSON.parse(dataCaptureLanguages);
    }
    const languageCodeValuesArr = JSON.parse(languageCodeValues);
    let dataCaptureLanguagesLabels = [];
    if (Array.isArray(dataAvailableLanguageArr)) {
      dataAvailableLanguageArr.forEach((langCode: string) => {
        if (Array.isArray(languageCodeValuesArr)) {
          languageCodeValuesArr.forEach((element: any) => {
            if (langCode === element.code) {
              dataCaptureLanguagesLabels.push(element.value);
            }
          });
        }
      });
    } else {
      if (Array.isArray(languageCodeValuesArr)) {
        languageCodeValuesArr.forEach((element: any) => {
          if (dataAvailableLanguageArr === element.code) {
            dataCaptureLanguagesLabels.push(element.value);
          }
        });
      }
    }  
    
    return dataCaptureLanguagesLabels;
  };

  static getLangSelectionPopupAttributes(textDir: string,dataCaptureLabels: any, mandatoryLanguages: string[], minLanguage: number, maxLanguage: number, userPrefLanguage : string) {
    //create string with mandatory Lang Names
    let mandatoryLang = "";
    mandatoryLanguages.forEach((lang) => {
      JSON.parse(localStorage.getItem(appConstants.LANGUAGE_CODE_VALUES)).forEach(
        (element) => {
          if (lang == element.code) {
            mandatoryLang = mandatoryLang + ", " + element.value;
          }
        }
      );
    });
    const mandatoryLangNames = mandatoryLang.substring(1, mandatoryLang.length);
    //create message to be displayed in popup
    let popupMainMessage = "";
    if (minLanguage == maxLanguage) {
      popupMainMessage = `${dataCaptureLabels.message[0]} ${minLanguage} ${dataCaptureLabels.message[3]}`;
    } else {
      popupMainMessage = `${dataCaptureLabels.message[1]} ${minLanguage} ${dataCaptureLabels.message[2]} ${maxLanguage} ${dataCaptureLabels.message[3]}`;
    }
    if (mandatoryLanguages.length > 0) {
      popupMainMessage += ` ${mandatoryLangNames} ${dataCaptureLabels.message[4]}`;
    }
    popupMainMessage += ` ${dataCaptureLabels.message[5]}`;
    //create attributes for popup
    let body = {};
    body = {
      case: "LANGUAGE_CAPTURE",
      title: dataCaptureLabels.title,
      dir: textDir,
      languages: JSON.parse(localStorage.getItem(appConstants.LANGUAGE_CODE_VALUES)),
      mandatoryLanguages: mandatoryLanguages,
      userPrefLanguage: userPrefLanguage,
      minLanguage: minLanguage,
      maxLanguage: maxLanguage,
      message: popupMainMessage,
      cancelButtonText: dataCaptureLabels.cancel_btn,
      submitButtonText: dataCaptureLabels.submit_btn,
      errorText:
        dataCaptureLabels.error_text[0] +
        " " +
        maxLanguage +
        " " +
        dataCaptureLabels.error_text[1],
    };
    return body;
  }

  static getApplicationLangs = (userRequest) => {
    const demographicData = userRequest.demographicDetails.identity;
    let applicationLanguages = [];
    if (demographicData) {
      let keyArr: any[] = Object.keys(demographicData);
      for (let index = 0; index < keyArr.length; index++) {
        const elementKey = keyArr[index];
        let dataArr = demographicData[elementKey];
        if (Array.isArray(dataArr)) {
          dataArr.forEach((dataArrElement) => {
            if (
              !applicationLanguages.includes(dataArrElement.language)
            ) {
              applicationLanguages.push(dataArrElement.language);
            }
          });
        }
      }
    } else if (userRequest.langCode) {
      applicationLanguages = [userRequest.langCode];
    }
    return applicationLanguages;
  }

  
  static getErrorCode  = (error: any) => {
    if (
      error[appConstants.ERROR] &&
      error[appConstants.ERROR][appConstants.NESTED_ERROR] &&
      error[appConstants.ERROR][appConstants.NESTED_ERROR].length > 0
    ) {
      return error[appConstants.ERROR][appConstants.NESTED_ERROR][0][appConstants.ERROR_CODE];
    } else {
      return "";
    }
  }
  
  static getErrorMessage  = (error: any) => {
    if (
      error[appConstants.ERROR] &&
      error[appConstants.ERROR][appConstants.NESTED_ERROR] &&
      error[appConstants.ERROR][appConstants.NESTED_ERROR].length > 0
    ) {
      return error[appConstants.ERROR][appConstants.NESTED_ERROR][0]["message"];
    } else {
      return "";
    }
  }
  
  static authenticationFailed = (error: any) => {
    //handle 401 exception
    const errorCode = Utils.getErrorCode(error);
    if (
      errorCode === appConstants.ERROR_CODES.tokenExpired ||
      errorCode === appConstants.ERROR_CODES.invalidateToken ||
      errorCode === appConstants.ERROR_CODES.authenticationFailed
    ) {
      return true;
    }
    return false;
  }

  static createErrorMessage = (error: any, errorlabels: any, apiErrorCodes: any, config: any) => {
    let message = errorlabels.error;
    let errorCode = "";
    if (Utils.authenticationFailed(error)) {
      message = errorlabels["sessionInvalidLogout"];
    } else {
      errorCode = Utils.getErrorCode(error);
      if (apiErrorCodes[errorCode]) {
        message = apiErrorCodes[errorCode];
      } 
    }
    const email = config[appConstants.CONFIG_KEYS.preregistration_contact_email];
    const phone = config[appConstants.CONFIG_KEYS.preregistration_contact_phone];
    if (!Utils.authenticationFailed(error)) {
      message = message + errorlabels["contactInformation"][0] + email + errorlabels["contactInformation"][1] + phone;
      if (errorCode != "") {
        message = message + errorlabels["contactInformation"][2] + errorCode;
      }
    }
    return message;
  }
}
