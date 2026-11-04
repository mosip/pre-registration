import {Component, OnInit, ElementRef, OnDestroy, Sanitizer, SecurityContext} from "@angular/core";
import { FormGroup, FormControl, Validators } from "@angular/forms";
import { Router, ActivatedRoute } from "@angular/router";
import * as appConstants from "../../../app.constants";
import { SafeResourceUrl } from "@angular/platform-browser";
import { ViewChild } from "@angular/core";
import { FileModel } from "src/app/shared/models/demographic-model/file.model";
import { UserModel } from "src/app/shared/models/demographic-model/user.modal";
import { RegistrationService } from "src/app/core/services/registration.service";
import { DataStorageService } from "src/app/core/services/data-storage.service";
import { TranslateService } from "@ngx-translate/core";
import { BookingService } from "../../booking/booking.service";
import { RequestModel } from "src/app/shared/models/request-model/RequestModel";
import { ConfigService } from "src/app/core/services/config.service";
import { DialougComponent } from "src/app/shared/dialoug/dialoug.component";
import { MatDialog } from "@angular/material";
import { FilesModel } from "src/app/shared/models/demographic-model/files.model";
import { LogService } from "src/app/shared/logger/log.service";
import Utils from "src/app/app.util";
import { Subscription } from "rxjs";


@Component({
  selector: "app-file-upload",
  templateUrl: "./file-upload.component.html",
  styleUrls: ["./file-upload.component.css"],
})
export class FileUploadComponent implements OnInit, OnDestroy {
  selected = [];
  @ViewChild("fileUpload")
  fileInputVariable: ElementRef;
  fileDocCatCode = "";
  sortedUserFiles: any[] = [];
  applicantType: string;
  allowedFilesHtml: string = "";
  allowedFileSize: string = "";
  sameAsselected: boolean = false;
  isModify: any;
  fileName: string = "";
  fileByteArray;
  fileUrl: SafeResourceUrl;
  applicantPreRegId: string;
  file: FileModel = new FileModel();
  userFile: FileModel[] = [this.file];
  userFiles: FilesModel = new FilesModel(this.userFile);
  formData = new FormData();
  user: UserModel = new UserModel();
  users: UserModel[] = [];
  enableBrowseButtonList = [];
  activeUsers: UserModel[] = [];
  documentCategory: string;
  documentType: string;
  loginId: string;
  documentIndex: number;
  selectedDocument: SelectedDocuments = {
    docCatCode: "",
    docTypeCode: "",
  };
  selectedDocuments: SelectedDocuments[] = [];
  dataCaptureLanguages = [];
  dataCaptureLanguagesLabels = [];
  dataCaptureLangsDir = [];
  applicationStatus: string;
  
  ltrLangs = this.config
    .getConfigByKey(appConstants.CONFIG_KEYS.mosip_left_to_right_orientation)
    .split(",");
  LOD: DocumentCategory[] = [];
  fileIndex: number = -1;
  documentLabels: any;
  demographicLabels: any;
  messagelabels: any;
  helpText: any;
  errorlabels: any;
  apiErrorCodes: any;
  fileExtension: string = "pdf";
  sameAs: string;
  disableNavigation: boolean = false;
  start: boolean = false;
  browseDisabled: boolean = true;
  documentName: string;
  flag: boolean;
  zoom: number = 0.5;
  userPrefLanguage = localStorage.getItem("userPrefLanguage");
  userPrefLanguageDir = "";
  userForm = new FormGroup({});
  validationMessage: any;
  documentUploadRequestBody: DocumentUploadRequestDTO = {
    docCatCode: "",
    docTypCode: "",
    langCode: "",
    refNumber: "",
  };
  files: FilesModel;
  documentCategoryDto: DocumentCategoryDTO = {
    attribute: "",
    value: "",
  };
  documentCategoryrequestDto: DocumentCategoryDTO[];
  documentRequest: RequestModel;
  step: number = 0;
  multipleApplicants: boolean = false;
  allApplicants: any[] = [];
  applicants: any[] = [];
  allowedFiles: string[];
  firstFile: Boolean = true;
  subscriptions: Subscription[] = [];
  identityData = [];
  uiFields = [];
  preRegId: number;
  isDocUploadRequired = [];
  fullNameField: "";
  readOnlyMode = false;
  dataLoaded = false;
  constructor(
    private registration: RegistrationService,
    private dataStorageService: DataStorageService,
    private router: Router,
    private config: ConfigService,
    private sanitizer: Sanitizer,
    private bookingService: BookingService,
    private translate: TranslateService,
    private dialog: MatDialog,
    private loggerService: LogService,
    private activatedRoute: ActivatedRoute
  ) {
    this.translate.use(this.userPrefLanguage);
  }

  ngOnInit(): void {
    // keep the purely sync part here
    this.getPrimaryLabels(this.userPrefLanguage);
    if (this.ltrLangs.includes(this.userPrefLanguage)) {
      this.userPrefLanguageDir = 'ltr';
    } else {
      this.userPrefLanguageDir = 'rtl';
    }
    // async part moved to helper; fire and forget
    void this.initAsync();
  }

  private async initAsync(): Promise<void> {
    await this.initiateComponent();
    this.fullNameField = this.config.getConfigByKey(
      appConstants.CONFIG_KEYS.preregistration_identity_name
    );
    this.getFileSize();
    this.allowedFiles = this.config.getConfigByKey(appConstants.CONFIG_KEYS.preregistration_document_alllowe_files).split(",");
    this.getAllowedFileTypes(this.allowedFiles);
    this.loginId = localStorage.getItem("loginId");
    await this.getAllApplicants();
    this.sameAs = this.registration.getSameAs();
    if (this.sameAs === "") {
      this.sameAsselected = false;
    } else {
      this.sameAsselected = true;
    }
    if (this.readOnlyMode) {
      this.userForm.disable();
    }
  }

