import {
  Component,
  OnInit,
  ViewChild,
  ElementRef,
  OnDestroy,
} from "@angular/core";
import { Subscription } from "rxjs";
import { MatDialog, MatSnackBar } from "@angular/material";
import { DialougComponent } from "../../../shared/dialoug/dialoug.component";
import { Router, ActivatedRoute } from "@angular/router";
import smoothscroll from "smoothscroll-polyfill";

import { BookingModel } from "../center-selection/booking.model";
import { NameList } from "src/app/shared/models/demographic-model/name-list.modal";
import { RequestModel } from "src/app/shared/models/request-model/RequestModel";
import { BookingService } from "../booking.service";
import { TranslateService } from "@ngx-translate/core";
import { DataStorageService } from "src/app/core/services/data-storage.service";
import { ConfigService } from "src/app/core/services/config.service";
import { BookingDeactivateGuardService } from "src/app/shared/can-deactivate-guard/booking-guard/booking-deactivate-guard.service";

import Utils from "src/app/app.util";
import * as appConstants from "../../../app.constants";
import { UserModel } from "src/app/shared/models/demographic-model/user.modal";

@Component({
  selector: "app-time-selection",
  templateUrl: "./time-selection.component.html",
  styleUrls: ["./time-selection.component.css"],
})
export class TimeSelectionComponent
  extends BookingDeactivateGuardService
  implements OnInit, OnDestroy
{
  @ViewChild("widgetsContent", { read: ElementRef }) public widgetsContent;
  @ViewChild("cardsContent", { read: ElementRef }) public cardsContent;
  textDir = localStorage.getItem("dir");
  registrationCenter: string;
  selectedCard: number;
  selectedTile = 0;
  showNote = false;
  limit = [];
  showAddButton = false;
  names: NameList[] = [];
  deletedNames = [];
  selectedNames = [];
  availabilityData = [];
  days: number;
  disableAddButton = true;
  activeTab = "morning";
  bookingDataList = [];
  temp: NameList[] = [];
  registrationCenterLunchTime = [];
  languagelabels: any;
  apiErrorCodes: any;
  errorlabels: any;
  showMorning: boolean;
  showAfternoon: boolean;
  disableContinueButton = false;
  spinner = true;
  canDeactivateFlag = true;
  DAYS: any;
  userPreferredLangCode = localStorage.getItem("userPrefLanguage");
  subscriptions: Subscription[] = [];
  preRegId: any = [];
  userInfo: any = [];
  regCenterInfo: any;
  showsNamesContainer: boolean;
  afternoonSlotAvailable: boolean = false;
  morningSlotAvailable: boolean = false;
  name = "";
  applicationStatus = "";
  constructor(
    private bookingService: BookingService,
    public dialog: MatDialog,
    private dataService: DataStorageService,
    private router: Router,
    private translate: TranslateService,
    private configService: ConfigService,
    private activatedRoute: ActivatedRoute,
    private _snackBar: MatSnackBar
  ) {
    super(dialog);
    smoothscroll.polyfill();
    this.translate.use(this.userPreferredLangCode);
  }

  ngOnInit(): void {
    void this.initAsync();
  }

  private async initAsync(): Promise<void> {
    if (this.router.url.includes("multiappointment")) {
      this.preRegId = [...JSON.parse(localStorage.getItem("multiappointment"))];
    } else {
      this.activatedRoute.params.subscribe((param) => {
        this.preRegId = [param["appId"]];
      });
    }
    this.activatedRoute.queryParams.subscribe((param) => {
      this.registrationCenter = param["regCenter"];
    });
    this.name = this.configService.getConfigByKey(
      appConstants.CONFIG_KEYS.preregistration_identity_name
    );
    this.dataService
      .getI18NLanguageFiles(this.userPreferredLangCode)
      .subscribe((response) => {
        this.languagelabels = response["timeSelection"].booking;
        this.errorlabels = response["error"];
        this.apiErrorCodes = response[appConstants.API_ERROR_CODES];
        this.DAYS = response["DAYS"];
      });
    await this.getUserInfo(this.preRegId);
    await this.getRegCenterDetails();
    this.prepareNameList(this.userInfo, this.regCenterInfo);
    this.days = this.configService.getConfigByKey(
      appConstants.CONFIG_KEYS.preregistration_availability_noOfDays
    );
    if (this.temp[0]) {
      this.registrationCenterLunchTime =
        this.temp[0].registrationCenter.lunchEndTime.split(":");
    }
    this.getSlotsforCenter(this.registrationCenter);
  }

  async getUserInfo(preRegId) {
    for (const id of preRegId) {
      try {
        const user = await this.getUserDetails(id);
        this.userInfo.push(user);
      } catch (error) {
        console.error('Failed to fetch details for preRegId: %s', id, error);
      }
    }
  }

  getUserDetails(prid) {
    return new Promise((resolve) => {
      this.dataService.getUser(prid.toString()).subscribe((response) => {
        resolve(
          new UserModel(
            prid.toString(),
            response[appConstants.RESPONSE],
            undefined,
            []
          )
        );
      },
      (err) => {
        this.dataService.getApplicationDetails(prid.toString()).subscribe((response) => {
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

  getRegCenterDetails() {
    return new Promise((resolve) => {
      this.dataService
        .getRegistrationCentersById(
          this.registrationCenter,
          this.userPreferredLangCode
        )
        .subscribe((response) => {
          if (response[appConstants.RESPONSE]) {
            console.log(response[appConstants.RESPONSE]);
            this.regCenterInfo =
              response[appConstants.RESPONSE].registrationCenters[0];
            resolve(true);
          }
        },
        (error) => {
          this.showErrorMessage(error);
        });
    });
  }

  private prepareNameList(userInfo, regCenterInfo) {
    console.log(this.userInfo.length);
    userInfo.forEach((user) => {
      console.log(user);
      const nameList: NameList = {
        preRegId: "",
        fullName: "",
        regDto: "",
        status: "",
        registrationCenter: "",
        bookingData: "",
        postalCode: "",
      };
      let demographicData = {};
      let applicationLanguages = [];
      if (user["request"] && user["request"].demographicDetails) {
        demographicData = user["request"].demographicDetails.identity;
        applicationLanguages = Utils.getApplicationLangs(user["request"]);
      }
      
      let filteredLangs = applicationLanguages.filter(applicationLang => 
        applicationLang == this.userPreferredLangCode
      );
      if (filteredLangs.length > 0) {
        let nameValues = demographicData[this.name];
        nameValues.forEach(nameVal => {
          if (nameVal["language"] == this.userPreferredLangCode) {
            nameList.fullName = nameVal["value"];
          }
        });  
      } else {
        if (demographicData[this.name] && demographicData[this.name].length > 0) {
          nameList.fullName = demographicData[this.name][0].value;
        } else {
          nameList.fullName = user.request.applicationId;
        }
      }
      if (user.request.preRegistrationId) {
        nameList.preRegId = user.request.preRegistrationId;
      } else {
        nameList.preRegId = user.request.applicationId;
      }
      nameList.status = user.request.statusCode;
      nameList.postalCode = demographicData["postalCode"];
      nameList.registrationCenter = regCenterInfo;
      console.log(`user.request.statusCode: ${user.request.statusCode}`);
      if (user.request.statusCode === appConstants.APPLICATION_STATUS_CODES.pending) {
        this.showNote = true;
      }  
      this.names.push(nameList);
      this.temp.push(nameList);
    });
  }

  public scrollRight(): void {
    console.log(this.widgetsContent.nativeElement.scrollWidth);
    // for edge browser
    this.widgetsContent.nativeElement.scrollBy({
      left: this.widgetsContent.nativeElement.scrollLeft + 750,
      behavior: "smooth",
    });
    // for chrome browser
    this.widgetsContent.nativeElement.scrollTo({
      left: this.widgetsContent.nativeElement.scrollLeft + 750,
      behavior: "smooth",
    });
  }

  public scrollLeft(): void {
    // for edge browser
    this.widgetsContent.nativeElement.scrollBy({
      left: this.widgetsContent.nativeElement.scrollLeft - 750,
      behavior: "smooth",
    });
    //for chrome browser
    this.widgetsContent.nativeElement.scrollTo({
      left: this.widgetsContent.nativeElement.scrollLeft - 750,
      behavior: "smooth",
    });
  }

  dateSelected(index: number) {
    this.selectedTile = index;
  }

  cardSelected(index: number): void {
    this.selectedCard = index;
    this.canAddApplicant(
      this.availabilityData[this.selectedTile].timeSlots[this.selectedCard]
    );
  }

  itemDelete(index: number): void {
    this.deletedNames.push(
      this.availabilityData[this.selectedTile].timeSlots[this.selectedCard]
        .names[index]
    );
    this.availabilityData[this.selectedTile].timeSlots[
      this.selectedCard
    ].names.splice(index, 1);
    this.selectedNames.splice(index, 1);
    this.canAddApplicant(
      this.availabilityData[this.selectedTile].timeSlots[this.selectedCard]
    );
  }

  addItem(index: number): void {
    if (
      this.canAddApplicant(
        this.availabilityData[this.selectedTile].timeSlots[this.selectedCard]
      )
    ) {
      this.availabilityData[this.selectedTile].timeSlots[
        this.selectedCard
      ].names.push(this.deletedNames[index]);
      let selectedObj = {
        name: this.deletedNames[index].fullName,
        bookingData: {
          displayTime:
            this.availabilityData[this.selectedTile].timeSlots[
              this.selectedCard
            ].displayTime,
          tag: this.availabilityData[this.selectedTile].timeSlots[
            this.selectedCard
          ].tag,
          tagLabel: this.availabilityData[this.selectedTile].timeSlots[
            this.selectedCard
          ].tagLabel,
          date: this.availabilityData[this.selectedTile].displayDate,
        },
      };
      this.selectedNames.push(selectedObj);
      this.deletedNames.splice(index, 1);
    }
  }

  canAddApplicant(slot: any): boolean {
    if (slot && slot.availability > slot.names.length) {
      this.disableAddButton = false;
      return true;
    } else {
      this.disableAddButton = true;
      return false;
    }
  }

  formatJson(centerDetails: any) {
    centerDetails.forEach((element) => {
      let sumAvailability = 0;
      let morningLabelText = "", afternoonLabelText = "";
      this.translate.get('timeSelection.text_morning').subscribe((label: string) => {
        morningLabelText = label;
      });
      this.translate.get('timeSelection.text_afternoon').subscribe((label: string) => {
        afternoonLabelText = label;
      });  
      element.timeSlots.forEach((slot) => {
        sumAvailability += slot.availability;
        slot.names = [];
        let fromTime = slot.fromTime.split(":");
        let toTime = slot.toTime.split(":");
        if (this.registrationCenterLunchTime[0] === null) {
          slot.tag = "morning";
          slot.tagLabel = morningLabelText; 
          element.showMorning = true;
          this.morningSlotAvailable = true;
          this.afternoonSlotAvailable = false;
        } else if (
          this.registrationCenterLunchTime[0] !== null &&
          this.registrationCenterLunchTime[0].split(":")[0] === "00"
        ) {
          slot.tag = "morning";
          slot.tagLabel = morningLabelText;
          element.showMorning = true;
          this.morningSlotAvailable = true;
          this.afternoonSlotAvailable = false;
        } else if (fromTime[0] < this.registrationCenterLunchTime[0]) {
          slot.tag = "morning";
          slot.tagLabel = morningLabelText;
          element.showMorning = true;
          this.morningSlotAvailable = true;
        } else {
          slot.tag = "afternoon";
          slot.tagLabel = afternoonLabelText;
          element.showAfternoon = true;
          this.afternoonSlotAvailable = true;
        }
        slot.displayTime =
          Number(fromTime[0]) > 12 ? Number(fromTime[0]) - 12 : fromTime[0];
        slot.displayTime += ":" + fromTime[1] + " - ";
        slot.displayTime +=
          Number(toTime[0]) > 12 ? Number(toTime[0]) - 12 : toTime[0];
        slot.displayTime += ":" + toTime[1];
      });
      element.TotalAvailable = sumAvailability;
      element.inActive = false;
      const ltrLangs = this.configService
      .getConfigByKey(appConstants.CONFIG_KEYS.mosip_left_to_right_orientation)
      .split(",");
      element.displayDate = Utils.getBookingDateTime(
        element.date,
        "",
        this.userPreferredLangCode,
        ltrLangs
      );
      let index = new Date(Date.parse(element.date)).getDay();
      element.displayDay = this.DAYS[index];
      if (!element.inActive) {
        this.availabilityData.push(element);
      }
    });
    this.enableBucketTabs();
    this.deletedNames = [...this.names];
    // this.placeNamesInSlots();
  }

  placeNamesInSlots() {
    this.availabilityData[this.selectedTile].timeSlots.forEach((slot) => {
      if (this.names.length !== 0) {
        while (
          slot.names.length < slot.availability &&
          this.names.length !== 0
        ) {
          slot.names.push(this.names[0]);
          this.names.splice(0, 1);
        }
      }
    });
    this.enableBucketTabs();
  }

  enableBucketTabs() {
    const element = this.availabilityData[this.selectedTile];
    if (element.showMorning && element.showAfternoon) {
      this.tabSelected("morning");
    } else if (element.showMorning) {
      this.tabSelected("morning");
    } else {
      this.tabSelected("afternoon");
    }
  }

  getSlotsforCenter(id) {
    const subs = this.dataService.getAvailabilityData(id).subscribe(
      (response) => {
        this.spinner = false;
        //console.log(response[appConstants.RESPONSE]);
        if (response[appConstants.RESPONSE]) {
          //console.log(response[appConstants.RESPONSE]);
          if (response[appConstants.RESPONSE].centerDetails.length > 0) {
            this.formatJson(response[appConstants.RESPONSE].centerDetails);
          } else {
            this.showErrorMessage(null, this.errorlabels.centerDetailsNotAvailable);
          }
        }
      },
      (error) => {
        //console.log(error);
        //this.showErrorMessage(error);
        this.showErrorMessage(null, this.errorlabels.centerDetailsNotAvailable);
      }
    );
    this.subscriptions.push(subs);
  }

  tabSelected(selection: string) {
    if (
      (selection === "morning" &&
        this.availabilityData[this.selectedTile].showMorning) ||
      (selection === "afternoon" &&
        this.availabilityData[this.selectedTile].showAfternoon)
    ) {
      this.activeTab = selection;
      console.log(this.availabilityData[this.selectedTile]);
      console.log(
        this.availabilityData[this.selectedTile].timeSlots.filter(
          (day) => day.tag === this.activeTab
        ).length
      );
      this.availabilityData[this.selectedTile].timeSlots.filter(
        (day) => day.tag === this.activeTab
      ).length > 0
        ? (this.showsNamesContainer = true)
        : (this.showsNamesContainer = false);
      console.log(this.showsNamesContainer);
    }
  }

  getNames(): string {
    const x = [];

    this.deletedNames.forEach((name) => {
      x.push(name.fullName);
    });

    return x.join(" \n - ");
  }

  async makeBooking() {
    this.canDeactivateFlag = false;
    this.disableContinueButton = true;
    this.bookingDataList = [];
    this.availabilityData.forEach((data) => {
      data.timeSlots.forEach((slot) => {
        if (slot.names.length !== 0) {
          slot.names.forEach((name) => {
            const bookingData = new BookingModel(
              name.preRegId,
              this.registrationCenter.toString(),
              data.date,
              slot.fromTime,
              slot.toTime
            );
            //console.log(bookingData);
            this.bookingDataList.push(bookingData);
          });
        }
      });
    });
    if (this.bookingDataList.length === 0 && this.selectedCard > 0 ) {
      this.disableContinueButton = false;
      this.showErrorMessage(
        null,
        this.languagelabels.noApplicantSelected
      );
      return;
    }
    else if (this.bookingDataList.length === 0 ) {
      this.disableContinueButton = false;
      this.showErrorMessage(
        null,
        this.languagelabels.noSlotsSelectedForApplicant
      );
      return;
    }
    if (
      !this.router.url.includes("multiappointment") &&
      this.userInfo[0].request.statusCode === appConstants.APPLICATION_STATUS_CODES.pending
    ) {
      const data = {
        case: "CONFIRMATION",
        title: this.languagelabels.applicationLockConfirm.title,
        message: this.languagelabels.applicationLockConfirm.message,
        noButtonText: this.languagelabels.applicationLockConfirm.noButtonText,
        yesButtonText: this.languagelabels.applicationLockConfirm.yesButtonText,
      };
      this.dialog
        .open(DialougComponent, {
          width: "400px",
          //height: "250px",
          data: data,
        })
        .afterClosed()
        .subscribe((response) => {
          //console.log(response);
          if (response === true) {
            this.bookingOperationRequest();
          } else {
            this.disableContinueButton = false;
          }
        });
    } else if (this.router.url.includes("multiappointment")) {
      let pridWithNoBookings = [];
      this.userInfo.forEach((user) => {
        if (user.request.statusCode === appConstants.APPLICATION_STATUS_CODES.pending) {
          pridWithNoBookings.push(user.request.preRegistrationId);
        }
      });
      if (pridWithNoBookings.length !== 0) {
        const data = {
          case: "CONFIRMATION",
          title: this.languagelabels.applicationLockConfirm.title,
          message: this.languagelabels.applicationLockConfirm.message,
          noButtonText: this.languagelabels.applicationLockConfirm.noButtonText,
          yesButtonText:
            this.languagelabels.applicationLockConfirm.yesButtonText,
        };
        this.dialog
          .open(DialougComponent, {
            width: "450px",
            //height: "250px",
            data: data,
          })
          .afterClosed()
          .subscribe((response) => {
            if (response === true) {
              this.bookingOperationRequest();
            } else {
              this.disableContinueButton = false;
            }
          });
      } else {
        this.bookingOperationRequest();
      }
    } else {
      this.bookingOperationRequest();
    }
  }

  bookingOperationRequest() {
    const obj = {
      bookingRequest: this.bookingDataList,
    };
    const request = new RequestModel(appConstants.IDS.booking, obj);
    if (this.deletedNames.length !== 0) {
      const data = {
        case: "CONFIRMATION",
        message:
          this.languagelabels.deletedApplicant1[0] +
          ' - ' +
          this.getNames() +
          ' \n ' +
          this.languagelabels.deletedApplicant1[1] +
          "?",
        yesButtonText: this.languagelabels.yesButtonText,
        noButtonText: this.languagelabels.noButtonText,
      };
      const dialogRef = this.dialog.open(DialougComponent, {
        width: "400px",
        //height: "250px",
        data: data,
        disableClose: true,
      });
      const subs = dialogRef.afterClosed().subscribe((selectedOption) => {
        if (selectedOption === true ) {
          this.bookingOperation(request);
        } else {
          this.disableContinueButton = false;
          return;
        }
      });
      this.subscriptions.push(subs);
    } else {
      this.bookingOperation(request);
    }
  }

  bookingOperation(request) {
    const subs = this.dataService.makeBooking(request).subscribe(
      (response) => {
        if (response[appConstants.RESPONSE]) {
          const data = {
            case: "MESSAGE",
            title: this.languagelabels.title_success,
            message: this.languagelabels.msg_success,
          };
          this.dialog
            .open(DialougComponent, {
              width: "400px",
              data: data,
            })
            .afterClosed()
            .subscribe(() => {
              this.temp.forEach((name) => {});
              this.bookingService.setSendNotification(true);
              const url = Utils.getURL(this.router.url, "summary", 3);
              if (this.router.url.includes("multiappointment")) {
                this.router.navigateByUrl(
                  url + `/multiappointment/acknowledgement`
                );
              } else {
                this.router.navigateByUrl(
                  url + `/${this.preRegId[0]}/acknowledgement`
                );
              }
            });
        } 
      },
      (error) => {
        if (Utils.getErrorCode(error) === appConstants.ERROR_CODES.timeExpired) {
          let timespan = Utils.getErrorMessage(error).match(/\d+/g);
          let errorMessage =
            this.errorlabels.timeExpired_1 +
            timespan[0] +
            this.errorlabels.timeExpired_2;
          this.showErrorMessage(error, errorMessage);
        } else {
          this.showErrorMessage(error);
        }
      }
    );
    this.subscriptions.push(subs);
  }

  /**
   * @description This is a dialoug box whenever an error comes from the server, it will appear.
   *
   * @private
   * @memberof TimeSelectionComponent
   */
   private showErrorMessage(error: any, customMsg?: string) {
    this.spinner = false;
    this.disableContinueButton = false;
    const titleOnError = this.errorlabels.errorLabel;
    let errorCode = "";
    let message = "";
    if (customMsg) {
      message = customMsg
    } else {
      errorCode = Utils.getErrorCode(error);
      if (errorCode == appConstants.ERROR_CODES.slotNotAvailable) {
        message = this.errorlabels.slotNotAvailable;
      }
      else {
        message = Utils.createErrorMessage(error, this.errorlabels, this.apiErrorCodes, this.configService); 
      }
    }
    const messageObj = {
      case: "ERROR",
      title: titleOnError,
      message: message,
      yesButtonText: this.errorlabels.button_ok,
    };
    const dialogRef = this.openDialog(messageObj, "400px");
    const subs = dialogRef.afterClosed().subscribe(() => {
      if (errorCode === appConstants.ERROR_CODES.slotNotAvailable) {
        this.canDeactivateFlag = false;
        if (this.router.url.includes("multiappointment")) {
          this.router.navigateByUrl(
            `${this.userPreferredLangCode}/pre-registration/booking/multiappointment/pick-center`
          );
        } else {
          this.router.navigateByUrl(
            `${this.userPreferredLangCode}/pre-registration/booking/${this.preRegId[0]}/pick-center`
          );
        }
      }
      if (this.errorlabels.centerDetailsNotAvailable === messageObj.message) {
        this.canDeactivateFlag = false;
        if (this.router.url.includes("multiappointment")) {
          this.router.navigateByUrl(
            `${this.userPreferredLangCode}/pre-registration/booking/multiappointment/pick-center`
          );
        } else {
          this.router.navigateByUrl(
            `${this.userPreferredLangCode}/pre-registration/booking/${this.preRegId[0]}/pick-center`
          );
        }
      }
    });
    this.subscriptions.push(subs);
  }

  // displayMessage(title: string, message: string, error: any) {
  //   this.spinner = false;
  //   this.disableContinueButton = false;
  //   if (
  //     error &&
  //     error[appConstants.ERROR] &&
  //     error[appConstants.ERROR][appConstants.NESTED_ERROR] &&
  //     error[appConstants.ERROR][appConstants.NESTED_ERROR][0].errorCode ===
  //       appConstants.ERROR_CODES.tokenExpired
  //   ) {
  //     message = this.errorlabels.tokenExpiredLogout;
  //     title = "";
  //   } else if (
  //     error &&
  //     error[appConstants.ERROR] &&
  //     error[appConstants.ERROR][appConstants.NESTED_ERROR] &&
  //     error[appConstants.ERROR][appConstants.NESTED_ERROR][0].errorCode ===
  //       appConstants.ERROR_CODES.slotNotAvailable
  //   ) {
  //     message = this.errorlabels.slotNotAvailable;
  //   }
  //   const messageObj = {
  //     case: "MESSAGE",
  //     title: title,
  //     message: message,
  //   };
  //   const dialogRef = this.openDialog(messageObj, "400px");
  //   const subs = dialogRef.afterClosed().subscribe(() => {
  //     if (
  //       error &&
  //       error[appConstants.ERROR] &&
  //       error[appConstants.ERROR][appConstants.NESTED_ERROR] &&
  //       error[appConstants.ERROR][appConstants.NESTED_ERROR][0].errorCode ===
  //         appConstants.ERROR_CODES.slotNotAvailable
  //     ) {
  //       this.canDeactivateFlag = false;
  //       if (this.router.url.includes("multiappointment")) {
  //         this.router.navigateByUrl(
  //           `${this.userPreferredLangCode}/pre-registration/booking/multiappointment/pick-center`
  //         );
  //       } else {
  //         this.router.navigateByUrl(
  //           `${this.userPreferredLangCode}/pre-registration/booking/${this.preRegId[0]}/pick-center`
  //         );
  //       }
  //     }
  //     if (this.errorlabels.centerDetailsNotAvailable === messageObj.message) {
  //       this.canDeactivateFlag = false;
  //       if (this.router.url.includes("multiappointment")) {
  //         this.router.navigateByUrl(
  //           `${this.userPreferredLangCode}/pre-registration/booking/multiappointment/pick-center`
  //         );
  //       } else {
  //         this.router.navigateByUrl(
  //           `${this.userPreferredLangCode}/pre-registration/booking/${this.preRegId[0]}/pick-center`
  //         );
  //       }
  //     }
  //   });
  //   this.subscriptions.push(subs);
  // }

  openDialog(data, width) {
    const dialogRef = this.dialog.open(DialougComponent, {
      width: width,
      data: data,
    });
    return dialogRef;
  }

  navigateDashboard() {
    this.canDeactivateFlag = false;
    this.router.navigate([`${this.userPreferredLangCode}/dashboard`]);
  }

  navigateBack() {
    this.canDeactivateFlag = false;
    const url = Utils.getURL(this.router.url, "pick-center", 1);
    this.router.navigateByUrl(url);
  }

  ngOnDestroy() {
    this.subscriptions.forEach((subscription) => subscription.unsubscribe());
  }
}
