import { Component, OnInit, OnDestroy } from "@angular/core";
import * as html2pdf from "html2pdf.js";
import { MatDialog } from "@angular/material";
import { BookingService } from "../../booking/booking.service";
import { DialougComponent } from "src/app/shared/dialoug/dialoug.component";
import { TranslateService } from "@ngx-translate/core";
import { DataStorageService } from "src/app/core/services/data-storage.service";
import { NotificationDtoModel } from "src/app/shared/models/notification-model/notification-dto.model";
import Utils from "src/app/app.util";
import * as appConstants from "../../../app.constants";
import { RequestModel } from "src/app/shared/models/request-model/RequestModel";
import { Subscription } from "rxjs";
import { ConfigService } from "src/app/core/services/config.service";
import { ActivatedRoute, Router } from "@angular/router";
import { NameList } from "src/app/shared/models/demographic-model/name-list.modal";
import { UserModel } from "src/app/shared/models/demographic-model/user.modal";

@Component({
  selector: "app-acknowledgement",
  templateUrl: "./acknowledgement.component.html",
  styleUrls: ["./acknowledgement.component.css"],
})
export class AcknowledgementComponent implements OnInit, OnDestroy {
  usersInfoArr = [];
  ackDataArr = [];
  ackDataItem = {};
  guidelines = [];
  guidelinesDetails = [];
  pdfOptions = {};
  fileBlob: Blob;
  errorlabels: any;
  apiErrorCodes: any;
  showSpinner: boolean = true;
  //notificationRequest = new FormData();
  bookingDataPrimary = "";
  bookingDataSecondary = "";
  subscriptions: Subscription[] = [];
  notificationTypes: string[];
  preRegIds: any;
  ltrLangs = this.configService
    .getConfigByKey(appConstants.CONFIG_KEYS.mosip_left_to_right_orientation)
    .split(",");
  regCenterId;
  langCode;
  textDir = localStorage.getItem("dir");
  name = "";
  applicantContactDetails = [];
  constructor(
    private bookingService: BookingService,
    private dialog: MatDialog,
    private translate: TranslateService,
    private dataStorageService: DataStorageService,
    private configService: ConfigService,
    private activatedRoute: ActivatedRoute,
    private router: Router
  ) {
    this.translate.use(localStorage.getItem("langCode"));
    this.langCode = localStorage.getItem("langCode");
  }


  ngOnInit(): void {
    if (this.router.url.includes('multiappointment')) {
      const stored = localStorage.getItem('multiappointment');
      try {
        this.preRegIds = stored ? [...JSON.parse(stored)] : [];
      } catch {
        this.preRegIds = [];
      }
      // we already know preRegIds → start async init
      void this.initAsync().catch(error => {
        console.error('Initialization failed:', error);
        this.showSpinner = false;
      });
    } else {
      // wait for appId from route before starting async init
      this.activatedRoute.params.subscribe((param) => {
        this.preRegIds = [param['appId']];
        void this.initAsync().catch(error => {
          console.error('Initialization failed:', error);
          this.showSpinner = false;
        });
      });
    }
    const subs = this.dataStorageService
      .getI18NLanguageFiles(this.langCode)
      .subscribe((response) => {
        this.errorlabels = response[appConstants.ERROR];
        this.apiErrorCodes = response[appConstants.API_ERROR_CODES];
      });
    this.subscriptions.push(subs);
    this.name = this.configService.getConfigByKey(
      appConstants.CONFIG_KEYS.preregistration_identity_name
    );
  }

  private async initAsync(): Promise<void> {
    await this.getUserInfo(this.preRegIds);
    // console.log(this.usersInfoArr);
    for (let i = 0; i < this.usersInfoArr.length; i++) {
      await this.getRegCenterDetails(this.usersInfoArr[i].langCode, i);
      await this.getLabelDetails(this.usersInfoArr[i].langCode, i);
      await this.getUserLangLabelDetails(this.langCode, i);
    }
    const notificationTypes = this.configService
      .getConfigByKey(appConstants.CONFIG_KEYS.mosip_notification_type)
      .split('|');
    this.notificationTypes = notificationTypes.map((item) =>
      item.toUpperCase()
    );
    await this.apiCalls();
    if (this.bookingService.getSendNotification()) {
      this.bookingService.resetSendNotification();
      await this.automaticNotification();
    }
    this.prepareAckDataForUI();
    this.showSpinner = false;
  }