  async getIdentityJsonFormat() {
    return new Promise((resolve) => {
      this.dataStorageService.getIdentityJson().subscribe((response) => {
        //response = identityStubJson;
        let identityJsonSpec =
          response[appConstants.RESPONSE]["jsonSpec"]["identity"];
        this.identityData = identityJsonSpec["identity"];
        this.identityData.forEach((obj) => {
          if (obj.controlType === "fileupload") {
            this.uiFields.push(obj);
          }
        });
        resolve(true);
      },
      (error) => {
        this.showErrorMessage(error);
      });
    });
  }

  private getPrimaryLabels(lang) {
    this.dataStorageService
    .getI18NLanguageFiles(lang)
    .subscribe((response) => {
      if (response["message"]) {
        this.documentLabels = response["documents"];
        this.demographicLabels = response["demographic"];
        this.helpText = response["helpText"];
        this.messagelabels = response["message"];
        this.errorlabels = response["error"];
        this.apiErrorCodes = response[appConstants.API_ERROR_CODES];
      }
            
    });
  }

  /**
   *@description This method initialises the users array and the language set by the user.
   *@private
   * @memberof FileUploadComponent
   */
  private async initiateComponent() {

    await this.getIdentityJsonFormat();
    this.isModify = localStorage.getItem("modifyDocument");
    this.activatedRoute.params.subscribe((param) => {
      this.preRegId = param["appId"];
    });
    if (this.preRegId) {
      await this.getUserInfo();
      await this.getUserFiles();
      if (!this.users[0].files) {
        this.users[0].files = this.userFiles;
      }
      this.initializeDataCaptureLanguages();
      this.translate.use(this.dataCaptureLanguages[0]);
      await this.getApplicantTypeID();
      //on page load, update application status from "Application_Incomplete"
      //to "Pending_Appointment", if all required documents are uploaded
      await this.changeStatusToPending();
      //on page load, update application status from "Pending_Appointment"
      //to "Application_Incomplete", if all required documents are NOT uploaded
      await this.changeStatusToIncomplete(); 
      this.dataLoaded = true;
    } else {
      if (!this.users[0].files) {
        this.users[0].files = this.userFiles;
      }
      this.loggerService.info("active users", this.activeUsers);
    }
  }

  getUserInfo() {
    return new Promise((resolve) => {
      this.dataStorageService
        .getUser(this.preRegId.toString())
        .subscribe((response) => {
          this.users.push(
            new UserModel(
              this.preRegId.toString(),
              response[appConstants.RESPONSE],
              undefined,
              undefined
            )
          );
          let resp = response[appConstants.RESPONSE];
          this.applicationStatus = resp["statusCode"];
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
          //this is fail safe operation as user may not have uploaded any documents yet
          //so no err handling is required
          resolve(true);
          //this.showErrorMessage(error);
        });
    });
  }
  setUserFiles(response) {
    if (!response["errors"]) {
      this.userFile = response[appConstants.RESPONSE][appConstants.METADATA];
    } else {
      let fileModel: FileModel = new FileModel("", "", "", "", "", "", "", "");
      if (this.userFile.length === 0) {
        this.userFile.push(fileModel);
      }
    }
    this.userFiles["documentsMetaData"] = this.userFile;
  }

  onModification() {
    if (
      this.users[0].files &&
      this.users[0].files.documentsMetaData[0].docCatCode &&
      this.users[0].files.documentsMetaData[0].docCatCode !== ""
    ) {
      for (
        let index = 0;
        index < this.users[0].files.documentsMetaData.length;
        index++
      ) {
        const fileMetadata = this.users[0].files.documentsMetaData;
        let arr = [];
        let indice: number;
        let indexLOD: number;
         this.LOD.forEach((ele, i) => {
          if (ele.code === fileMetadata[index].docCatCode) {
            indice = index;
            indexLOD = i;
            ele.selectedRefNumber = fileMetadata[index].refNumber
              ? fileMetadata[index].refNumber
              : "";
            arr.push(ele);
          }
        });
        if (arr.length > 0) {
          let temp = arr[0].documentTypes.filter(
            (ele) => ele.code === fileMetadata[indice].docTypCode
          );
          this.LOD[indexLOD].selectedDocName = temp[0].code;
          this.LOD[indexLOD].selectedRefNumber = arr[0].selectedRefNumber
            ? arr[0].selectedRefNumber
            : "";
        }
      }
    } else return;
  }

