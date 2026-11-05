import { Component, OnInit } from "@angular/core";
import { DataStorageService } from "src/app/core/services/data-storage.service";
import { Router, ActivatedRoute } from "@angular/router";
import { Subscription } from "rxjs";
import { MatDialog } from "@angular/material";
import { UserModel } from "src/app/shared/models/demographic-model/user.modal";
import { RegistrationService } from "src/app/core/services/registration.service";
import { TranslateService } from "@ngx-translate/core";
import Utils from "src/app/app.util";
import * as appConstants from "../../../app.constants";
import { ConfigService } from "src/app/core/services/config.service";
import { CodeValueModal } from "src/app/shared/models/demographic-model/code.value.modal";
import { DialougComponent } from "src/app/shared/dialoug/dialoug.component";
import identityStubJson from "../../../../assets/identity-spec.json";

@Component({
  selector: "app-preview",
  templateUrl: "./preview.component.html",
  styleUrls: ["./preview.component.css"],
})
export class PreviewComponent implements OnInit {
  previewData: any;
  Language;
  user: UserModel;
  preRegId: string;
  subscriptions: Subscription[] = [];
  files: any;
  documentTypes = [];
  documentMapObject = [];
  sameAs = "";
  residenceStatus: any;
  genders: any;
  dataCaptureLabels;
  apiErrorCodes: any;
  errorlabels: any;
  previewLabels: any;
  helpText: any;
  appPrefetched = false;
  identityData = [];
  uiFields = [];
  locationHeirarchy = [];
  locationHeirarchies = [];
  dynamicFields = [];
  dropDownFields = {};
  dataCaptureLanguages = [];
  dataCaptureLanguagesLabels = [];
  dataCaptureLangsDir = [];
  ltrLangs = this.configService
    .getConfigByKey(appConstants.CONFIG_KEYS.mosip_left_to_right_orientation)
    .split(",");
  controlIds = [];
  ControlIdLabelObjects = {};
  readOnlyMode=false;
  userPrefLanguage = localStorage.getItem("userPrefLanguage");
  userPrefLanguageDir = "";
  isNavigateToDemographic = false;
  dataLoaded = false;
  constructor(
    public dialog: MatDialog,
    private dataStorageService: DataStorageService,
    private router: Router,
    private registrationService: RegistrationService,
    private translate: TranslateService,
    private activatedRoute: ActivatedRoute,
    private configService: ConfigService
  ) {
    this.translate.use(this.userPrefLanguage);
    localStorage.setItem("modifyDocument", "false");
  }

  ngOnInit() {
    this.activatedRoute.params.subscribe((param) => {
      this.preRegId = param["appId"];
    });
    if (this.ltrLangs.includes(this.userPrefLanguage)) {
      this.userPrefLanguageDir = "ltr";
    } else {
      this.userPrefLanguageDir = "rtl";
    }
    this.getLanguageLabels();
    // kick off async initialisation (we ignore the returned Promise on purpose)
    void this.initAsync();
  }

  private async initAsync(): Promise<void> {
    await this.getIdentityJsonFormat();
    // await this.getResidentDetails();
    // await this.getGenderDetails();
    await this.getDynamicFieldValues(null);
    await this.getUserInfo();
    await this.getUserFiles();
    await this.getDocumentCategories();
    this.previewData = this.user.request.demographicDetails.identity;
    this.initializeDataCaptureLanguages();
    this.calculateAge();
    this.formatDob(this.previewData.dateOfBirth);
    this.files = this.user.files ? this.user.files : [];
    this.documentsMapping();
    this.docReqCheck();
    //remove blank fields
    let updatedUIFields = [],
      tempObj = {};
    this.uiFields.forEach((control) => {
      if (this.previewData[control.id]) {
        this.controlIds.push(control.id);
        this.dataCaptureLanguages.forEach((langCode) => {
          tempObj[langCode] = control.labelName[langCode];
        });

        this.ControlIdLabelObjects[control.id] = tempObj;
        tempObj = {};
        updatedUIFields.push(control);
      }
    });
    let locations = [];
    this.locationHeirarchies.forEach((element) => {
      locations.push(...element);
    });
    let controlId = "";
    for (let i = 0; i < this.controlIds.length; i++) {

      controlId = this.controlIds[i];
      updatedUIFields.forEach((control) => {
        if (control.id === controlId && control.fieldType === "dynamic") {
          if (control.type === appConstants.FIELD_TYPE_SIMPLE_TYPE) {
            this.previewData[controlId].forEach((ele) => {
              this.dropDownFields[controlId].forEach((codeValue) => {
                if (
                  ele.language === codeValue.languageCode &&
                  ele.value === codeValue.valueCode
                ) {
                  ele.value = codeValue.valueName;
                }
              });
            });
          }
          if (control.type === appConstants.FIELD_TYPE_STRING) {
            const ele = this.previewData[controlId];
            this.dropDownFields[controlId].forEach((codeValue) => {
              if (ele === codeValue.valueCode && this.dataCaptureLanguages[0] === codeValue.languageCode) {
                this.previewData[controlId] = codeValue.valueName;
              }
            });
          }
        }
      });
      if (locations.includes(controlId)) {
        for (let j = 0; j < this.previewData[controlId].length; j++) {
          if (this.previewData[controlId][j].value && this.previewData[controlId][j].language) {
            await this.fetchLocationName(
              this.previewData[controlId][j].value,
              this.previewData[controlId][j].language,
              j,
              controlId
            );
          }
        }
      }
    }
    this.dataLoaded = true;
    //console.log(this.previewData);
  }