  getUserInfo(preRegIds: string[]) {
    return new Promise((resolve) => {
      preRegIds.forEach(async (prid: any, index) => {
        await this.getUserDetails(prid).then(async (user) => {
          let regDto;
          //console.log(user);
          await this.getAppointmentDetails(prid).then((appointmentDetails) => {
            regDto = appointmentDetails;
          });
          let demographicData = {};
          let applicationLanguages = [];
          if (user["request"] && user["request"].demographicDetails) {
            demographicData = user["request"].demographicDetails.identity;
            applicationLanguages = Utils.getApplicationLangs(user["request"]);
          }
          if (applicationLanguages.length == 0) {
            applicationLanguages = [this.langCode];
          }
          applicationLanguages = Utils.reorderLangsForUserPreferredLang(applicationLanguages, this.langCode);
          applicationLanguages.forEach(applicationLang => {
            const nameListObj: NameList = {
              preRegId: "",
              fullName: "",
              regDto: "",
              status: "",
              registrationCenter: "",
              bookingData: "",
              postalCode: "",
              langCode: "",
              labelDetails: [],
              userLangLabelDetails: []
            };
            if (user["request"].preRegistrationId) {
              nameListObj.preRegId = user["request"].preRegistrationId;
            } else {
              nameListObj.preRegId = user["request"].applicationId;
            }
            nameListObj.status = user["request"].statusCode;
            if (demographicData[this.name]) {
              let nameValues = demographicData[this.name];
              nameValues.forEach(nameVal => {
                if (nameVal["language"] == applicationLang) {
                  nameListObj.fullName = nameVal["value"];
                }
              });  
            }
            if (demographicData["postalCode"]) {
              nameListObj.postalCode = demographicData["postalCode"];
            }
            nameListObj.registrationCenter = "";
            nameListObj.langCode = applicationLang;
            nameListObj.regDto = regDto;
            this.usersInfoArr.push(nameListObj);
            console.log(this.usersInfoArr);
            if (user["request"] && user["request"].demographicDetails) {
              this.applicantContactDetails.push({
                "preRegId": user["request"].preRegistrationId,
                "phone": demographicData["phone"],
                "email": demographicData["email"]
              });
            } else {
              const emailRegex = new RegExp(
                this.configService.getConfigByKey(
                  appConstants.CONFIG_KEYS.mosip_regex_email
                )
              );
              const loginId = localStorage.getItem("loginId");
              console.log(loginId);
              let isloginIdEmail = false;
              if (emailRegex.test(loginId)) {
                isloginIdEmail = true;
              }
              this.applicantContactDetails.push({
                "preRegId": user["request"].applicationId,
                "phone": !isloginIdEmail? loginId: null,
                "email": isloginIdEmail? loginId: null
              });
            } 
          });
        });
        if (index === preRegIds.length - 1) {
          resolve(true);
        }
      });
    });
  }

  getUserDetails(prid) {
    return new Promise((resolve) => {
      this.dataStorageService.getUser(prid.toString()).subscribe((response) => {
        if (response[appConstants.RESPONSE] !== null) {
          resolve(
            new UserModel(
              prid.toString(),
              response[appConstants.RESPONSE],
              undefined,
              []
            )
          );
        }
      },
      (error) => {
        this.dataStorageService.getApplicationDetails(prid.toString()).subscribe((response) => {
          resolve(
            new UserModel(
              prid.toString(),
              response[appConstants.RESPONSE],
              undefined,
              []
            )
          );
        },
        (error) => {
          this.showErrorMessage(error);
        });
      });
    });
  }

  getAppointmentDetails(preRegId) {
    return new Promise((resolve) => {
      this.dataStorageService
        .getAppointmentDetails(preRegId)
        .subscribe((response) => {
          //console.log(response);
          if (response[appConstants.RESPONSE]) {
            this.regCenterId =
            response[appConstants.RESPONSE].registration_center_id;
          }
          resolve(response[appConstants.RESPONSE]);
        },
        (error) => {
          this.showErrorMessage(error);
        });
    });
  }

  getRegCenterDetails(langCode, index) {
    return new Promise((resolve) => {
      this.dataStorageService
        .getRegistrationCentersById(this.regCenterId, langCode)
        .subscribe((response) => {
          if (response[appConstants.RESPONSE]) {
            this.usersInfoArr[index].registrationCenter =
              response[appConstants.RESPONSE].registrationCenters[0];
            resolve(true);
          }
        },
        (error) => {
          this.usersInfoArr[index].registrationCenter = "";
          resolve(true);
          //suppress the err popup, as reg center maybe added only in one lang
          //this.showErrorMessage(error);
        });
    });
  }

