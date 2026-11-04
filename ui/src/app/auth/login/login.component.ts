import { Component, OnInit } from "@angular/core";
import { Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { DialougComponent } from "src/app/shared/dialoug/dialoug.component";
import { MatDialog } from "@angular/material";
import { AuthService } from "../auth.service";
import { DataStorageService } from "src/app/core/services/data-storage.service";
import { RegistrationService } from "src/app/core/services/registration.service";
import { ConfigService } from "src/app/core/services/config.service";
import * as appConstants from "../../app.constants";
import Utils from "src/app/app.util";
import moment from "moment";
import {first} from "rxjs/operators";

@Component({
  selector: "app-login",
  templateUrl: "./login.component.html",
  styleUrls: ["./login.component.css"],
})
export class LoginComponent implements OnInit {
  appVersion;
  disableBtn = false;
  timer: any;
  inputOTP: string;
  inputContactDetails = "";
  selectedLanguage = "";
  dir = "ltr";
  showSendOTP = true;
  showResend = false;
  showVerify = false;
  showContactDetails = true;
  showOTP = false;
  disableVerify = false;
  Languagelabels: any;
  loggedOutLang: string;
  errorMessage: string;
  loadingMessage: string;
  minutes: string;
  seconds: string;
  showSpinner = true;
  showLanguageDropDown = true;
  validationMessages = {};
  siteKey: any;
  enableCaptcha = false;
  enableSendOtp: boolean;
  showCaptcha = true;
  captchaError: boolean;
  mandatoryLanguages: string[];
  optionalLanguages: string[];
  minLanguage: number;
  maxLanguage: number;
  languageSelectionArray = [];
  userPreferredLanguage: string;
  langCode: string;
  LanguageCodelabels;
  languageCodeValue: any = [];
  captchaToken = null;
  resetCaptcha: boolean;
  constructor(
    private authService: AuthService,
    private router: Router,
    private translate: TranslateService,
    private dialog: MatDialog,
    private dataService: DataStorageService,
    private regService: RegistrationService,
    private configService: ConfigService
  ) {
    clearInterval(this.timer);
  }

  //very imp to clear the timer func 
  ngOnDestroy(){
    clearInterval(this.timer);
  }

  ngOnInit(): void {
    const forceLogout = localStorage.getItem(appConstants.FORCE_LOGOUT);
    const langCode = localStorage.getItem('langCode');
    if (forceLogout !== appConstants.FORCE_LOGOUT_YES && this.authService.isAuthenticated() && langCode) {
      this.router.navigate([langCode, 'dashboard']);
    } else {
      // fire & forget async flow
      this.initializeAfterLogout(forceLogout, langCode).catch(err => {
        // Optional: centralize error handling/logging
        console.error('Error during initialization after logout', err);
      });
    }
  }

  private async initializeAfterLogout(forceLogout: string | null, langCode: string | null): Promise<void> {
    if (forceLogout === appConstants.FORCE_LOGOUT_YES) {
      this.authService.onLogout();
    }
    await this.loadDefaultConfig();
    await this.loadConfigs();
    if (langCode && this.router.url.includes(langCode)) {
      this.handleBrowserReload();
    }
    localStorage.setItem('dir', this.dir);
  }

  async loadDefaultConfig() {
    return new Promise((resolve, reject) => {
      this.dataService.getI18NLanguageFiles("default").subscribe((response) => {
        this.LanguageCodelabels = response["languages"];
        resolve(true);
      });
    });
  }

  async loadConfigs(): Promise<void> {
    const response = await this.dataService.getConfig().pipe(first()).toPromise();
    this.configService.setConfig(response);
    this.appVersion = this.configService.getConfigByKey(
      "preregistration.ui.version"
    );
    this.isCaptchaEnabled();
    this.loadLanguagesWithConfig();
    let urlLangCode = this.router.url.split("/").pop();
    if (this.languageSelectionArray.indexOf(urlLangCode) !== -1){
      localStorage.setItem("langCode", urlLangCode);
    }
    if (!localStorage.getItem("langCode")) {
      localStorage.setItem("langCode", this.languageSelectionArray[0]);
    }
    let langCodeInUrl = this.router.url.includes(`${localStorage.getItem("langCode")}`);
    if (!langCodeInUrl) {
      this.router.navigate([`${localStorage.getItem("langCode")}`]);
    }
    this.selectedLanguage = localStorage.getItem("langCode");
    this.setLanguageDirection(this.selectedLanguage);
    this.authService.userPreferredLang = this.selectedLanguage;
    this.userPreferredLanguage = this.selectedLanguage;
    localStorage.setItem("userPrefLanguage", this.userPreferredLanguage);
    this.translate.use(this.userPreferredLanguage);
    this.loadValidationMessages();
    this.showSpinner = false;
  }

  handleBrowserReload() {
    let otp_sent_time = null;
    if(localStorage.getItem("otp_sent_time") != null){
      otp_sent_time = localStorage.getItem("otp_sent_time").endsWith("Z")
      ? localStorage.getItem("otp_sent_time")
      : localStorage.getItem("otp_sent_time") + "Z";
    } 
    const user_email_or_phone = localStorage.getItem("user_email_or_phone");
    console.log(`otp_sent_time: ${otp_sent_time}`);
    if (otp_sent_time && user_email_or_phone) {
      let otpSentTime = moment(otp_sent_time).toISOString();
      let currentTime = moment().toISOString();
      let otpExpiryIntervalInSeconds = Number(
        this.configService.getConfigByKey(
          appConstants.CONFIG_KEYS.mosip_kernel_otp_expiry_time
        )
      );
      if (Number.isNaN(otpExpiryIntervalInSeconds)) {
        otpExpiryIntervalInSeconds = 120; //2 mins by default
      }
      var timeLapsedInSeconds = moment(currentTime).diff(
        moment(otpSentTime),
        "seconds"
      );
      if (timeLapsedInSeconds <= otpExpiryIntervalInSeconds) {
        console.log("otp interval not yet expired");
        let newOtpIntervalInSeconds =
          otpExpiryIntervalInSeconds - timeLapsedInSeconds;
        console.log(`newOtpIntervalInSeconds: ${newOtpIntervalInSeconds}`);
        if (JSON.parse(localStorage.getItem("show_captcha"))) {
          this.showCaptcha = true;
        } else {
          this.showCaptcha = false;
        }
        this.errorMessage = "";
        this.inputOTP = "";
        this.showOTP = true;
        this.showSendOTP = false;
        this.showContactDetails = false;
        this.enableSendOtp = false;
        this.inputContactDetails = user_email_or_phone;
        let mins = 0;
        if (newOtpIntervalInSeconds >= 60) {
          mins = newOtpIntervalInSeconds / 60;
          mins = Math.floor(mins);
        }
        if (mins < 10) {
          this.minutes = "0" + mins;
        } else {
          this.minutes = String(mins);
        }
        let secs = newOtpIntervalInSeconds % 60;
        if (secs < 10) {
          this.seconds = "0" + secs;
        } else {
          this.seconds = String(secs);
        }
        this.timer = setInterval(this.timerFn, 1000);
      } else {
        console.log("otp interval expired");
        localStorage.removeItem("otp_sent_time");
        localStorage.removeItem("user_email_or_phone");
        localStorage.removeItem("show_captcha");
      }
    }
  }

  loadLanguagesWithConfig() {
    this.mandatoryLanguages = Utils.getMandatoryLangs(this.configService);
    this.optionalLanguages = Utils.getOptionalLangs(this.configService);
    this.minLanguage = Utils.getMinLangs(this.configService);
    this.maxLanguage = Utils.getMaxLangs(this.configService);
    this.languageSelectionArray = [
      ...this.mandatoryLanguages,
      ...this.optionalLanguages,
    ];
    this.showLanguageDropDown = this.maxLanguage == 1 ? false : true;
    localStorage.setItem(
      "availableLanguages",
      JSON.stringify(this.languageSelectionArray)
    );
    this.languageSelectionArray.forEach(async (language) => {
      let localeId = language.substring(0, 2);
      if (this.LanguageCodelabels[language]) {
        localeId = this.LanguageCodelabels[language].locale;
        if (localeId && localeId.indexOf("_") != -1) {
          localeId = localeId.split("_")[0];
        }
        if (localeId) {
          if (localStorage.getItem(localeId) == null) {
            console.log(`importing localeId: ${localeId}`);
            await Utils.localeInitializer(localeId);
          }
        }
      }
    });
    this.prepareDropdownLabelArray();
  }

  prepareDropdownLabelArray() {
    this.languageSelectionArray.forEach((language) => {
      let codevalue = {
        code: language,
        value: this.LanguageCodelabels[language].nativeName,
        locale: this.LanguageCodelabels[language].locale,
      };
      this.languageCodeValue.push(codevalue);
    });
    localStorage.setItem(
      appConstants.LANGUAGE_CODE_VALUES,
      JSON.stringify(this.languageCodeValue)
    );
  }

  isCaptchaEnabled() {
    if (
      this.configService.getConfigByKey(
        "mosip.preregistration.captcha.enable"
      ) === "false" ||
      this.configService.getConfigByKey(
        "mosip.preregistration.captcha.enable"
      ) === undefined
    ) {
      this.enableCaptcha = false;
    } else if (
      this.configService.getConfigByKey(
        "mosip.preregistration.captcha.enable"
      ) === "true"
    ) {
      this.enableCaptcha = true;
      this.loadRecaptchaSiteKey();
    }
    if (!this.enableCaptcha) {
      this.enableSendOtp = true;
    }
  }

  loadRecaptchaSiteKey() {
    this.siteKey = this.configService.getConfigByKey(
      "mosip.preregistration.captcha.sitekey"
    );
  }

  loadValidationMessages() {
    this.dataService
      .getI18NLanguageFiles(localStorage.getItem("langCode"))
      .subscribe((response) => {
        this.Languagelabels = response;
        this.validationMessages = this.Languagelabels["login"];
      });
  }

  setLanguageDirection(userSelectedLanguage) {
    const ltrLangs = this.configService
      .getConfigByKey(appConstants.CONFIG_KEYS.mosip_left_to_right_orientation)
      .split(",");
    if (ltrLangs.includes(userSelectedLanguage)) {
      this.dir = "ltr";
    } else {
      this.dir = "rtl";
    }
    localStorage.setItem("dir", this.dir);
  }

  setTimer() {
    const time = Number(
      this.configService.getConfigByKey(
        appConstants.CONFIG_KEYS.mosip_kernel_otp_expiry_time
      )
    );

    if (!Number.isNaN(time)) {
      const minutes = time / 60;
      const seconds = time % 60;
      if (minutes < 10) {
        this.minutes = "0" + minutes;
      } else {
        this.minutes = String(minutes);
      }
      if (seconds < 10) {
        this.seconds = "0" + seconds;
      } else {
        this.seconds = String(seconds);
      }
    } else {
      this.minutes = "02";
      this.seconds = "00";
    }
  }

  changeLanguage(): void {
    this.setLanguageDirection(this.selectedLanguage);
    this.authService.userPreferredLang = this.selectedLanguage;
    this.userPreferredLanguage = this.selectedLanguage;
    localStorage.setItem("userPrefLanguage", this.selectedLanguage);
    localStorage.setItem("langCode", this.selectedLanguage);
    this.translate.use(this.userPreferredLanguage);
    this.router.navigate([this.userPreferredLanguage]);
    this.loadValidationMessages();
    this.errorMessage = "";
  }

  showVerifyBtn() {
    if (
      this.inputOTP.length ===
      Number(
        this.configService.getConfigByKey(
          appConstants.CONFIG_KEYS.mosip_kernel_otp_default_length
        )
      )
    ) {
      this.errorMessage = "";
      this.showVerify = true;
    } else {
      this.showVerify = false;
    }
  }

  verifyInput() {
    this.errorMessage ="";
  }

  timerFn = () => {
    let secValue,
      minValue = 0;
    if (
      document.getElementById("timer") && 
      document.getElementById("timer").style.visibility == "visible" && 
      document.getElementById("secondsSpan") &&
      document.getElementById("secondsSpan").innerText &&
      document.getElementById("minutesSpan") &&
      document.getElementById("minutesSpan").innerText
    ) {
      secValue = Number(document.getElementById("secondsSpan").innerText);
      minValue = Number(document.getElementById("minutesSpan").innerText);
    } else {
      secValue = Number(this.seconds);
      minValue = Number(this.minutes);
      if (document.getElementById("timer")) {
        document.getElementById("timer").style.visibility = "visible";
      }
    }
    if (secValue === 0) {
      secValue = 60;
      if (minValue === 0) {
        // console.log("redirecting to initial phase on completion of timer");
        // redirecting to initial phase on completion of timer
        this.showContactDetails = true;
        this.showSendOTP = true;
        this.showOTP = false;
        this.showVerify = false;
        this.enableSendOtp = true;
        if (this.enableCaptcha) {
          this.showCaptcha = true;
          this.enableSendOtp = false;
        }
        if (document.getElementById("minutesSpan")) {
          document.getElementById("minutesSpan").innerText = this.minutes;
        }
        if (document.getElementById("timer")) {
          document.getElementById("timer").style.visibility = "hidden";
        }
        clearInterval(this.timer);
        return;
      }
      if (document.getElementById("minutesSpan") &&
        document.getElementById("minutesSpan").innerText) {
        document.getElementById("minutesSpan").innerText = "0" + (minValue - 1);
      }
    }
    if (document.getElementById("secondsSpan") &&
      document.getElementById("secondsSpan").innerText) {
      let newSecVal = --secValue;
      if (secValue === 10 || secValue < 10) {
        document.getElementById("secondsSpan").innerText = "0" + newSecVal;
      } else {
        document.getElementById("secondsSpan").innerText = newSecVal + "";
      }
    }
  };

  submit() {
    this.loginIdValidator();
    this.resetCaptcha = false;
    if (
      (this.showSendOTP || this.showResend) &&
      this.errorMessage == "" &&
      this.enableSendOtp
    ) {
      this.loadingMessage = this.validationMessages["loading"];
      this.dataService
        .sendOtpWithCaptcha(
          this.inputContactDetails,
          this.userPreferredLanguage,
          this.captchaToken
        )
        .subscribe(
          (response) => {
            this.loadingMessage = "";
            if (!response["errors"]) {
              let otpSentTime = response["responsetime"];
              localStorage.setItem("otp_sent_time", String(otpSentTime));
              localStorage.setItem(
                "user_email_or_phone",
                this.inputContactDetails
              );
              this.errorMessage = undefined;
              this.inputOTP = "";
              this.showOTP = true;
              this.showSendOTP = false;
              this.showContactDetails = false;
              localStorage.setItem("show_captcha", JSON.stringify(false));
              this.showCaptcha = false;
              // initial set up for timer
              // console.log("setting timer");
              this.setTimer();
              if (document.getElementById("timer")) {
                document.getElementById("timer").style.visibility = "visible";
              }
              if (document.getElementById("secondsSpan")) {
                document.getElementById("secondsSpan").innerText = this.seconds;
              }
              if (document.getElementById("minutesSpan")) {
                document.getElementById("minutesSpan").innerText = this.minutes;
              }
              this.timer = setInterval(this.timerFn, 1000);
            } 
          },
          (error) => {
            clearInterval(this.timer);
            if (this.enableCaptcha){
              this.resetCaptcha = true;
              this.captchaToken = null;
              this.enableSendOtp = false;  
              console.log("Resetting captcha:" + this.resetCaptcha);
            }
            this.loadingMessage = "";
            const otpFailedToSendMsg = this.validationMessages["serverUnavailable"];
            this.showErrorMessage(error, otpFailedToSendMsg);
          }
        );
      // dynamic update of button text for Resend and Verify
    } else if (this.showVerify && this.errorMessage == "") {
      this.disableVerify = true;
      this.dataService
        .verifyOtp(this.inputContactDetails, this.inputOTP)
        .subscribe(
          (response) => {
            if (!response[appConstants.NESTED_ERROR]) {
              clearInterval(this.timer);
              localStorage.setItem("loggedIn", "true");
              this.authService.setToken();
              this.regService.setLoginId(this.inputContactDetails);
              localStorage.setItem("loginId", this.inputContactDetails);
              this.disableVerify = false;
              this.router.navigate([this.userPreferredLanguage, "dashboard"]);
            } 
          },
          (error) => {
            this.inputOTP = "";
            this.disableVerify = false;
            this.showVerify = false;
            let optInvalidMsg = this.Languagelabels["message"]["login"]["msg3"];
            this.showErrorMessage(error, optInvalidMsg);
            const errCode = Utils.getErrorCode(error);
            if (errCode === appConstants.ERROR_CODES.otpAttemptExceeded){
              this.showContactDetails = true;
              this.showSendOTP = true;
              this.showOTP = false;
              this.showVerify = false;
              this.enableSendOtp = true;
              if (this.enableCaptcha) {
                this.showCaptcha = true;
                this.enableSendOtp = false;
              }
              if (document.getElementById("minutesSpan")) {
                document.getElementById("minutesSpan").innerText = this.minutes;
              }
              if (document.getElementById("timer")) {
                document.getElementById("timer").style.visibility = "hidden";
              }
              clearInterval(this.timer);
              console.log("otp validation attempt exceeded");
              localStorage.removeItem("otp_sent_time");
              localStorage.removeItem("user_email_or_phone");
              localStorage.removeItem("show_captcha");
            }
          }
        );
    }
  }

  loginIdValidator() {
    this.errorMessage = "";

    const modes = this.configService.getConfigByKey(
      appConstants.CONFIG_KEYS.mosip_login_mode
    );
    const emailRegex = new RegExp(
      this.configService.getConfigByKey(
        appConstants.CONFIG_KEYS.mosip_regex_email
      )
    );
    const phoneRegex = new RegExp(
      this.configService.getConfigByKey(
        appConstants.CONFIG_KEYS.mosip_regex_phone
      )
    );
    if (modes === "email,mobile") {
      if (
        !(
          emailRegex.test(this.inputContactDetails) ||
          phoneRegex.test(this.inputContactDetails)
        )
      ) {
        this.errorMessage = this.validationMessages["invalidInput"];
      }
    } else if (modes === "email") {
      if (!emailRegex.test(this.inputContactDetails)) {
        this.errorMessage = this.validationMessages["invalidEmail"];
      }
    } else if (modes === "mobile") {
      if (!phoneRegex.test(this.inputContactDetails)) {
        this.errorMessage = this.validationMessages["invalidMobile"];
      }
    }
  }

  getCaptchaToken(event: Event) {
    if (event !== undefined && event != null) {
      console.log("Captcha event " + event);
      this.captchaToken = event;
      this.enableSendOtp = true;
    } else {
      console.log("Captcha has expired" + event);
      this.enableSendOtp = false;
    }
  }

  showOtpMessage() {
    this.inputOTP = "";
    let response = this.Languagelabels;
    let otpmessage = response["message"]["login"]["msg3"];
    const message = {
      case: "MESSAGE",
      message: otpmessage,
    };
    this.dialog.open(DialougComponent, {
      width: "400px",
      data: message,
    });
  }

  /**
   * @description This is a dialoug box whenever an error comes from the server, it will appear.
   *
   * @private
   * @memberof DemographicComponent
   */
   private showErrorMessage(error: any, customMsg?: string) {
    let errorlabels = this.Languagelabels[appConstants.ERROR];
    let apiErrorCodes = this.Languagelabels[appConstants.API_ERROR_CODES];
    const titleOnError = errorlabels.errorLabel;
    const errorCode = Utils.getErrorCode(error);
    let message = "";
    if (apiErrorCodes[errorCode]) {
      message = apiErrorCodes[errorCode];
    } else {
      message = customMsg; 
    }
    const body = {
      case: "ERROR",
      title: titleOnError,
      message: message,
      yesButtonText: errorlabels.button_ok,
    };
    this.dialog.open(DialougComponent, {
      width: "400px",
      data: body,
    });
  }
}