  initializeDataCaptureLanguages = async () => {
    if (this.user.request) {
      const identityObj = this.user.request.demographicDetails.identity;
      if (identityObj) {
        let keyArr: any[] = Object.keys(identityObj);
        for (let index = 0; index < keyArr.length; index++) {
          const elementKey = keyArr[index];
          let dataArr = identityObj[elementKey];
          if (Array.isArray(dataArr)) {
            dataArr.forEach((dataArrElement) => {
              if (
                !this.dataCaptureLanguages.includes(dataArrElement.language)
              ) {
                this.dataCaptureLanguages.push(dataArrElement.language);
              }
            });
          }
        }
      } else if (this.user.request.langCode) {
        this.dataCaptureLanguages = [this.user.request.langCode];
      }
      //reorder the languages, by making user login lang as first one in the array
      this.dataCaptureLanguages = Utils.reorderLangsForUserPreferredLang(this.dataCaptureLanguages, this.userPrefLanguage);
      //populate the lang labels
      this.dataCaptureLanguages.forEach((langCode) => {
        JSON.parse(localStorage.getItem(appConstants.LANGUAGE_CODE_VALUES)).forEach(
          (element) => {
            if (langCode === element.code) {
              this.dataCaptureLanguagesLabels.push(element.value);
            }
          }
        );
        //set the language direction as well
        if (this.ltrLangs.includes(langCode)) {
          this.dataCaptureLangsDir.push("ltr");
        } else {
          this.dataCaptureLangsDir.push("rtl");
        }
      });
    }
    this.translate.use(this.dataCaptureLanguages[0]);
    console.log(`dataCaptureLanguages: ${this.dataCaptureLanguages}`);
  };

  checkArray(data, control) {
    let result = false;
    //if (controlId.type == "string")
    if (Array.isArray(data)) {
      result = true;
    }
    return result;
  }

  async getIdentityJsonFormat() {
    return new Promise((resolve, reject) => {
      this.dataStorageService.getIdentityJson().subscribe((response) => {
        //console.log(response);
        //response = identityStubJson;
        let identityJsonSpec = response[appConstants.RESPONSE]["jsonSpec"]["identity"];
        //console.log(jsonSpec)
        this.identityData = identityJsonSpec["identity"];
        let locationHeirarchiesFromJson = [
          ...identityJsonSpec["locationHierarchy"],
        ];
        if (Array.isArray(locationHeirarchiesFromJson[0])) {
          this.locationHeirarchies = locationHeirarchiesFromJson;
        } else {
          let hierarchiesArray = [];
          hierarchiesArray.push(locationHeirarchiesFromJson);
          this.locationHeirarchies = hierarchiesArray;
        }
        this.locationHeirarchy = this.locationHeirarchies[0];
        
        console.log(...this.locationHeirarchies);
        this.identityData.forEach((obj) => {
          if (
            obj.inputRequired === true &&
            obj.controlType !== null &&
            !(obj.controlType === "fileupload")
          ) {
            this.uiFields.push(obj);
          }
        });
        this.dynamicFields = this.uiFields.filter(
          (fields) =>
            fields.fieldType === "dynamic"
        );
        this.getIntialDropDownArrays();
        resolve(true);
      },
      (error) => {
        this.showErrorMessage(error);
      });
    });
  }

