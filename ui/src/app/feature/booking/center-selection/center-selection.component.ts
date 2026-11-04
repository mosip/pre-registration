import { Component, OnInit, OnDestroy } from "@angular/core";
import { MatDialog } from "@angular/material";
import { DialougComponent } from "../../../shared/dialoug/dialoug.component";
import { DataStorageService } from "src/app/core/services/data-storage.service";
import { RegistrationCentre } from "./registration-center-details.model";
import { Router, ActivatedRoute } from "@angular/router";

import { UserModel } from "src/app/shared/models/demographic-model/user.modal";
import { BookingService } from "../booking.service";
import { TranslateService } from "@ngx-translate/core";
import Utils from "src/app/app.util";
import { ConfigService } from "src/app/core/services/config.service";
import * as appConstants from "./../../../app.constants";
import { BookingDeactivateGuardService } from "src/app/shared/can-deactivate-guard/booking-guard/booking-deactivate-guard.service";
import { Subscription } from "rxjs";

@Component({
  selector: "app-center-selection",
  templateUrl: "./center-selection.component.html",
  styleUrls: ["./center-selection.component.css"],
})
export class CenterSelectionComponent
  extends BookingDeactivateGuardService
  implements OnInit, OnDestroy {
  REGISTRATION_CENTRES: RegistrationCentre[] = [];
  searchClick: boolean = true;
  pageLoaded = false;
  canDeactivateFlag = true;
  locationTypes = [];
  identityData = [];
  allLocationTypes = [];
  locationType = null;
  searchText = null;
  showTable = false;
  selectedCentre = null;
  showMap = false;
  showMessage = false;
  enableNextButton = false;
  bookingDataList = [];
  errorlabels: any;
  nearbyClicked = false;
  apiErrorCodes: any;
  step = 0;
  textDir = localStorage.getItem("dir");
  showDescription = false;
  mapProvider = "OSM";
  searchTextFlag = false;
  displayMessage = "Showing nearby registration centers";
  users: UserModel[] = [];
  subscriptions: Subscription[] = [];
  userPreferredLangCode = localStorage.getItem("userPrefLanguage");
  workingDays: string;
  preRegId = [];
  recommendedCenterLocCode = 1;
  locationNames = [];
  locationCodes = [];
  // MatPaginator Inputs
  totalItems = 0;
  defaultPageSize = 10;
  pageSize = this.defaultPageSize;
  pageIndex = 0;
  pageSizeOptions: number[] = [5, 10, 15, 20];
  positions:any
  constructor(
    public dialog: MatDialog,
    private service: BookingService,
    private dataService: DataStorageService,
    private router: Router,
    private route: ActivatedRoute,
    private translate: TranslateService,
    private configService: ConfigService,
    private activatedRoute: ActivatedRoute
  ) {
    super(dialog);
    this.translate.use(this.userPreferredLangCode);
  }

  ngOnInit(): void {
    if (this.router.url.includes('multiappointment')) {
      const stored = localStorage.getItem('multiappointment');
      if (stored) {
        try {
          this.preRegId = [...JSON.parse(stored)];
        } catch (error) {
          console.error('Failed to parse multiappointment data from localStorage:', error);
          this.preRegId = [];
        }
      } else {
        this.preRegId = [];
      }
      void this.initAsync(); // start only after preRegId is known
    } else {
      this.activatedRoute.params.subscribe((param) => {
        this.preRegId = [param['appId']];
        void this.initAsync(); // start only after param is known
      });
    }
  }

  private async initAsync(): Promise<void> {
    this.getErrorLabels();
    await this.getUserInfo(this.preRegId);
    this.REGISTRATION_CENTRES = [];
    this.selectedCentre = null;
    const configValue = this.configService.getConfigByKey(
      appConstants.CONFIG_KEYS.preregistration_recommended_centers_locCode
    );
    this.recommendedCenterLocCode = Number(configValue);
    if (Number.isNaN(this.recommendedCenterLocCode)) {
      console.error('Invalid recommendedCenterLocCode config value:', configValue);
      this.recommendedCenterLocCode = 1; // fallback to default
    }
    console.log(`recommendedCenterLocCode: ${this.recommendedCenterLocCode}`);
    await this.getIdentityJsonFormat();
    const subs = this.dataService.getLocationTypeData().subscribe((response) => {
      //get all location types from db
      this.allLocationTypes = response[appConstants.RESPONSE]['locationHierarchyLevels'];
      console.log(this.allLocationTypes);
      //get the recommended loc hierachy code to which booking centers are mapped
      //now filter out only those hierachies which are higher than the recommended loc hierachy code
      //ex: if locHierachy is ["Country","Region","Province","City","PostalCode"] and the
      //recommended loc hierachy code is 3 for "City", then show only "Country","Region","Province"
      //in the Search dropdown. There are no booking centers mapped to "PostalCode", so don't include it.
      this.locationTypes = this.allLocationTypes.filter(
        (locType) => locType.hierarchyLevel <= this.recommendedCenterLocCode
      );
      // sort the filtered array in ascending order of hierarchyLevel
      this.locationTypes.sort((a, b) => a.hierarchyLevel - b.hierarchyLevel);
      this.getRecommendedCenters();
    });
    this.subscriptions.push(subs);
  }

  async getUserInfo(preRegId) {
    for (const id of preRegId) {
      try {
        const user = await this.getUserDetails(id);
        this.users.push(user);
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

  getErrorLabels() {
    this.dataService.getI18NLanguageFiles(localStorage.getItem('userPrefLanguage')).subscribe((response) => {
      this.errorlabels = response["error"];
      this.apiErrorCodes = response[appConstants.API_ERROR_CODES];
    });
  }
/**
   * @description This method will get the Identity Schema Json
   */
 async getIdentityJsonFormat() {
  return new Promise((resolve, reject) => {
    this.dataService.getIdentityJson().subscribe(
      async (response) => {
        let identityJsonSpec =
          response[appConstants.RESPONSE]["jsonSpec"]["identity"];
        this.identityData = identityJsonSpec["identity"];
        resolve(true);
      },
      (error) => {
        this.showErrorMessage(error);
      }
    );
  });
}

  async getRecommendedCenters() {
    this.totalItems = 0;
    this.nearbyClicked = false;
    let uiFieldName = null;
    this.identityData.forEach((obj) => {
      if (
        obj.inputRequired === true &&
        obj.controlType !== null &&
        (obj.controlType !== "fileupload")
      ) {
        if (obj.locationHierarchyLevel && this.recommendedCenterLocCode == obj.locationHierarchyLevel) {
          uiFieldName = obj.id;
        }
      }
    });
    if (!uiFieldName) {
      this.showErrorMessage(null, this.errorlabels.error);
    } else {
      console.log(`uiFieldName: ${uiFieldName}`);
      this.users.forEach((user) => {
        console.log(user);
        if (user.request && user.request.demographicDetails && user.request.demographicDetails.identity) {
          if (
            typeof user.request.demographicDetails.identity[uiFieldName] ===
            "object"
          ) {
            this.locationCodes.push(
              user.request.demographicDetails.identity[uiFieldName][0].value
            );
          } else if (
            typeof user.request.demographicDetails.identity[uiFieldName] ===
            "string"
          ) {
            this.locationCodes.push(
              user.request.demographicDetails.identity[uiFieldName]
            );
          }
        }
      });
      console.log(this.locationCodes);
      await this.getLocationNamesByCodes();
      this.getRecommendedCentersApiCall();
    }
  }

  getLocationNamesByCodes() {
    return new Promise((resolve) => {
      if (this.locationCodes.length == 0) {
        resolve(true);
      }
      this.locationCodes.forEach(async (pins,index) => {
        await this.getLocationNames(pins);
        if(index===this.locationCodes.length-1){
          resolve(true);
        }
      });
    });
  }

  getRecommendedCentersApiCall() {
    this.REGISTRATION_CENTRES = [];
    const subs = this.dataService
      .recommendedCenters(
        this.userPreferredLangCode,
        this.recommendedCenterLocCode,
        this.locationNames
      )
      .subscribe((response) => {
        if (response[appConstants.RESPONSE]) {
          this.displayResults(response["response"]);
        } 
      },
      (error) => {
        if (this.locationNames.length != 0) {
          this.showErrorMessage(error, this.errorlabels.regCenterNotavailabe);
        }
        this.pageLoaded = true;
      });
    this.subscriptions.push(subs);
  }

  getLocationNames(locationCode) {
    return new Promise((resolve) => {
      this.dataService
        .getLocationInfoForLocCode(locationCode, this.userPreferredLangCode)
        .subscribe((response) => {
          if (response[appConstants.RESPONSE]) {
            let locName = response[appConstants.RESPONSE]["name"];
            this.locationNames.push(locName);
            resolve(true);
          }
        },
        (error) => {
          this.showErrorMessage(error, this.errorlabels.regCenterNotavailabe);
        });
    });
  }

  setSearchClick(flag: boolean) {
    this.searchClick = flag;
    this.nearbyClicked = false;
  }
  onSubmit() {
    this.searchTextFlag = true;
    if (this.searchText.length !== 0 || this.searchText !== null) {
      this.displayMessage = `Searching results for ${this.searchText} ....`;
    } else {
      this.displayMessage = "";
    }
  }
  setStep(index: number) {
    this.step = index;
  }

  nextStep() {
    this.step++;
    this.showDescription = true;
  }

  prevStep() {
    this.step--;
  }
  resetPagination() {
    this.totalItems = 0;
    this.pageSize = this.defaultPageSize;
    this.pageIndex = 0;
    this.getRecommendedCenters();
  }

  showResults(pageEvent) {
    this.REGISTRATION_CENTRES = [];
    if (this.locationType !== null && this.searchText !== null) {
      this.showMap = false;
      if (pageEvent) {
        this.pageSize = pageEvent.pageSize;
        this.pageIndex = pageEvent.pageIndex;
      }
      const subs = this.dataService
        .getRegistrationCentersByNamePageWise(
          this.locationType.hierarchyLevel,
          this.searchText,
          this.pageIndex,
          this.pageSize
        )
        .subscribe(
          (response) => {
            if (response[appConstants.RESPONSE]) {
              this.totalItems = response[appConstants.RESPONSE].totalItems;
              this.displayResults(response[appConstants.RESPONSE]);
              this.showMessage = false;
            } else {
              this.totalItems = 0;
              this.showMessage = true;
              this.selectedCentre = null;
            }
          },
          (error) => {
            this.showMessage = true;
            this.totalItems = 0;
            this.selectedCentre = null;
          });
      this.subscriptions.push(subs);
    } else {
      this.showMessage = true;
      this.totalItems = 0;
      this.selectedCentre = null;
    }
  }

  onChangeLocationType() {
    this.showMessage = false;
    this.totalItems = 0;
    this.searchText = "";
    this.selectedCentre = null;
  }

  plotOnMap() {
    this.showMap = true;
    this.service.changeCoordinates([
      Number(this.selectedCentre.longitude),
      Number(this.selectedCentre.latitude),
    ]);
  }

  selectedRow(row) {
    this.selectedCentre = row;
    this.enableNextButton = true;

    if (Object.keys(this.selectedCentre).length !== 0) {
      this.plotOnMap();
    }
  }

  getLocation() {
    this.REGISTRATION_CENTRES = [];
    this.nearbyClicked = true;
    if ("geolocation" in navigator) {
      this.showMap = false;
      this.positions = navigator.geolocation;
      this.positions.getCurrentPosition((position) => {
        console.log(position.coords);
        const subs = this.dataService
          .getNearbyRegistrationCenters(position.coords)
          .subscribe(
            (response) => {
              if (
                response[appConstants.RESPONSE]["registrationCenters"].length !== 0
              ) {
                this.displayResults(response[appConstants.RESPONSE]);
              } else {
                this.showMessage = true;
                this.selectedCentre = null;
              }
            },
            (error) => {
              this.showMessage = true;
              this.selectedCentre = null;
            });
        this.subscriptions.push(subs);
      });
    }
  }

  changeTimeFormat(time: string): string | number {
    let inputTime = time.split(":");
    let formattedTime: any;
    if (Number(inputTime[0]) < 12 && Number(inputTime[0]) > 0) {
      formattedTime = inputTime[0];
      formattedTime += ":" + inputTime[1] + " am";
    } else if (Number(inputTime[0]) === 0) {
      formattedTime = Number(inputTime[0]) + 12;
      formattedTime += ":" + inputTime[1] + " am";
    } else if (Number(inputTime[0]) === 12) {
      formattedTime = inputTime[0];
      formattedTime += ":" + inputTime[1] + " pm";
    } else {
      formattedTime = Number(inputTime[0]) - 12;
      formattedTime += ":" + inputTime[1] + " pm";
    }
    return formattedTime;
  }

  showTime(startTime: string, endTime: string): string | number {
    let formattedStartTime = this.changeTimeFormat(startTime);
    let formattedEndTime = this.changeTimeFormat(endTime);
    let formattedTime = formattedStartTime + ' - ' + formattedEndTime;
    if (this.textDir == "rtl") {
      formattedTime = formattedEndTime + ' - ' + formattedStartTime;
    }
    return formattedTime;
  }

  dispatchCenterCoordinatesList() {
    const coords = [];
    this.REGISTRATION_CENTRES.forEach((centre) => {
      const data = {
        id: centre.id,
        latitude: Number(centre.latitude),
        longitude: Number(centre.longitude),
      };
      coords.push(data);
    });
    this.service.listOfCenters(coords);
  }

  routeNext() {
    this.canDeactivateFlag = false;
    this.router.navigate(["../pick-time"], {
      relativeTo: this.route,
      queryParams: { regCenter: this.selectedCentre.id },
    });
  }

  routeDashboard() {
    this.canDeactivateFlag = false;
    this.router.navigate([`${this.userPreferredLangCode}/dashboard`]);
  }

  routeBack() {
    if (
      this.router.url.includes("multiappointment") ||
      localStorage.getItem("modifyMultipleAppointment") === "true"
    ) {
      this.routeDashboard();
    } else {
      this.users.forEach((user) => {
        console.log(user);
        if (user.preRegId == this.preRegId[0] && user.request && user.request.demographicDetails && user.request.demographicDetails.identity) {
          let url = "";
          url = Utils.getURL(this.router.url, "summary", 3);
          this.canDeactivateFlag = false;
          this.router.navigateByUrl(url + `/${this.preRegId[0]}/preview`);
        }
      });   
      this.routeDashboard();
    }
  }

  async displayResults(response: any) {
    if (response["registrationCenters"]) {
      this.REGISTRATION_CENTRES = response["registrationCenters"];
    } else if (response["data"]) {
      this.REGISTRATION_CENTRES = response["data"];
    }
    await this.getWorkingDays();
    this.showTable = true;
    if (this.REGISTRATION_CENTRES) {
      this.selectedRow(this.REGISTRATION_CENTRES[0]);
      this.dispatchCenterCoordinatesList();
    }
  }

  getWorkingDays() {
    return new Promise((resolve) => {
      this.REGISTRATION_CENTRES.forEach((center) => {
        this.dataService
          .getWorkingDays(center.id, this.userPreferredLangCode)
          .subscribe((response) => {
            center.workingDays = "";
            if (response[appConstants.RESPONSE] && response[appConstants.RESPONSE]["workingdays"]) {
              response[appConstants.RESPONSE]["workingdays"].forEach((day) => {
                    if (center.workingDays != "") {
                      center.workingDays += ", ";
                    }
                    center.workingDays = center.workingDays + day.name;
              });
            }
            this.pageLoaded = true;
            resolve(true);
          },
          (error) => {
            this.showErrorMessage(error);
            resolve(true);
          });
      });
    });
  }

  /**
   * @description This is a dialoug box whenever an error comes from the server, it will appear.
   *
   * @private
   * @memberof CenterSelectionComponent
   */
   private showErrorMessage(error: any, customErrMsg?: string) {
    const titleOnError = this.errorlabels.errorLabel;
    let message = "";
    if (customErrMsg) {
      message = customErrMsg;
    } else {
      message = Utils.createErrorMessage(error, this.errorlabels, this.apiErrorCodes, this.configService);  
    }
    const body = {
      case: "ERROR",
      title: titleOnError,
      message: message,
      yesButtonText: this.errorlabels.button_ok,
    };
    const dialogRef = this.openDialog(body, "400px");
    dialogRef.afterClosed().subscribe(() => {
      if (body.message === this.errorlabels.regCenterNotavailabe) {
        this.canDeactivateFlag = false;
        if (
          this.router.url.includes("multiappointment") ||
          localStorage.getItem("modifyMultipleAppointment") === "true"
        ) {
          this.routeDashboard();
        } else {
          localStorage.setItem(appConstants.MODIFY_USER, "true");
          this.router.navigate([
            `${this.userPreferredLangCode}/pre-registration/demographic/${this.preRegId[0]}`,
          ]);
        }
      }
    });
  }

  openDialog(data, width) {
    const dialogRef = this.dialog.open(DialougComponent, {
      width: width,
      data: data,
    });
    return dialogRef;
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((subscription) => subscription.unsubscribe());
  }
}
