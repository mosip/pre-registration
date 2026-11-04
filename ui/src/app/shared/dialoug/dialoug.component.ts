import { Component, OnInit, Inject } from "@angular/core";
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material";
import { AuthService } from "src/app/auth/auth.service";
import * as appConstants from "../../app.constants";
import { ConfigService } from "src/app/core/services/config.service";
import { Router } from "@angular/router";
import { MatDialog } from "@angular/material";
import { RouterExtService } from "../router/router-ext.service";
import { DataStorageService } from "src/app/core/services/data-storage.service";
import { AuditModel } from "../models/demographic-model/audit.model";

export interface DialogData {
  case: number;
}

@Component({
  selector: "app-dialoug",
  templateUrl: "./dialoug.component.html",
  styleUrls: ["./dialoug.component.css"],
})
export class DialougComponent implements OnInit {
  input;
  message = {};
  selectedOption = null;
  confirm = true;
  isChecked = true;
  applicantNumber;
  checkCondition;
  applicantEmail;
  textDir = localStorage.getItem("dir");
  inputList = [];
  invalidApplicantNumber = false;
  invalidApplicantEmail = false;
  selectedName: any;
  addedList = [];
  disableAddButton = true;
  disableSend = true;
  selectedLanguage = [];
  disablelanguageSubmitBtn = true;
  constructor(
    public dialogRef: MatDialogRef<DialougComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData | any,
    private authService: AuthService,
    private config: ConfigService,
    private router: Router,
    private dialogBox: MatDialog,
    private routerService: RouterExtService,
    private dataService: DataStorageService
  ) {}

  ngOnInit() {
    this.input = this.data;
    if (this.input.case === "LANGUAGE_CAPTURE") {
      this.selectedLanguage = [...this.input.mandatoryLanguages];
      if (!this.input.mandatoryLanguages.includes(this.input.userPrefLanguage)) {
        this.selectedLanguage.push(this.input.userPrefLanguage);
      }
      this.enableDataCaptureSubmitBtn();
    }
  }

  onNoClick(input): void {
    this.dialogRef.close(input);
  }

  onSubmit(): void {
    this.onNoClick("");
  }

  validateMobile() {
    const re = new RegExp(
      this.config.getConfigByKey(appConstants.CONFIG_KEYS.mosip_regex_phone)
    );
    if (re.test(String(this.applicantNumber).toLowerCase())) {
      this.inputList[1] = this.applicantNumber;
      this.invalidApplicantNumber = false;
      this.disableSend = false;
    } else {
      this.invalidApplicantNumber = true;
      this.disableSend = true;
    }
  }

  validateEmail() {
    const re = new RegExp(
      this.config.getConfigByKey(appConstants.CONFIG_KEYS.mosip_regex_email)
    );
    if (re.test(String(this.applicantEmail).toLowerCase())) {
      this.inputList[0] = this.applicantEmail;
      this.invalidApplicantEmail = false;
      this.disableSend = false;
    } else {
      this.invalidApplicantEmail = true;
      this.disableSend = true;
    }
  }

  enableButton(email, mobile) {
    if (!email && !mobile) {
      this.disableSend = true;
      this.invalidApplicantEmail = false;
      this.invalidApplicantNumber = false;
    } else if (email && !mobile && !this.invalidApplicantEmail)
      this.disableSend = false;
    else if (mobile && !email && !this.invalidApplicantNumber)
      this.disableSend = false;
    else if (!this.invalidApplicantEmail && !this.invalidApplicantNumber)
      this.disableSend = false;
  }

  onSelectCheckbox() {
    this.isChecked = !this.isChecked;
  }

  onSelectLanguage(lang, event) {
    if (event && event.checked) {
      this.selectedLanguage.push(lang);
    } else {
      this.selectedLanguage.splice(this.selectedLanguage.indexOf(lang), 1);
    }
    this.enableDataCaptureSubmitBtn();
  }

  enableDataCaptureSubmitBtn() {

    if (
      this.selectedLanguage.length >= Number(this.input.minLanguage) &&
      this.selectedLanguage.length <= Number(this.input.maxLanguage)
    ) {
      console.log(this.input.minLanguage);
      this.disablelanguageSubmitBtn = false;
    } else {
      this.disablelanguageSubmitBtn = true;
    }
  }

  collectDataCaptureLanguage() {
    this.dialogRef.close(this.selectedLanguage);
  }

  cancelConsent(message) {
    let consentText = [];
    message.forEach((element) => {
      consentText.push(element["fileText"]);
    });
    let description = {
      url: localStorage.getItem("consentUrl"),
      template: consentText,
      description: "Consent Not Accepted",
    };
    let auditObj = new AuditModel();
    auditObj.actionUserId = localStorage.getItem("loginId");
    auditObj.eventName = "CONSENT";
    auditObj.description = JSON.stringify(description);
    this.dataService.logAudit(auditObj).subscribe((res) => {});
  }

  async userRedirection(textDirection) {
    if (
      localStorage.getItem(appConstants.NEW_APPLICANT) === "true" &&
      localStorage.getItem(appConstants.NEW_APPLICANT_FROM_PREVIEW) === "true"
    ) {
      await this.thirdPopUp(textDirection);
    } else if (
      localStorage.getItem(appConstants.NEW_APPLICANT) === "true" &&
      Number(localStorage.getItem("noOfApplicant")) > 0
    ) {
      await this.secondPopUp(textDirection);
    } else if (
      localStorage.getItem(appConstants.NEW_APPLICANT) === "true" &&
      Number(localStorage.getItem("noOfApplicant")) === 0
    ) {
      await this.firstPopUp(textDirection);
    }
  }

  firstPopUp(textDirection) {
    const data = {
      case: "MESSAGE",
      textDir: textDirection,
      message: this.input.alertMessageFirst,
    };
    this.dialogBox
      .open(DialougComponent, {
        width: "400px",
        data: data,
        disableClose: true
      })
      .afterClosed()
      .subscribe(() => this.loggingUserOut());
  }

  secondPopUp(textDirection) {
    const data = {
      case: "MESSAGE",
      textDir: textDirection,
      message: this.input.alertMessageSecond,
    };
    this.dialogBox
      .open(DialougComponent, {
        width: "400px",
        data: data,
        disableClose: true,
      })
      .afterClosed()
      .subscribe(() => this.redirectingUser());
  }

  thirdPopUp(textDirection) {
    const data = {
      case: "MESSAGE",
      textDir: textDirection,
      message: this.input.alertMessageThird,
    };
    this.dialogBox
      .open(DialougComponent, {
        width: "400px",
        data: data,
        disableClose: true,
      })
      .afterClosed()
      .subscribe(() => this.redirectingUser());
  }

  loggingUserOut() {
    this.authService.onLogout();
  }
  redirectingUser() {
    let url = this.routerService.getPreviousUrl();
    console.log(url);
    let preRegId = "";
    if (url) {
      preRegId = url.split("/")[4];
    }
    console.log(`preRegId: ${preRegId}`);
    if (localStorage.getItem(appConstants.NEW_APPLICANT_FROM_PREVIEW) === "true") {
      this.router.navigate([
        `${localStorage.getItem(
          "langCode"
        )}/pre-registration/summary/${preRegId}/preview`,
      ]);
    } else
      this.router.navigate([`${localStorage.getItem("langCode")}/dashboard`]);
  }

  applicationCancelAndDiscardSubmit(selectedOption) {
      this.dialogRef.close(selectedOption);
  }
}