  docReqCheck() {
    this.documentMapObject.forEach((ob) => {
      this.identityData.forEach((obj) => {
        if (obj.subType === ob.code) {
          if (obj.required == true && ob.fileName === undefined) {
            ob["class"] = "error"
          }
          else {
            ob["class"] = "default"
          }
        }
      });
    });
  }

  getIntialDropDownArrays() {
    this.uiFields.forEach((control) => {
      if (control.controlType === "dropdown" || control.controlType === "button") {
        this.dropDownFields[control.id] = [];
      }
    });
  }

  private filterOnLangCode( 
    field: string,
    entityArray: any,
    langCode: string) {
    return new Promise((resolve, reject) => {
      if (entityArray) {
        entityArray.map((element: any) => {
          let codeValue: CodeValueModal;
          if (element.genderName) {
            codeValue = {
              valueCode: element.code,
              valueName: element.genderName,
              languageCode: element.langCode,
            };
          } else if (element.name) {
            codeValue = {
              valueCode: element.code,
              valueName: element.name,
              languageCode: element.langCode,
            };
          } else {
            codeValue = {
              valueCode: element.code,
              valueName: element.value,
              languageCode: langCode,
            };
          }
          this.dropDownFields[field].push(codeValue);
          resolve(true);
        });
      }
    });
  }

  getUserInfo() {
    return new Promise((resolve) => {
      this.dataStorageService
        .getUser(this.preRegId.toString())
        .subscribe((response) => {
          this.user = new UserModel(
            this.preRegId.toString(),
            response[appConstants.RESPONSE],
            undefined,
            []
          );
          let resp = response[appConstants.RESPONSE];
          if (resp["statusCode"] == appConstants.APPLICATION_STATUS_CODES.prefetched) {
            this.appPrefetched = true;
          }
          if (resp["statusCode"] !== appConstants.APPLICATION_STATUS_CODES.incomplete &&
            resp["statusCode"] !== appConstants.APPLICATION_STATUS_CODES.pending) {
            this.readOnlyMode = true;
          } else {
            this.readOnlyMode = false;
          }
          resolve(true);
        },
        (error) => {
          this.showErrorMessage(error);
        });
    });
  }

  getUserFiles() {
    return new Promise((resolve) => {
      this.dataStorageService
        .getUserDocuments(this.preRegId)
        .subscribe((response) => {
          this.setUserFiles(response);
          resolve(true);
        },
        (error) => {
          resolve(true);
          //user files can be uploaded or not
          //so we dont have to show error message
          //this.showErrorMessage(error);
        });
    });
  }

  setUserFiles(response) {
    if (!response["errors"]) {
      this.user.files = response[appConstants.RESPONSE][appConstants.METADATA];
    }
  }

  getDocumentCategories() {
    const applicantcode = localStorage.getItem("applicantType");
    return new Promise((resolve) => {
      this.dataStorageService
        .getDocumentCategories(applicantcode)
        .subscribe((response) => {
          this.documentTypes =
            response[appConstants.RESPONSE].documentCategories;
          resolve(true);
        },
        (error) => {
          this.showErrorMessage(error);
        });
    });
  }