  async getLabelDetails(langCode, index) {
    return new Promise((resolve) => {
      this.dataStorageService
      .getI18NLanguageFiles(langCode)
      .subscribe((response) => {
        this.usersInfoArr[index].labelDetails.push(response["acknowledgement"]);
        //console.log(this.usersInfoArr[index].labelDetails);
        resolve(true);
      });
    });
  }

  async getUserLangLabelDetails(langCode, index) {
    return new Promise((resolve) => {
      this.dataStorageService
      .getI18NLanguageFiles(langCode)
      .subscribe((response) => {
        this.usersInfoArr[index].userLangLabelDetails.push(response["acknowledgement"]);
        //console.log(this.usersInfoArr[index].labelDetails);
        resolve(true);
      });
    });
  }

  prepareAckDataForUI() {
    this.preRegIds.forEach(prid => {
      let ackDataItem = {
        "qrCodeBlob": null,
      };
      let preRegIdLabels = [],
      appDateLabels = [],
      contactPhoneLabels = [],
      messages = [],
      labelNames = [],
      nameValues = [],
      labelRegCntrs = [],
      regCntrNames = [],
      appLangCode = [],
      bookingDataPrimary = [],
      bookingTimePrimary = [];

      this.ackDataItem["preRegId"] = prid;
      
      this.ackDataItem["contactPhone"] =
        this.usersInfoArr[0].registrationCenter.contactPhone;
      
      this.usersInfoArr.forEach(userInfo => {
        if (userInfo.preRegId == prid) {
          this.ackDataItem["qrCodeBlob"] = userInfo.qrCodeBlob;
          const labels = userInfo.labelDetails[0];
          preRegIdLabels.push(labels.label_pre_id);
          appDateLabels.push(labels.label_appointment_date_time);
          contactPhoneLabels.push(labels.label_cntr_contact_number);
          labelNames.push(labels.label_name);
          labelRegCntrs.push(labels.label_reg_cntr);
          nameValues.push(userInfo.fullName);
          //console.log(userInfo.registrationCenter.name);
          if (userInfo.registrationCenter.name) {
            regCntrNames.push(userInfo.registrationCenter.name);
          }
          appLangCode.push(userInfo.langCode);
          //set the message in user login lang if available
          let fltrLangs = this.usersInfoArr.filter(userInfoItm => userInfoItm.preRegId == userInfo.preRegId && userInfoItm.langCode == this.langCode);
          if (fltrLangs.length == 1) {
            //matching lang found
            bookingTimePrimary.push({
              langCode: userInfo.langCode,
              time:userInfo.bookingTimePrimary,
              langAvailable: true
            });
            bookingDataPrimary.push({
              langCode: userInfo.langCode,
              date:userInfo.bookingDataPrimary,
              langAvailable: true
            });  
            let fltr = messages.filter(msg => msg.preRegId == fltrLangs[0].preRegId);
            if (fltr.length == 0) {
              messages.push({
                "preRegId": fltrLangs[0].preRegId,
                "message": fltrLangs[0].labelDetails[0].message
              });
            }
          } else {
            //matching lang found
            bookingTimePrimary.push({
              langCode: userInfo.langCode,
              time:userInfo.bookingTimePrimary,
              langAvailable: false
            });
            bookingDataPrimary.push({
              langCode: userInfo.langCode,
              date:userInfo.bookingDataPrimary,
              langAvailable: false
            });  
            let fltr = messages.filter(msg => msg.preRegId == userInfo.preRegId);
            if (fltr.length == 0) {
              messages.push({
                "preRegId": userInfo.preRegId,
                "message": userInfo.userLangLabelDetails[0].message
              });  
            }
          }
        }
      });
      //console.log(appLangCode);
      if (this.ltrLangs.includes(appLangCode[0])) {
        this.ackDataItem["appLangCodeDir"] = "ltr";
      } else {
        this.ackDataItem["appLangCodeDir"] = "rtl";
      }
      this.ackDataItem["appLangCode"] = appLangCode;
      this.ackDataItem["bookingTimePrimary"] = bookingTimePrimary;
      this.ackDataItem["bookingDataPrimary"] = bookingDataPrimary;
      this.ackDataItem["preRegIdLabels"] = JSON.stringify(
        preRegIdLabels
      )
        .replace(/\[/g, "")
        .replace(/,/g, " / ")
        .replace(/"/g, " ")
        .replace(/]/g, "");
      this.ackDataItem["appDateLabels"] = JSON.stringify(appDateLabels)
        .replace(/\[/g, "")
        .replace(/,/g, " / ")
        .replace(/"/g, " ")
        .replace(/]/g, "");
      this.ackDataItem["contactPhoneLabels"] = JSON.stringify(
        contactPhoneLabels
      )
        .replace(/\[/g, "")
        .replace(/,/g, " / ")
        .replace(/"/g, " ")
        .replace(/]/g, "");
      this.ackDataItem["messages"] = messages;
      this.ackDataItem["labelNames"] = JSON.stringify(labelNames)
        .replace(/\[/g, "")
        .replace(/,/g, " / ")
        .replace(/"/g, " ")
        .replace(/]/g, "");
      this.ackDataItem["nameValues"] = JSON.stringify(nameValues)
        .replace(/\[/g, "")
        .replace(/,/g, " / ")
        .replace(/"/g, " ")
        .replace(/]/g, "");
      this.ackDataItem["labelRegCntrs"] = JSON.stringify(labelRegCntrs)
        .replace(/\[/g, "")
        .replace(/,/g, " / ")
        .replace(/"/g, " ")
        .replace(/]/g, "");
      this.ackDataItem["regCntrNames"] = JSON.stringify(regCntrNames)
        .replace(/\[/g, "")
        .replace(/,/g, " / ")
        .replace(/"/g, " ")
        .replace(/]/g, "");
      for (let j = 0; j < this.guidelines.length; j++) {
        if (appLangCode.includes(this.guidelines[j].langCode)) {
          this.ackDataItem[
            this.guidelines[j].langCode
          ] = this.guidelines[j].fileText.split("\n");
        }
      }
      console.log(this.ackDataItem);
      this.ackDataArr.push(this.ackDataItem);
      this.ackDataItem = {};
    });
    
  }

  async apiCalls() {
    this.formatDateTime();
    await this.qrCodeForUser();
    await this.getTemplate();
  }

  async qrCodeForUser() {
    return new Promise((resolve) => {
      this.usersInfoArr.forEach(async (user) => {
        //console.log(user);
        await this.generateQRCode(user);
        if (this.usersInfoArr.indexOf(user) === this.usersInfoArr.length - 1) {
          resolve(true);
        }
      });
    });
  }

  formatDateTime() {
    const ltrLangs = this.configService
    .getConfigByKey(appConstants.CONFIG_KEYS.mosip_left_to_right_orientation)
    .split(",");
    this.usersInfoArr.forEach(userInfo => {
      if (!userInfo.bookingData) {
        userInfo.bookingDataPrimary = Utils.getBookingDateTime(
          userInfo.regDto.appointment_date,
          userInfo.regDto.time_slot_from,
          userInfo.langCode,
          ltrLangs
        );
        userInfo.bookingTimePrimary = Utils.formatTime(
          userInfo.regDto.time_slot_from
        );
      } else {
        const date = userInfo.bookingData.split(",");
        userInfo.bookingDataPrimary = Utils.getBookingDateTime(
          date[0],
          date[1],
          userInfo.langCode,
          ltrLangs
        );
        userInfo.bookingTimePrimary = Utils.formatTime(date[1]);
      }    
    });  
  }

  automaticNotification() {
    setTimeout(() => {
      this.sendNotification(this.applicantContactDetails, false);
    }, 500);
  }

  getTemplate() {
    return new Promise((resolve) => {
      const subs = this.dataStorageService
        .getGuidelineTemplate("Onscreen-Acknowledgement")
        .subscribe((response) => {
          this.guidelines = response["response"]["templates"];
          //console.log(this.guidelines);
          resolve(true);
        });
      this.subscriptions.push(subs);
    });
  }

  download() {
    this.ackDataArr.forEach(ackDataItem => {
      const preRegId = ackDataItem["preRegId"];
      this.pdfOptions = {
        margin: [15,15],
        filename: preRegId + ".pdf",
        image: { type: "jpeg", quality: 0.98 },
        html2canvas: { scale: 1, letterRendering: true },
        jsPDF: { unit: 'pt', format: 'letter', orientation: 'portrait' },
        pagebreak: { mode: ['avoid-all', 'css', 'legacy'] }
      };
      
      const element = document.getElementById("pdf-section" + "-" + preRegId);
      window.scroll(0, 0);
      html2pdf(element, this.pdfOptions);
    });
  }

  // async generateBlob() {
  //   const element = document.getElementById("print-section");
  //   return await html2pdf()
  //     .set(this.pdfOptions)
  //     .from(element)
  //     .outputPdf("dataurlstring");
  // }

  // async createBlob() {
  //   const dataUrl = await this.generateBlob();
  //   // convert base64 to raw binary data held in a string
  //   const byteString = atob(dataUrl.split(",")[1]);

  //   // separate out the mime component
  //   const mimeString = dataUrl.split(",")[0].split(":")[1].split(";")[0];

  //   // write the bytes of the string to an ArrayBuffer
  //   const arrayBuffer = new ArrayBuffer(byteString.length);

  //   var _ia = new Uint8Array(arrayBuffer);
  //   for (let i = 0; i < byteString.length; i++) {
  //     _ia[i] = byteString.charCodeAt(i);
  //   }

  //   const dataView = new DataView(arrayBuffer);
  //   return await new Blob([dataView], { type: mimeString });
  // }

  sendAcknowledgement() {
    const data = {
      case: "SEND_ACKNOWLEDGEMENT",
      notificationTypes: this.notificationTypes,
    };
    const subs = this.dialog
      .open(DialougComponent, {
        width: "350px",
        data: data
      })
      .afterClosed()
      .subscribe((applicantNumber) => {
        //console.log(applicantNumber);
        if (applicantNumber !== undefined) {
          this.preRegIds.forEach(preRegId => {
            this.applicantContactDetails.push({
              "preRegId": preRegId,
              "phone": applicantNumber[1],
              "email": applicantNumber[0]
            });
          });
          this.sendNotification(this.applicantContactDetails, true);
        }
      });
    this.subscriptions.push(subs);
  }

  async generateQRCode(name) {
    try {
      const index = this.usersInfoArr.indexOf(name);
      if (!this.usersInfoArr[index].qrCodeBlob) {
        return new Promise((resolve, reject) => {
          const subs = this.dataStorageService
            .generateQRCode(name.preRegId)
            .subscribe((response) => {
              console.log(response["response"]);
              this.usersInfoArr[index].qrCodeBlob = response["response"].qrcode;
              resolve(true);
            });
        });
      }
    } catch (ex) {
      console.log("this.usersInfo[index].qrCodeBlob >>> " + ex.messages);
    }
  }

  async sendNotification(contactInfoArr, additionalRecipient: boolean) {
    //this.fileBlob = await this.createBlob();
    this.preRegIds.forEach(async preRegId => {
      let notificationObject = {};
      this.usersInfoArr.forEach(async (user) => {
        if (preRegId == user.preRegId) {
          let contactInfo = {};
          contactInfoArr.forEach(item => {
            if (item["preRegId"] == preRegId) {
              contactInfo = item;
            }
          });
          notificationObject[user.langCode] = new NotificationDtoModel(
            user.fullName != "" ?user.fullName:user.preRegId,
            user.preRegId,
            user.bookingData
              ? user.bookingData.split(",")[0]
              : user.regDto.appointment_date,
            Number(user.bookingTimePrimary.split(":")[0]) < 10
              ? "0" + user.bookingTimePrimary
              : user.bookingTimePrimary,
              contactInfo["phone"] === undefined ? null : contactInfo["phone"],
              contactInfo["email"] === undefined ? null : contactInfo["email"],
            additionalRecipient,
            false
          );
        }
      });
      const model = new RequestModel(
        appConstants.IDS.notification,
        notificationObject
      );
      let notificationRequest = new FormData();
      notificationRequest.append(
        appConstants.notificationDtoKeys.notificationDto,
        JSON.stringify(model).trim()
      );
      notificationRequest.append(
        appConstants.notificationDtoKeys.langCode,
        Object.keys(notificationObject).join(",")
      );
      // notificationRequest.append(
      //   appConstants.notificationDtoKeys.file,
      //   this.fileBlob,
      //   `${preRegId}.pdf`
      // );
      await this.sendNotificationForPreRegId(notificationRequest);
    }); 
  }

  private sendNotificationForPreRegId(notificationRequest) {
    return new Promise((resolve, reject) => {
      this.subscriptions.push(
        this.dataStorageService
        .sendNotification(notificationRequest)
        .subscribe((response) => {
          resolve(true);
        },
        (error) => {
          resolve(true);
          this.showErrorMessage(error);
        })
      );
    });
  }

  /**
   * @description This is a dialoug box whenever an error comes from the server, it will appear.
   *
   * @private
   * @memberof AcknowledgementComponent
   */
   private showErrorMessage(error: any) {
    const titleOnError = this.errorlabels.errorLabel;
    const message = Utils.createErrorMessage(error, this.errorlabels, this.apiErrorCodes, this.configService); 
    const body = {
      case: "ERROR",
      title: titleOnError,
      message: message,
      yesButtonText: this.errorlabels.button_ok,
    };
    this.dialog.open(DialougComponent, {
      width: "400px",
      data: body,
    });
  }

  ngOnDestroy() {
    this.subscriptions.forEach((subscription) => subscription.unsubscribe());
  }
}