  initializeDataCaptureLanguages = async () => {
    if (this.users.length > 0) {
      const identityObj = this.users[0].request.demographicDetails.identity;
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
      } else if (this.users[0].request.langCode) {
        this.dataCaptureLanguages = [this.users[0].request.langCode];
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
    console.log(this.dataCaptureLanguages);
  };

  /**
   *@description method to change the current user to be shown as None value in the same as array.
   *@private
   * @memberof FileUploadComponent
   */
  private setNoneApplicant() {
    let allApplicants = this.allApplicants;
    if (this.users && allApplicants) {
      let filtered = allApplicants.filter(applicant => applicant.preRegistrationId !== this.preRegId);
      this.allApplicants = filtered;
    }  
  }
  /**
   *@description method to initialise the allowedFiles array used to show in the html page
   *
   * @param {string[]} allowedFiles
   * @memberof FileUploadComponent
   */
  getAllowedFileTypes(allowedFiles: string[]) {
    let i = 0;
    for (let file of allowedFiles) {
      if (i == 0) {
        this.allowedFilesHtml =
          this.allowedFilesHtml + file.substring(file.indexOf("/") + 1);
      } else {
        this.allowedFilesHtml =
          this.allowedFilesHtml + "," + file.substring(file.indexOf("/") + 1);
      }
      i++;
    }
  }
  /**
   *@description method to set the value of allowed file size to be displayed in html
   *
   * @memberof FileUploadComponent
   */
  getFileSize() {
    this.allowedFileSize =
      (
        this.config.getConfigByKey(
          appConstants.CONFIG_KEYS.preregistration_document_alllowe_file_size
        ) / 1000000
      ).toString() + "mb";
  }

  /**
   *
   *@description after add applicant the allaplicants array contains an extra none.
   *This method removes this extra none.
   * @memberof FileUploadComponent
   */
  removeExtraNone() {
    let i: number = 0;
    for (let applicant of this.allApplicants) {
      if (applicant.preRegistrationId == "") {
        this.allApplicants.splice(i, 1);
      }
      i++;
    }
  }
  /**
   *@description method to check if none is available or not
   *
   * @returns
   * @memberof FileUploadComponent
   */
  isNoneAvailable() {
    let noneCount: number = 0;
    for (let applicant of this.allApplicants) {
      if (applicant.preRegistrationId == "") {
        noneCount++;
      }
    }
    return true;
  }
  /**
   *@description method to sorf the files in the users array according to the doccument categories in LOD. Will be used in future for sorting files.
   *
   * @memberof FileUploadComponent
   */
  sortUserFiles() {
    for (let document of this.LOD) {
      for (let file of this.users[0].files.documentsMetaData) {
        if (document.code === file.docCatCode) {
          this.sortedUserFiles.push(file);
        }
      }
    }
    for (let i = 0; i <= this.users[0].files[0].documentsMetaData; i++) {
      this.users[0].files[0][i] = this.sortedUserFiles[i];
    }
  }

  /**
   *
   *@description method to get applicants name array to be shown in same as List.
   * @param {*} applicants
   * @returns
   * @memberof FileUploadComponent
   */
  getApplicantsName(applicants) {
    let i = 0;
    let j = 0;
    let allApplicants: any[] = [];
    allApplicants = JSON.parse(JSON.stringify(applicants));
    allApplicants.forEach(applicant => {
      let nameFieldObject = applicant["demographicMetadata"][this.fullNameField];
      if (nameFieldObject && Array.isArray(nameFieldObject)) {
        let found = false;
        nameFieldObject.forEach(nameField => {
          if (nameField.language == this.userPrefLanguage) {
            applicant["applicantName"] = nameField.value;
            found = true; 
          }
        });
        if (!found) {
          applicant["applicantName"] = nameFieldObject[0].value;
        }
      } else {
        applicant["applicantName"] = nameFieldObject;
      }
      //console.log(applicant);
    });
    //console.log("allApplicants>>>" + JSON.stringify(allApplicants));
    return allApplicants;
  }
  /**
   *
   *@description method to get the applicant type code to fetch the document cagtegories to be uploaded.
   * @memberof FileUploadComponent
   */
  async getApplicantTypeID() {
    //console.log("getApplicantTypeID");
    let attributesArr = [];
    const identityObj = this.users[0].request.demographicDetails.identity;
    if (identityObj) {
      let keyArr: any[] = Object.keys(identityObj);
      for (let index = 0; index < keyArr.length; index++) {
        const element = keyArr[index];
        if (element != appConstants.IDSchemaVersionLabel) {
          let elemValue = identityObj[element];
          // this.identityData.forEach((obj) => {
          //   if (element === obj.id && obj.controlType === "ageDate" || obj.controlType === "date") {
          //     console.log(elemValue);
          //   }
          // });
          attributesArr.push({
            "attribute": element,
            "value": elemValue
          }); 
        }
      }
    }
    attributesArr.push({
      "attribute": appConstants.APPLICANT_TYPE_ATTRIBUTES.biometricAvailable,
      "value": false 
    });
    let applicantTypeReq = new RequestModel(
      appConstants.IDS.applicantTypeId,
      {
        "attributes": attributesArr
      },
      {}
    );
    return new Promise((resolve) => {
      this.subscriptions.push(
        this.dataStorageService
        .getApplicantType(applicantTypeReq)
        .subscribe(
          async (response) => {
            if (response[appConstants.RESPONSE]) {
              localStorage.setItem(
                "applicantType",
                response["response"].applicantType.applicantTypeCode
              );
              await this.getDocumentCategories(
                response["response"].applicantType.applicantTypeCode
              );
              this.setApplicantType(response);
              resolve(true);
            } 
          },
          (error) => {
            this.showErrorMessage(error);
          }
        )
      );
    });    
  }

  /**
   *@description method to set applicant type.
   *
   * @param {*} response
   * @memberof FileUploadComponent
   */
  async setApplicantType(response) {
    this.applicantType = await response["response"].applicationtypecode;
  }
  /**
   *@description method to get document catrgories from master data.
   *
   * @param {*} applicantcode
   * @memberof FileUploadComponent
   */
  async getDocumentCategories(applicantcode) {
    return new Promise((resolve) => {
      this.subscriptions.push(
        this.dataStorageService
        .getDocumentCategoriesByLang(applicantcode, this.userPrefLanguage)
        .subscribe(
          (res) => {
            if (res[appConstants.RESPONSE]) {
              let documentCategories = res["response"].documentCategories;
              //console.log("documentCategories received");
              documentCategories.forEach((documentCategory) => {
                this.uiFields.forEach((uiField) => {
                  if (uiField.subType == documentCategory.code) {
                    if (uiField.inputRequired && uiField.required) { 
                      documentCategory["required"] = uiField.required;
                      documentCategory["labelName"] = uiField.labelName;
                      documentCategory["containerStyle"] = uiField.containerStyle;
                      documentCategory["headerStyle"] = uiField.headerStyle;
                      documentCategory["id"] = uiField.id;
                      this.userForm.addControl(uiField.id, new FormControl(""));
                      this.userForm.controls[uiField.id].setValidators(
                          Validators.required
                        );
                      this.userForm.controls[uiField.id].setValue("");
                      this.LOD.push(documentCategory);
                    }
                  }
                });
              });
              documentCategories.forEach((documentCategory) => {
                this.uiFields.forEach((uiField) => {
                   if (uiField.subType == documentCategory.code) {
                    if (uiField.inputRequired && !uiField.required) {
                      documentCategory["required"] = uiField.required;
                      documentCategory["labelName"] = uiField.labelName;
                      documentCategory["containerStyle"] = uiField.containerStyle;
                      documentCategory["headerStyle"] = uiField.headerStyle;
                      documentCategory["id"] = uiField.id;
                      this.userForm.addControl(uiField.id, new FormControl(""));
                      this.userForm.controls[uiField.id].setValue("");
                      this.LOD.push(documentCategory);
                    }
                  } 
                });
              });
              if (this.userFiles && this.userFiles["documentsMetaData"]) {
                this.userFiles["documentsMetaData"].forEach((userFile) => {
                  this.uiFields.forEach((uiField) => {
                    if (uiField.subType == userFile.docCatCode) {
                      if (this.userForm.controls[uiField.id]) {
                        this.userForm.controls[uiField.id].setValue(
                          userFile.docName
                        );
                      }
                    }
                  });
                });
              }
              //this.LOD = res["response"].documentCategories;
              //console.log(this.LOD);
              this.enableBrowseButtonList = new Array(this.LOD.length).fill(
                false
              );
              this.onModification();
              //console.log(this.LOD);
              resolve(true);
            } 
          },
          (error) => {
            this.showErrorMessage(error);
          }
        )
      );  
    });  
  }

  /**
   *@description method to get the list of applicants to eb shown in same as options
   *
   * @memberof FileUploadComponent
   */
  async getAllApplicants() {
    return new Promise((resolve) => {
      this.subscriptions.push(
        this.dataStorageService.getUsers(this.loginId).subscribe(
          (response) => {
            if (response[appConstants.RESPONSE]) {
              this.bookingService.addApplicants(
                response["response"]["basicDetails"]
              );
            } 
          },
          (error) => {
            //the is a fail safe operation hence no err messages are to be displayed
            //this.showErrorMessage(error);
          },
          () => {
            this.setApplicants();
            resolve(true)
          }
        )
      );
    });    
  }
  
  /**
   *@description method to set the applicants array  used in same as options aray
   *
   * @memberof FileUploadComponent
   */
  setApplicants() {
    this.applicants = JSON.parse(
      JSON.stringify(this.bookingService.getAllApplicants())
    );
    this.removeApplicantsWithoutPOA();
    this.updateApplicants();
    let temp = this.getApplicantsName(this.applicants);
    this.allApplicants = JSON.parse(JSON.stringify(temp));
    console.log("this.allApplicants" + this.allApplicants.length);
    temp = JSON.parse(JSON.stringify(this.allApplicants));
    this.setNoneApplicant();
  }

  removeApplicantsWithoutPOA() {
    let i = 0;
    let tempApplicants = [];
    for (let applicant of this.applicants) {
      if (applicant.demographicMetadata["proofOfAddress"] != null) {
        tempApplicants.push(this.applicants[i]);
      }
      i++;
    }
    this.applicants = JSON.parse(JSON.stringify(tempApplicants));
  }

  updateApplicants() {
    let flag: boolean = false;
    let x: number = 0;
    for (let i of this.activeUsers) {
      for (let j of this.applicants) {
        if (i.preRegId == j.preRegistrationId) {
          flag = true;
          break;
        }
      }
      if (flag) {
        this.activeUsers.splice(x, 1);
      }
      x++;
    }
    let fullName: FullName = {
      language: "",
      value: "",
    };
    let user: Applicants = {
      preRegistrationId: "",
      demographicMetadata: {
        fullName: [fullName],
      },
    };
    let activeUsers: any[] = [];
    for (let i of this.activeUsers) {
      fullName = {
        language: "",
        value: "",
      };
      user = {
        preRegistrationId: "",
        demographicMetadata: {
          fullName: [fullName],
        },
      };
      if (i.files) {
        for (let file of i.files.documentsMetaData) {
          if (file.docCatCode === "POA") {
            user.preRegistrationId = i.preRegId;
            user.demographicMetadata.fullName =
              i.request.demographicDetails.identity.fullName;
            activeUsers.push(JSON.parse(JSON.stringify(user)));
          }
        }
      }
    }

    for (let i of activeUsers) {
      this.applicants.push(i);
    }
  }

  /**
   *@description method to preview the first file.
   *
   * @memberof FileUploadComponent
   */
  viewFirstFile() {
    this.fileIndex = 0;
    this.viewFile(this.users[0].files[0].documentsMetaData[0]);
  }
  /**
   *@description method to preview file by index.
   *
   * @param {number} i
   * @memberof FileUploadComponent
   */
  viewFileByIndex(i: number) {
    this.viewFile(this.users[0].files.documentsMetaData[i]);
  }

  setByteArray(fileByteArray) {
    this.fileByteArray = fileByteArray;
  }

  /**
   *@description method to preview a specific file.
   *
   * @param {FileModel} file
   * @memberof FileUploadComponent
   */
  viewFile(fileMeta: FileModel) {
    this.fileIndex = 0;
    this.disableNavigation = true;
    const subs = this.dataStorageService
      .getFileData(fileMeta.documentId, this.users[0].preRegId)
      .subscribe(
        (res) => {
          if (res[appConstants.RESPONSE]) {
            this.setByteArray(res["response"].document);
          } 
          this.fileName = fileMeta.docName;
          this.fileDocCatCode = fileMeta.docCatCode;
          let i = 0;
          for (let x of this.users[0].files.documentsMetaData) {
            if (
              this.fileName === x.docName &&
              this.fileDocCatCode === x.docCatCode
            ) {
              break;
            }
            i++;
          }
          this.fileIndex = i;
          this.fileExtension = fileMeta.docName.substring(
            fileMeta.docName.indexOf(".") + 1
          );
          this.fileExtension = this.fileExtension.toLowerCase();
          if (this.fileByteArray) {
            switch (this.fileExtension) {
              case "pdf":
                this.flag = false;
                this.fileUrl =
                  "data:application/pdf;base64," + this.fileByteArray;
                break;
              default:
                this.flag = true;
                this.fileUrl = this.sanitizer.sanitize(
                    SecurityContext.URL,
                    "data:image/jpeg;base64," + this.fileByteArray
                );
                break;
            }
          }
          this.disableNavigation = false;
        },
        (error) => {
          this.start = false;
          this.disableNavigation = false;
          this.showErrorMessage(error);
        });
    this.subscriptions.push(subs);
  }

  /**
   *@description method to preview a specific file.
   *
   * @param {FileModel} file
   * @memberof FileUploadComponent
   */
   deleteUploadedFile(fileMeta) {
    //console.log(fileMeta);
    let dialogRef = this.confirmationDialog(fileMeta.docName);
    dialogRef.afterClosed().subscribe((confirm) => {
      if (confirm == true) {
        this.disableNavigation = true;
        const subs = this.dataStorageService
          .deleteFile(fileMeta.documentId, this.preRegId)
          .subscribe(
            (res) => {
            if (res[appConstants.RESPONSE]) {
              //console.log("deleted");
              if (fileMeta.docCatCode === "POA") {
                console.log(fileMeta.docCatCode);
                this.sameAsselected = false;
                this.registration.setSameAs("");
                this.sameAs = this.registration.getSameAs();
              } 
              let allFiles = this.users[0].files.documentsMetaData;
              if (allFiles) {
                let updatedFiles = allFiles.filter(file => file.docCatCode !== fileMeta.docCatCode);
                this.users[0].files.documentsMetaData = updatedFiles;
              }
              let index: number;
              this.LOD.forEach((ele, i) => {
                if (ele.code === fileMeta.docCatCode) index = i;
              });
              this.LOD[index].selectedDocName = "";
              this.LOD[index].selectedRefNumber = "";
              this.uiFields.forEach((uiField) => {
                if (uiField.subType == this.LOD[index].code) {
                  this.userForm.controls[this.LOD[index].id].setValue("");
                }
              });
              this.removeFilePreview();
              //When users deletes uploaded file, then we have to move
              //application back to "Incomplete" status.
              this.changeStatusToIncomplete(); 
            }
            this.disableNavigation = false;
          },
          (error) => {
            this.disableNavigation = false;
            this.showErrorMessage(error, this.messagelabels.uploadDocuments.msg10);
          }
        );
        this.subscriptions.push(subs);
      } 
    });  
  }

  confirmationDialog(fileName: string) {
    let body = {
      case: "CONFIRMATION",
      title: this.messagelabels.uploadDocuments.title_confirm,
      message: this.messagelabels.uploadDocuments.msg11 + fileName,
      yesButtonText: this.messagelabels.uploadDocuments.title_confirm,
      noButtonText: this.messagelabels.uploadDocuments.button_cancel,
    };
    const dialogRef = this.openDialog(body, "400px");
    return dialogRef;
  }

  /**
   *@description method to preview last available file.
   *
   * @memberof FileUploadComponent
   */
  viewLastFile() {
    this.fileIndex = this.users[0].files[0].documentsMetaData.length - 1;
    this.viewFile(this.users[0].files[0].documentsMetaData[this.fileIndex]);
  }

  /**
   * dynamic assigning of idSS
   *
   * @param {*} i
   * @memberof FileUploadComponent
   */
  clickOnButton(i) {
    document.getElementById("file_" + i).click();
  }

  /**
   *@description method gets called when a file has been uploaded from the html.
   *
   * @param {*} event
   * @memberof FileUploadComponent
   */
  handleFileInput(
    event: any,
    docName: string,
    docCode: string,
    refNumber: string
  ) {
    const extensions = this.allowedFilesHtml.split(',').map(e => e.trim()).filter(Boolean);
    const extensionRegex = extensions.length ? new RegExp('^(?:' + extensions.join('|') + ')$') : null;
    const oldFileExtension = this.fileExtension;
    this.fileExtension = event.target.files[0].name.substring(
      event.target.files[0].name.indexOf(".") + 1
    );
    this.fileExtension = this.fileExtension.toLowerCase();
    let allowedFileUploaded: Boolean = false;
    this.disableNavigation = true;

    // if (event.target.files[0].type === file) {
    if (extensionRegex && extensionRegex.test(this.fileExtension)) {
      allowedFileUploaded = true;
      if (
        event.target.files[0].name.length <=
        this.config.getConfigByKey(
          appConstants.CONFIG_KEYS
            .preregistration_document_alllowe_file_name_lenght
        )
      ) {
        if (
          event.target.files[0].size <
          this.config.getConfigByKey(
            appConstants.CONFIG_KEYS.preregistration_document_alllowe_file_size
          )
        ) {
          this.getBase64(event.target.files[0]).then((data) => {
            this.fileByteArray = data;
          });
          if (!this.documentType && !this.documentCategory) {
            this.setJsonString(docName, docCode, refNumber);
          }
          this.sendFile(event);
        } else {
          this.displayMessage(
            this.errorlabels.errorLabel,
            this.messagelabels.uploadDocuments.msg1
          );
          this.disableNavigation = false;
        }
      } else {
        this.displayMessage(
          this.errorlabels.errorLabel,
          this.messagelabels.uploadDocuments.msg5
        );
        this.disableNavigation = false;
      }
      this.fileExtension = oldFileExtension;
    }

    if (!allowedFileUploaded) {
      this.fileExtension = oldFileExtension;
      this.displayMessage(
        this.errorlabels.errorLabel,
        this.messagelabels.uploadDocuments.msg3
      );
      this.disableNavigation = false;
    }
  }

  /**
   *@description method gets called when a value in "refNumber" textbox is changed.
   *
   * @param {*} event
   * @memberof FileUploadComponent
   */
  handleDocRefInput(event: any, docCode: string) {
    const refNumber = event.target.value;
    const validRegex = new RegExp("^[a-zA-Z0-9_.-\\s]*$");
    const validRegex1 = new RegExp("^(?=.{0,50}$).*");
    if (validRegex.test(refNumber) == false) {
      event.target.value = "";
      this.showErrorMessage(null, this.messagelabels.uploadDocuments.msg12);
    }
    else if (validRegex1.test(refNumber) == false) {
      event.target.value = "";
      this.showErrorMessage(null, this.messagelabels.uploadDocuments.msg13);
    } else { 
      for (let file of this.users[0].files.documentsMetaData) {
        if (file.docCatCode == docCode) {
          let documentId = file.documentId;
          this.disableNavigation = true;
          const subs = this.dataStorageService
            .updateDocRefId(documentId, this.preRegId, refNumber)
            .subscribe(
              (response) => {
                //docRedId saved
                this.disableNavigation = false;
              },
              (error) => {
                this.disableNavigation = false;
                this.showErrorMessage(error);
              });
          this.subscriptions.push(subs);
        }
      }
    }
  }

  /**
   *@description method to get base 64 of a file
   *
   * @param {*} file
   * @returns
   * @memberof FileUploadComponent
   */
  getBase64(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => resolve(reader.result);
      reader.onerror = () => reject(reader.error);
    });
  }

  /**
   *@description method called when the docuemnt type option has been changed in a document category
   *
   * @param {*} event
   * @param {number} index
   * @memberof FileUploadComponent
   */
  selectChange(event, index: number) {
    this.enableBrowseButtonList[index] = true;
    this.LOD[index].selectedRefNumber = "";
    let found = false;
    let i = -1;
    this.documentCategory = event.source.placeholder;
    this.documentType = event.source.value;
    this.selectedDocument.docCatCode = JSON.parse(
      JSON.stringify(this.documentCategory)
    );
    this.selectedDocument.docTypeCode = JSON.parse(
      JSON.stringify(this.documentType)
    );
    if (this.selectedDocuments.length > 0) {
      for (let document of this.selectedDocuments) {
        i++;
        if (document.docCatCode == this.documentCategory) {
          found = true;
          this.selectedDocuments[i] = this.selectedDocument;
          break;
        }
      }
    }
    if (!found) {
      this.selectedDocuments.push(this.selectedDocument);
    }

    this.selectedDocument = {
      docCatCode: "",
      docTypeCode: "",
    };

    this.documentIndex = index;
    this.setJsonString(this.documentType, this.documentCategory, "");
  }

  /**
   *@description method called when the docuemnt type option has been opened in a document category
   *
   * @param {*} event
   * @param {number} index
   * @memberof FileUploadComponent
   */
  openedChange(index: number, event) {
    this.documentCategory = this.LOD[index].code;
    this.documentIndex = index;
    if (this.selectedDocuments.length > 0) {
      for (let document of this.selectedDocuments) {
        if (document.docCatCode == this.documentCategory) {
          this.documentType = document.docTypeCode;
        }
      }
    }
  }
  onFilesChange() {}
  /**
   *@description method to remove the preview of a file.
   *
   * @memberof FileUploadComponent
   */
  removeFilePreview() {
    this.fileName = "";
    this.fileUrl = this.sanitizer.sanitize(SecurityContext.URL, "");
    this.fileIndex = -1;
  }

  /**
   *@description method to set the Json string required to send the file to server.
   *
   * @param {*} event
   * @memberof FileUploadComponent
   */
  setJsonString(docName: string, docCode: string, refNumber: string) {
    this.documentUploadRequestBody.docCatCode = docCode;
    this.documentUploadRequestBody.langCode = this.dataCaptureLanguages[0];
    this.documentUploadRequestBody.docTypCode = docName;
    this.documentUploadRequestBody.refNumber = refNumber;
    this.documentRequest = new RequestModel(
      appConstants.IDS.documentUpload,
      this.documentUploadRequestBody,
      {}
    );
    this.documentCategory = null;
    this.documentType = null;
  }

  /**
   *@description method to send the file to the server.
   *
   * @param {*} event
   * @memberof FileUploadComponent
   */
  sendFile(event) {
    this.formData.append(
      appConstants.DOCUMENT_UPLOAD_REQUEST_DTO_KEY,
      JSON.stringify(this.documentRequest)
    );
    this.formData.append(
      appConstants.DOCUMENT_UPLOAD_REQUEST_DOCUMENT_KEY,
      event.target.files.item(0)
    );

    const subs = this.dataStorageService
      .sendFile(this.formData, this.users[0].preRegId)
      .subscribe(
        async (response) => {
          if (response[appConstants.RESPONSE]) {
            this.updateUsers(response);
            //on file upload, update application status from "Application_Incomplete"
            //to "Pending_Appointment", if all required documents are uploaded
            await this.changeStatusToPending();
          } 
        },
        (error) => {
          if (error && error.error && error.error.errors && error.error.errors.length > 0 && error.error.errors[0].errorCode === "PRG_PAM_DOC_025"){
            this.showErrorMessage(error, this.messagelabels.uploadDocuments.msg14);
          } else {
            this.showErrorMessage(error, this.messagelabels.uploadDocuments.msg7);
          }
          this.fileInputVariable.nativeElement.value = "";
          this.disableNavigation = false;
        },
        () => {
          this.fileInputVariable.nativeElement.value = "";
          this.disableNavigation = false;
        }
      );
    this.formData = new FormData();
    this.subscriptions.push(subs);
  }

  /**
   *@description method to update the users array after a file has been uploaded.
   *
   * @param {*} fileResponse
   * @memberof FileUploadComponent
   */
  updateUsers(fileResponse) {
    //console.log(fileResponse);
    let i = 0;
    let fileObject = new FileModel();
    fileObject.docCatCode = fileResponse.response.docCatCode;
    fileObject.doc_file_format = fileResponse.response.docFileFormat;
    fileObject.documentId = fileResponse.response.docId;
    fileObject.docName = fileResponse.response.docName;
    fileObject.docTypCode = fileResponse.response.docTypCode;
    fileObject.multipartFile = this.fileByteArray;
    fileObject.prereg_id = this.users[0].preRegId;
    fileObject.refNumber = fileResponse.response.refNumber;
    this.uiFields.forEach((uiField) => {
      if (uiField.subType == fileResponse.response.docCatCode) {
        this.userForm.controls[uiField.id].setValue(
          fileResponse.response.docName
        );
      }
    });
    //console.log(`this.fileDocCatCode: ${this.fileDocCatCode}`);
    if (this.fileDocCatCode == fileResponse.response.docCatCode) {
      this.removeFilePreview();
    }
    //console.log("sendFile");
    for (let file of this.users[0].files.documentsMetaData) {
      if (
        file.docCatCode == fileObject.docCatCode ||
        file.docCatCode == null ||
        file.docCatCode == ""
      ) {
        this.users[this.step].files.documentsMetaData[i] = fileObject;
        break;
      }
      i++;
    }
    if (i == this.users[0].files.documentsMetaData.length) {
      this.users[this.step].files.documentsMetaData.push(fileObject);
    }
    //console.log(this.users[0].files.documentsMetaData);
    this.userFile = [];
  }

  changeStatusToPending = async () => {
    //console.log("changeStatusToPending");
    //check if all required documents have been uploaded
    this.uiFields.forEach((control) => {
      const controlId = control.id;
      if (this.userForm.controls[`${controlId}`]) {
        this.userForm.controls[`${controlId}`].markAsTouched();
      }
    });
    //console.log(this.userForm.valid);
    //if yes, and if application status is "Application_Incomplete",
    //then update it to "Pending_Appointment"
    if (this.userForm.valid) {
      //console.log("calling updateApplicationStatus");
      await this.updateApplicationStatus(appConstants.APPLICATION_STATUS_CODES.incomplete, 
        appConstants.APPLICATION_STATUS_CODES.pending);
    } 
    //Mark all form fields are untouched to prevent errors before Submit. 
    this.uiFields.forEach((control) => {
      const controlId = control.id;
      if (this.userForm.controls[`${controlId}`]) {
        this.userForm.controls[`${controlId}`].markAsUntouched();
      }
    });
  }

  //When users deletes uploaded file, then we have to move
  //application back to "Incomplete" status.
  changeStatusToIncomplete = async () => {
    //console.log("changeStatusToIncomplete");
    //check if all required documents have been uploaded
    this.uiFields.forEach((control) => {
      const controlId = control.id;
      if (this.userForm.controls[`${controlId}`]) {
        this.userForm.controls[`${controlId}`].markAsTouched();
      }
    });
    //console.log(this.userForm.valid);
    //if yes, and if application status is "Pending_Appointment",
    //then update it to "Application_Incomplete"
    if (!this.userForm.valid) {
      //console.log("calling updateApplicationStatus");
      await this.updateApplicationStatus(appConstants.APPLICATION_STATUS_CODES.pending, 
        appConstants.APPLICATION_STATUS_CODES.incomplete);
    } 
    //Mark all form fields are untouched to prevent errors before Submit. 
    this.uiFields.forEach((control) => {
      const controlId = control.id;
      if (this.userForm.controls[`${controlId}`]) {
        this.userForm.controls[`${controlId}`].markAsUntouched();
      }
    });
  }

  openFile() {
    const file = new Blob(this.users[0].files[0][0].multipartFile, {
      type: "application/pdf",
    });
    const fileUrl = URL.createObjectURL(file);
    window.open(fileUrl);
  }

  /**
   *@description method called when a same as option has been selected.
   *
   * @param {*} event
   * @memberof FileUploadComponent
   */
  sameAsChange(event, fileMetadata) {
    this.disableNavigation = true;
    if (event.value == "") {
      let arr = fileMetadata.filter((ent) => ent.docCatCode === "POA");
      //console.log("removing file " + arr[0].documentId);
      const subs = this.dataStorageService
        .deleteFile(arr[0].documentId, this.preRegId)
        .subscribe(
          (res) => {
            if (res[appConstants.RESPONSE]) {
              this.sameAsselected = false;
              this.registration.setSameAs(event.value);
              this.removePOADocument();
              let index: number;
              this.LOD.forEach((ele, i) => {
                if (ele.code === "POA") index = i;
              });
              this.LOD[index].selectedDocName = "";
              this.LOD[index].selectedRefNumber = "";
              this.uiFields.forEach((uiField) => {
                if (uiField.subType == this.LOD[index].code) {
                  this.userForm.controls[this.LOD[index].id].setValue("");
                }
              });
              this.removeFilePreview();
              //When users deletes uploaded file, then we have to move
              //application back to "Incomplete" status.
              this.changeStatusToIncomplete(); 
            }
            this.disableNavigation = false;
          },
          (error) => {
            this.disableNavigation = false;
            this.showErrorMessage(error, this.messagelabels.uploadDocuments.msg9);
          }
        );
      this.subscriptions.push(subs);
    } else {
      //console.log("copying file " + event.value);
      const subs = this.dataStorageService
        .copyDocument(event.value, this.users[0].preRegId)
        .subscribe(
          async (response) => {
            if (response[appConstants.RESPONSE]) {
              this.registration.setSameAs(event.value);
              this.removePOADocument();
              this.updateUsers(response);
              //on copy document, update application status from "Application_Incomplete"
              //to "Pending_Appointment", if all required documents are uploaded
              await this.changeStatusToPending();
              let index: number;
              let poaTypes = [];
              this.LOD.forEach((ele, i) => {
                if (ele.code === "POA") {
                  index = i;
                  poaTypes.push(ele);
                }
              });
              let docList = poaTypes[0].documentTypes.filter(
                (element) => element.code === response["response"]["docTypCode"]
              );
              this.documentName = docList[0].code;
              this.LOD[index].selectedDocName = this.documentName;
              this.LOD[index].selectedRefNumber =
                response["response"]["refNumber"];
              this.sameAsselected = true;  
            } else {
              this.sameAs = this.registration.getSameAs();
              this.sameAsselected = false;
              this.displayMessage(
                this.errorlabels.errorLabel,
                this.messagelabels.uploadDocuments.msg9
              );
            }
            this.disableNavigation = false;
          },
          (error) => {
            this.sameAs = this.registration.getSameAs();
            this.sameAsselected = false;
            this.disableNavigation = false;
            this.showErrorMessage(error, this.messagelabels.uploadDocuments.msg8);
          }
        );
        this.subscriptions.push(subs);
    }
  }

  /**
   *@description method to remove the POA document from users array when same as option has been selected.
   *
   * @memberof FileUploadComponent
   */
  removePOADocument() {
    //console.log("removePOADocument");
    this.userFiles = new FilesModel();
    //let i = 0;
    // if (this.users[0].files.documentsMetaData) {
    //   for (let file of this.users[0].files.documentsMetaData) {
    //     console.log(file);
    //     if (file.docCatCode == "POA") {
    //       this.users[0].files.documentsMetaData.splice(i, 1);
    //     }
    //     i++;
    //   }
    // }
    let allFiles = this.users[0].files.documentsMetaData;
    if (allFiles) {
      let updatedFiles = allFiles.filter(file => file.docCatCode !== "POA");
      //console.log(updatedFiles);
      this.users[0].files.documentsMetaData = updatedFiles;
    }
  }

  ifDisabled(category) {
    this.users[0].files[0].documentsMetaData.forEach((element) => {
      if ((element.docCatCode = category)) {
        return true;
      }
    });
    return false;
  }

  /**
   *@description method called when back button has been clicked.
   *
   * @memberof FileUploadComponent
   */
  onBack() {
    localStorage.setItem(appConstants.MODIFY_USER, "true");
    let url = Utils.getURL(this.router.url, "demographic");
    this.router.navigateByUrl(url + `/${this.preRegId}`);
  }

  /**
   *@description method called when next button has been clicked.
   *
   * @memberof FileUploadComponent
   */
  async onNext() {
    if (this.readOnlyMode) {
      localStorage.setItem("modifyDocument", "false");
      let url = Utils.getURL(this.router.url, "summary");
      this.router.navigateByUrl(url + `/${this.preRegId}/preview`);
    } else {
      //on next, update application status from "Application_Incomplete"
      //to "Pending_Appointment", if all required documents are uploaded
      this.uiFields.forEach((control) => {
        const controlId = control.id;
        if (this.userForm.controls[`${controlId}`]) {
          this.userForm.controls[`${controlId}`].markAsTouched();
        }
      });
      if (this.userForm.valid) {
        await this.updateApplicationStatus(appConstants.APPLICATION_STATUS_CODES.incomplete, 
          appConstants.APPLICATION_STATUS_CODES.pending);
        localStorage.setItem("modifyDocument", "false");
        let url = Utils.getURL(this.router.url, "summary");
        this.router.navigateByUrl(url + `/${this.preRegId}/preview`);
      }
    }
  }

  //eg: update the application status from "Application_Incomplete" to "Pending_Appointment"
  updateApplicationStatus = async (fromStatus: string, toStatus: string) => {
    return new Promise((resolve) => {
      this.dataStorageService.getApplicationStatus(this.users[0].preRegId).subscribe(
        (response) => {
          const applicationStatus = response["response"]["statusCode"];
          if (applicationStatus === fromStatus) {
            console.log(`updating application status from ${fromStatus} to ${toStatus}`);
            this.dataStorageService.updateApplicationStatus(
            this.users[0].preRegId, toStatus)
            .subscribe(
              (response) => {
                resolve(true);
              },
              (error) => {
                resolve(true);      
              }
            );
          }
          resolve(true);
        },
        (error) => {
          resolve(true);      
        }
      );
    });  
  }

  /**
   *@description method to preview the next file in the html page
   *
   * @param {number} fileIndex
   * @memberof FileUploadComponent
   */
  nextFile(fileIndex: number) {
    this.fileIndex = fileIndex + 1;
    this.viewFileByIndex(this.fileIndex);
  }

  /**
   *@description method to preview the previous file in the html page
   *
   * @param {number} fileIndex
   * @memberof FileUploadComponent
   */
  previousFile(fileIndex: number) {
    this.fileIndex = fileIndex - 1;
    this.viewFileByIndex(this.fileIndex);
  }

  /**
   * @description This is a dialoug box whenever an error comes from the server, it will appear.
   *
   * @private
   * @memberof FileUploadComponent
   */
   private showErrorMessage(error: any, customMsg?: string) {
    const titleOnError = this.errorlabels.errorLabel;
    let message = "";
    if (customMsg) {
      message = customMsg;
    } else {
      message = Utils.createErrorMessage(error, this.errorlabels, this.apiErrorCodes, this.config); 
    }  
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

  /**
   *@description method to set and display error message.
   *
   * @param {string} title
   * @param {string} message
   * @memberof FileUploadComponent
   */
  displayMessage(title: string, message: string) {
    const messageObj = {
      case: "MESSAGE",
      title: title,
      message: message,
    };
    this.openDialog(messageObj, "400px");
  }

  /**
   *@description method to open dialog box to show the error message
   *
   * @param {*} data
   * @param {*} width
   * @returns
   * @memberof FileUploadComponent
   */
  openDialog(data, width) {
    const dialogRef = this.dialog.open(DialougComponent, {
      width: width,
      data: data,
    });
    return dialogRef;
  }

  changeStatus(event, index: number) {
    this.LOD[index].selectedDocName = event.value;
  }

  changeRefNumber(event, index: number) {
    this.LOD[index].selectedRefNumber = event.target.value;
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((subscription) => subscription.unsubscribe());
  }
}

export interface DocumentUploadRequestDTO {
  docCatCode: string;
  docTypCode: string;
  langCode: string;
  refNumber: string;
}

export interface DocumentCategoryDTO {
  attribute: string;
  value: any;
}

export interface DocumentCategory {
  code: string;
  description: string;
  isActive: string;
  langCode: string;
  name: string;
  documentTypes?: DocumentCategory[];
  selectedDocName?: string;
  selectedRefNumber: string;
  labelName: string;
  required: boolean;
  containerStyle: {};
  headerStyle: {};
  id: string;
}

export interface Applicants {
  bookingMetadata?: string;
  preRegistrationId: string;
  demographicMetadata: DemographicMetaData;
  statusCode?: string;
}
export interface FullName {
  language: string;
  value: string;
}
export interface ProofOfAddress {
  docId: string;
  docName: string;
  docCatCode: string;
  docTypCode: string;
  docFileFormat?: string;
  refNumber: string;
}

export interface DemographicMetaData {
  fullName?: FullName[];
  postalCode?: string;
  proofOfAddress?: ProofOfAddress;
}

export interface SelectedDocuments {
  docCatCode: string;
  docTypeCode: string;
}