  formatDob(dob: string) {
    dob = dob.replace(/\//g, "-");
    const ltrLangs = this.configService
    .getConfigByKey(appConstants.CONFIG_KEYS.mosip_left_to_right_orientation)
    .split(",");
    this.previewData.dateOfBirth = Utils.getBookingDateTime(
      dob,
      "",
      this.dataCaptureLanguages[0],
      ltrLangs
    );
  }

  documentsMapping() {
    this.documentMapObject = [];
    if (this.files && this.documentTypes.length !== 0) {
      this.documentTypes.forEach((type) => {
        const file = this.files.filter((file) => file.docCatCode === type.code);
        if (
          type.code === "POA" &&
          file.length === 0 &&
          this.registrationService.getSameAs() !== ""
        ) {
          const obj = {
            docName: this.sameAs,
          };
          file.push(obj);
        }
        const obj = {
          code: type.code,
          name: type.description,
          fileName: file.length > 0 ? file[0].docName : undefined,
        };
        this.documentMapObject.push(obj);
      });
    }
  }

  async getDynamicFieldValues(pageNo) {
    let pageNumber;
    if (pageNo == null) {
      pageNumber = 0;
    } else {
      pageNumber = pageNo;
    }
    return new Promise((resolve) => {
      this.subscriptions.push(
        this.dataStorageService
        .getDynamicFieldsandValuesForAllLang(pageNumber)
        .subscribe(async (response) => {
          let dynamicField = response[appConstants.RESPONSE]["data"];
          this.dynamicFields.forEach((field) => {
            dynamicField.forEach((res) => {
              if (field.subType === res.name || field.id === res.name) {
                this.filterOnLangCode(
                  field.id,
                  res["fieldVal"],
                  res["langCode"]
                );
              }
            });
          });
          let totalPages = response[appConstants.RESPONSE]["totalPages"];
          if (totalPages) {
            totalPages = Number(totalPages);
          }
          pageNumber = pageNumber + 1;
          if (totalPages > pageNumber) {
            await this.getDynamicFieldValues(pageNumber);
            resolve(true);
          } else {
            resolve(true);
          }
        },
        (error) => {
          this.showErrorMessage(error);
        })
      );  
    });
  }

  getLanguageLabels() {
    this.dataStorageService
      .getI18NLanguageFiles(this.userPrefLanguage)
      .subscribe((response) => {
        this.sameAs = response["sameAs"];
        this.previewLabels = response["preview"];
        this.helpText = response["helpText"];
        this.errorlabels = response["error"];
        this.apiErrorCodes = response[appConstants.API_ERROR_CODES];
        this.dataCaptureLabels = response["dashboard"].dataCaptureLanguage;
      });
  }

  calculateAge() {
    const now = new Date();
    const born = new Date(this.previewData.dateOfBirth);
    const years = Math.floor(
      (now.getTime() - born.getTime()) / (365.25 * 24 * 60 * 60 * 1000)
    );
    this.previewData.age = years;
  }

  modifyDemographic() {
    const url = Utils.getURL(this.router.url, "demographic", 3);
    localStorage.setItem(appConstants.MODIFY_USER_FROM_PREVIEW, "true");
    localStorage.setItem(appConstants.MODIFY_USER, "true");
    this.router.navigateByUrl(url + `/${this.preRegId}`);
  }

  modifyDocument() {
    localStorage.setItem("modifyDocument", "true");
    this.navigateBack();
  }

  private getGenderDetails() {
    return new Promise((resolve) => {
      this.dataStorageService.getGenderDetails().subscribe((response) => {
        if (response[appConstants.RESPONSE]) {
          this.genders =
            response[appConstants.RESPONSE][
              appConstants.DEMOGRAPHIC_RESPONSE_KEYS.genderTypes
            ];
          resolve(true);
        }
      });
    });
  }

  /**
   * @description This will get the residenceStatus
   * details from the master data.
   *
   * @private
   * @returns
   * @memberof DemographicComponent
   */
  private getResidentDetails() {
    return new Promise((resolve) => {
      this.dataStorageService.getResidentDetails().subscribe((response) => {
        if (response[appConstants.RESPONSE]) {
          this.residenceStatus =
            response[appConstants.RESPONSE][
              appConstants.DEMOGRAPHIC_RESPONSE_KEYS.residentTypes
            ];
          resolve(true);
        }
      });
    });
  }

  enableContinue(): boolean {
    let flag = true;
    this.documentMapObject.forEach((object) => {
      if (object.fileName === undefined) {
        if (
          object.code === "POA" &&
          this.registrationService.getSameAs() !== ""
        ) {
          flag = true;
        } else {
          flag = false;
        }
      }
    });
    return flag;
  }

  private fetchLocationName(
    locCode: string,
    langCode: string,
    index: number,
    controlName: string
  ) {
    return new Promise((resolve) => {
      this.dataStorageService
        .getLocationInfoForLocCode(locCode, langCode)
        .subscribe((response) => {
          if (response[appConstants.RESPONSE]) {
            this.previewData[controlName][index]["label"] =
              response[appConstants.RESPONSE]["name"];
          }
          resolve(true);
        },
        (error) => {
          //fetching location names can be a fail safe operation
          //in case there is some error, we can still proceed 
          resolve(true);
          //this.showErrorMessage(error);
        });
    });
  }

  /**
   * This method navigate the user to demographic page if user clicks on Add New applicant.
   */
   async onNewApplication() {
    //first check if data capture languages are in session or not
    const dataCaptureLangsFromSession = localStorage.getItem(appConstants.DATA_CAPTURE_LANGUAGES);
    console.log(`dataCaptureLangsFromSession: ${dataCaptureLangsFromSession}`);
    if (dataCaptureLangsFromSession) {
      this.navigateToDemographic();
    } else {
      //no data capture langs stored in session, hence prompt the user 
      const mandatoryLanguages = Utils.getMandatoryLangs(this.configService);
      const optionalLanguages = Utils.getOptionalLangs(this.configService);
      const maxLanguage = Utils.getMaxLangs(this.configService);
      const minLanguage = Utils.getMinLangs(this.configService);
      if (
        maxLanguage > 1 &&
        optionalLanguages.length > 0 &&
        maxLanguage !== mandatoryLanguages.length
      ) {
        await this.openLangSelectionPopup(mandatoryLanguages, minLanguage, maxLanguage);
      } else if (mandatoryLanguages.length > 0) {
        if (maxLanguage == 1) {
          localStorage.setItem(appConstants.DATA_CAPTURE_LANGUAGES, JSON.stringify([mandatoryLanguages[0]]));
        } else {
          let reorderedArr = Utils.reorderLangsForUserPreferredLang(mandatoryLanguages, this.userPrefLanguage);
          localStorage.setItem(appConstants.DATA_CAPTURE_LANGUAGES, JSON.stringify(reorderedArr));
        }
        this.isNavigateToDemographic = true;
      }
      if (this.isNavigateToDemographic) {
        let dataCaptureLanguagesLabels = Utils.getLanguageLabels(localStorage.getItem(appConstants.DATA_CAPTURE_LANGUAGES), 
          localStorage.getItem(appConstants.LANGUAGE_CODE_VALUES));
        localStorage.setItem(appConstants.DATA_CAPTURE_LANGUAGE_LABELS, JSON.stringify(dataCaptureLanguagesLabels));
        this.navigateToDemographic();
      }
    }
  }
  
  openLangSelectionPopup(mandatoryLanguages: string[], minLanguage: number, maxLanguage: number) {
    return new Promise((resolve) => {
      const popupAttributes = Utils.getLangSelectionPopupAttributes(this.dataCaptureLangsDir[0], this.dataCaptureLabels, 
        mandatoryLanguages, minLanguage, maxLanguage, this.userPrefLanguage);
      const dialogRef = this.openDialog(popupAttributes, "550px", "350px");
      dialogRef.afterClosed().subscribe((res) => {
        //console.log(res);
        if (res == undefined) {
          this.isNavigateToDemographic = false;
        } else {
          let reorderedArr = Utils.reorderLangsForUserPreferredLang(res, this.userPrefLanguage);
          localStorage.setItem(appConstants.DATA_CAPTURE_LANGUAGES, JSON.stringify(reorderedArr));
          this.isNavigateToDemographic = true;
        }
        resolve(true);
      });
    });
  }

  /**
   * @description This is a dialoug box whenever an error comes from the server, it will appear.
   *
   * @private
   * @memberof PreviewComponent
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
  openDialog(data, width, height?) {
    const dialogRef = this.dialog.open(DialougComponent, {
      width: width,
      height: height,
      data: data,
      restoreFocus: false
    });
    return dialogRef;
  }

  navigateToDemographic() {
    localStorage.setItem(appConstants.NEW_APPLICANT, "true");
    localStorage.setItem(appConstants.MODIFY_USER_FROM_PREVIEW, "false");
    localStorage.setItem(appConstants.MODIFY_USER, "false");
    localStorage.setItem(appConstants.NEW_APPLICANT_FROM_PREVIEW, "true");
    this.router.navigate([`${this.userPrefLanguage}/pre-registration/demographic/new`]);
  }
  navigateBack() {
    const url = Utils.getURL(this.router.url, "file-upload", 3);
    this.router.navigateByUrl(url + `/${this.preRegId}`);
  }

  navigateNext() {
    let url = Utils.getURL(this.router.url, "booking", 3);
    url = url + `/${this.preRegId}/pick-center`;
    this.router.navigateByUrl(url);
  }
}
