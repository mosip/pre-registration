import { Injectable } from "@angular/core";
import { HttpClient, HttpParams, HttpHeaders } from "@angular/common/http";

import * as appConstants from "../../app.constants";
import { AppConfigService } from "../../app-config.service";
import { Applicant } from "../../shared/models/dashboard-model/dashboard.modal";
import { ConfigService } from "./config.service";
import { RequestModel } from "src/app/shared/models/request-model/RequestModel";
import { AuditModel } from "src/app/shared/models/demographic-model/audit.model";

/**
 * @description This class is responsible for sending or receiving data to the service.
 *
 * @author Shashank Agrawal
 * @export
 * @class DataStorageService
 */
@Injectable({
  providedIn: "root",
})
export class DataStorageService {
  /**
   * @description Creates an instance of DataStorageService.
   * @see HttpClient
   * @param {HttpClient} httpClient
   * @param {AppConfigService} appConfigService
   * @param {ConfigService} configService
   * @memberof DataStorageService
   */
  constructor(
    private httpClient: HttpClient,
    private appConfigService: AppConfigService,
    private configService: ConfigService
  ) {}

  BASE_URL = this.appConfigService.getConfig()["BASE_URL"];
  PRE_REG_URL = this.appConfigService.getConfig()["PRE_REG_URL"];

  getI18NLanguageFiles(langCode: string) {
    return this.httpClient.get(`./assets/i18n/${langCode}.json`);
  }

  getUsers(userId: string) {
    let url =
      this.BASE_URL + this.PRE_REG_URL + appConstants.APPEND_URL.applicants;
    return this.httpClient.get<Applicant[]>(url);
  }

  getApplicationDetails(applicationId: string) {
    let url =
      this.BASE_URL + this.PRE_REG_URL + "applications/" + applicationId;
    return this.httpClient.get<Applicant[]>(url);
  }

  /**
   * @description This method returns the user details for the given pre-registration id.
   *
   * @param {string} preRegId - pre-registartion-id
   * @returns an `Observable` of the body as an `Object`
   * @memberof DataStorageService
   */
  getUser(preRegId: string) {
    let url =
      this.BASE_URL +
      this.PRE_REG_URL +
      appConstants.APPEND_URL.applicants +
      appConstants.APPENDER +
      preRegId;
    return this.httpClient.get(url);
  }

  /**
   * @description This methos returns the list of available genders
   *
   *
   * @returns an `Observable` of the body as an `Object`
   * @memberof DataStorageService
   */
  getGenderDetails() {
    const url =
      this.BASE_URL +
      this.PRE_REG_URL +
      "proxy" +
      appConstants.APPEND_URL.gender;
    return this.httpClient.get(url);
  }

  /**
   * @description This methos returns the list of available genders
   *
   *
   * @returns an `Observable` of the body as an `Object`
   * @memberof DataStorageService
   */
  getResidentDetails() {
    const url =
      this.BASE_URL +
      this.PRE_REG_URL +
      "proxy" +
      appConstants.APPEND_URL.resident;
    return this.httpClient.get(url);
  }

  /**
   * @description This method is responsible for doing the transliteration for a given word.
   *
   * @param {*} request
   * @returns an `Observable` of the body as an `Object`
   * @memberof DataStorageService
   */
  getTransliteration(request: any) {
    const obj = new RequestModel(appConstants.IDS.transliteration, request);
    const url =
      this.BASE_URL +
      this.PRE_REG_URL +
      appConstants.APPEND_URL.transliteration;
    return this.httpClient.post(url, obj);
  }

  getUserDocuments(preRegId) {
    return this.httpClient.get(
      this.BASE_URL +
        this.PRE_REG_URL +
        appConstants.APPEND_URL.document +
        preRegId
    );
  }

  /**
   * @description This method adds the user
   *
   * @param {*} identity `Object`
   * @returns an `Observable` of the body as an `Object`
   * @memberof DataStorageService
   */
  addUser(identity: any) {
    const obj = new RequestModel(appConstants.IDS.newUser, identity);
    let url =
      this.BASE_URL + this.PRE_REG_URL + appConstants.APPEND_URL.applicants;
    return this.httpClient.post(url, obj);
  }

  updateUser(identity: any, preRegId: string) {
    let url =
      this.BASE_URL +
      this.PRE_REG_URL +
      appConstants.APPEND_URL.applicants +
      appConstants.APPENDER +
      preRegId;
    const obj = new RequestModel(appConstants.IDS.updateUser, identity);
    return this.httpClient.put(url, obj);
  }

  sendFile(formdata: FormData, preRegId: string) {
    return this.httpClient.post(
      this.BASE_URL +
        this.PRE_REG_URL +
        appConstants.APPEND_URL.post_document +
        preRegId,
      formdata
    );
  }

  deletePreRegistration(preId: string) {
    return this.httpClient.delete(
      this.BASE_URL +
        this.PRE_REG_URL +
        appConstants.APPEND_URL.delete_prereg +
        preId
    );
  }

  deleteLostUin(appId: string) {
    return this.httpClient.delete(
      this.BASE_URL +
        this.PRE_REG_URL +
        appConstants.APPEND_URL.delete_lostuin +
        appId
    );
  }

  deleteUpdateRegistration(appId: string) {
    return this.httpClient.delete(
      this.BASE_URL +
        this.PRE_REG_URL +
        appConstants.APPEND_URL.delete_updateregistration +
        appId
    );
  }

  cancelAppointment(data: RequestModel, preRegId: string) {
    return this.httpClient.put(
      this.BASE_URL +
        this.PRE_REG_URL +
        appConstants.APPEND_URL.cancelAppointment +
        preRegId,
      data
    );
  }

  getNearbyRegistrationCenters(coords: any) {
    return this.httpClient.get(
      this.BASE_URL +
        this.PRE_REG_URL +
        "proxy" +
        appConstants.APPEND_URL.master_data +
        appConstants.APPEND_URL.nearby_registration_centers +
        localStorage.getItem("langCode") +
        "/" +
        coords.longitude +
        "/" +
        coords.latitude +
        "/" +
        this.configService.getConfigByKey(
          appConstants.CONFIG_KEYS.preregistration_nearby_centers
        )
    );
  }

  getRegistrationCentersByName(locType: string, text: string) {
    return this.httpClient.get(
      this.BASE_URL +
        this.PRE_REG_URL +
        "proxy" +
        appConstants.APPEND_URL.master_data +
        appConstants.APPEND_URL.registration_centers_by_name +
        localStorage.getItem("langCode") +
        "/" +
        locType +
        "/" +
        text
    );
  }

  getRegistrationCentersByNamePageWise(
    locType: string,
    text: string,
    pageNumber: number,
    pageSize: number
  ) {
    let url =
      this.BASE_URL +
      this.PRE_REG_URL +
      "proxy" +
      appConstants.APPEND_URL.master_data +
      appConstants.APPEND_URL.registration_centers_by_name +
      "page/" +
      localStorage.getItem("langCode") +
      "/" +
      locType +
      "/" +
      encodeURIComponent(text) +
      "?" +
      "pageNumber=" +
      pageNumber +
      "&pageSize=" +
      pageSize +
      "&orderBy=desc&sortBy=createdDateTime";
    return this.httpClient.get(url);
  }

  getLocationTypeData() {
    return this.httpClient.get(
      this.BASE_URL +
        this.PRE_REG_URL +
        "proxy" +
        appConstants.APPEND_URL.master_data +
        "locationHierarchyLevels/" +
        localStorage.getItem("langCode")
    );
  }

  getLocationInfoForLocCode(locCode: string, langCode: string) {
    let url =
      this.BASE_URL +
      this.PRE_REG_URL +
      "proxy" +
      appConstants.APPEND_URL.master_data +
      "locations/info/" +
      locCode +
      "/" +
      langCode;
    return this.httpClient.get(url);
  }

  getLocationsHierarchyByLangCode(langCode: string, locCode: string) {
    return this.httpClient.get(
      this.BASE_URL +
        this.PRE_REG_URL +
        "proxy" +
        appConstants.APPEND_URL.master_data +
        "locations/" +
        locCode +
        "/" +
        langCode
    );
  }

  getAvailabilityData(registrationCenterId) {
    return this.httpClient.get(
      this.BASE_URL +
        this.PRE_REG_URL +
        appConstants.APPEND_URL.booking_availability +
        registrationCenterId
    );
  }

  makeBooking(request: RequestModel) {
    return this.httpClient.post(
      this.BASE_URL +
        this.PRE_REG_URL +
        appConstants.APPEND_URL.booking_appointment,
      request
    );
  }

  /**
   * @description This method return the list of list of countries.
   *
   * @return String
   * @memberof DataStorageService
   */
  getLocationMetadataHirearchy() {
    return this.configService.getConfigByKey(
      appConstants.CONFIG_KEYS.mosip_country_code
    );
  }

  /**
   * @description This method return the below list of location hierarchy in specified language for the given location hierarchy and langugae code.
   *
   * @param {string} lang
   * @param {string} location
   * @return an `Observable` of the body as an `Object`
   * @memberof DataStorageService
   */
  getLocationImmediateHierearchy(lang: string, location: string) {
    const url =
      this.BASE_URL +
      this.PRE_REG_URL +
      "proxy" +
      appConstants.APPEND_URL.master_data +
      appConstants.APPEND_URL.location_immediate_children +
      location +
      appConstants.APPENDER +
      lang;
    return this.httpClient.get(url);
  }

  deleteFile(documentId, preRegId) {
    return this.httpClient.delete(
      this.BASE_URL +
        this.PRE_REG_URL +
        appConstants.APPEND_URL.post_document +
        documentId,
      {
        params: new HttpParams().append(
          appConstants.PARAMS_KEYS.preRegistrationId,
          preRegId
        ),
      }
    );
  }

  getSecondaryLanguageLabels(langCode: string) {
    return this.httpClient.get(`./assets/i18n/${langCode}.json`);
  }

  copyDocument(sourceId: string, destinationId: string) {
    const url =
      this.BASE_URL +
      this.PRE_REG_URL +
      appConstants.APPEND_URL.post_document +
      destinationId +
      "?catCode=" +
      appConstants.PARAMS_KEYS.POA +
      "&sourcePreId=" +
      sourceId;

    return this.httpClient.put(url, {
      observe: "body",
      responseType: "json",
    });
  }

  getFileData(fileDocumentId, preId) {
    return this.httpClient.get(
      this.BASE_URL +
        this.PRE_REG_URL +
        appConstants.APPEND_URL.post_document +
        fileDocumentId,
      {
        params: new HttpParams().append(
          appConstants.PARAMS_KEYS.preRegistrationId,
          preId
        ),
      }
    );
  }

  generateQRCode(data: string) {
    const obj = new RequestModel(appConstants.IDS.qrCode, data);
    return this.httpClient.post(
      this.BASE_URL + this.PRE_REG_URL + appConstants.APPEND_URL.qr_code,
      obj
    );
  }

  sendNotification(data: FormData) {
    return this.httpClient.post(
      this.BASE_URL + this.PRE_REG_URL + appConstants.APPEND_URL.notification,
      data
    );
  }
  
  sendCancelNotification(data: FormData) {
    return this.httpClient.post(
      this.BASE_URL + this.PRE_REG_URL + appConstants.APPEND_URL.send_notification,
      data
    );
  }

  getLocationOnLocationCodeAndLangCode(locationCode, langCode) {
    const url =
      this.BASE_URL +
      this.PRE_REG_URL +
      "proxy" +
      appConstants.APPEND_URL.master_data +
      "locations/" +
      locationCode +
      "/" +
      langCode;
    return this.httpClient.get(url);
  }

  recommendedCenters(
    langCode: string,
    locationHierarchyCode: number,
    data: string[]
  ) {
    let url =
      this.BASE_URL +
      this.PRE_REG_URL +
      "proxy" +
      appConstants.APPEND_URL.master_data +
      "registrationcenters/" +
      langCode +
      "/" +
      locationHierarchyCode +
      "/names?";
    data.forEach((name) => {
      url += "name=" + name;
      if (data.indexOf(name) !== data.length - 1) {
        url += "&";
      }
    });
    if (url.charAt(url.length - 1) === "&") {
      url = url.substring(0, url.length - 1);
    }
    return this.httpClient.get(url);
  }

  getRegistrationCenterByIdAndLangCode(id: string, langCode: string) {
    const url =
      this.BASE_URL +
      this.PRE_REG_URL +
      "proxy" +
      appConstants.APPEND_URL.master_data +
      "registrationcenters/" +
      id +
      "/" +
      langCode;
    return this.httpClient.get(url);
  }

  getGuidelineTemplate(templateType: string) {
    const url =
      this.BASE_URL +
      this.PRE_REG_URL +
      "proxy" +
      appConstants.APPEND_URL.master_data +
      "templates/templatetypecodes" +
      "/" +
      templateType;
    localStorage.setItem("consentUrl", url);
    return this.httpClient.get(url);
  }

  getApplicantType(docuemntCategoryDto) {
    return this.httpClient.post(
      this.BASE_URL +
        this.PRE_REG_URL +
        "proxy" +
        appConstants.APPEND_URL.applicantType +
        appConstants.APPEND_URL.getApplicantType,
      docuemntCategoryDto
    );
  }

  getDocumentCategories(applicantCode) {
    const APPLICANT_VALID_DOCUMENTS_URL =
      this.BASE_URL +
      this.PRE_REG_URL +
      "proxy" +
      appConstants.APPEND_URL.location +
      appConstants.APPEND_URL.validDocument +
      applicantCode +
      "/languages";
    return this.httpClient.get(APPLICANT_VALID_DOCUMENTS_URL, {
      params: new HttpParams().append(
        appConstants.PARAMS_KEYS.getDocumentCategories,
        localStorage.getItem("langCode")
      ),
    });
  }
  
  getDocumentCategoriesByLang(applicantCode, langCode) {
    const APPLICANT_VALID_DOCUMENTS_URL =
      this.BASE_URL + this.PRE_REG_URL + 'proxy' +
      appConstants.APPEND_URL.location +
      appConstants.APPEND_URL.validDocument +
      applicantCode +
      "/languages"; 
    return this.httpClient.get(APPLICANT_VALID_DOCUMENTS_URL, {
      params: new HttpParams().append(
        appConstants.PARAMS_KEYS.getDocumentCategories,
        langCode
      ),
    });
  }

  getConfig() {
    const url =
      this.BASE_URL +
      this.PRE_REG_URL +
      appConstants.APPEND_URL.auth +
      appConstants.APPEND_URL.config;
    return this.httpClient.get(url);
  }

  sendOtp(userId: string, langCode: string) {
    const req = {
      langCode: langCode,
      userId: userId,
    };

    const obj = new RequestModel(appConstants.IDS.sendOtp, req);

    const url =
      this.BASE_URL +
      this.PRE_REG_URL +
      appConstants.APPEND_URL.auth +
      appConstants.APPEND_URL.send_otp +
      "/" +
      "langcode";
    return this.httpClient.post(url, obj);
  }

  sendOtpWithCaptcha(userId: string, langCode: string, captchaToken: string) {
    const req = {
      langCode: langCode,
      userId: userId,
      captchaToken: captchaToken,
    };

    const obj = new RequestModel(appConstants.IDS.sendOtp, req);

    const url =
      this.BASE_URL +
      this.PRE_REG_URL +
      appConstants.APPEND_URL.auth +
      "sendOtpWithCaptcha";
    return this.httpClient.post(url, obj);
  }

  verifyOtp(userId: string, otp: string) {
    const request = {
      otp: otp,
      userId: userId,
    };

    const obj = new RequestModel(appConstants.IDS.validateOtp, request);

    const url =
      this.BASE_URL +
      this.PRE_REG_URL +
      appConstants.APPEND_URL.auth +
      appConstants.APPEND_URL.login;
    return this.httpClient.post(url, obj);
  }

  /**
   * @description This is to get the list of working days for a given registration center id.
   *
   * @param {string} registartionCenterId
   * @param {string} langCode
   * @returns
   * @memberof DataStorageService
   */
  getWorkingDays(registartionCenterId: string, langCode: string) {
    const url =
      this.BASE_URL +
      this.PRE_REG_URL +
      "proxy" +
      appConstants.APPEND_URL.master_data +
      "workingdays/" +
      registartionCenterId +
      "/" +
      langCode;
    return this.httpClient.get(url);
  }

  /**
   * @description This method is responsible to logout the user and invalidate the token.
   *
   * @returns an `Observable` of the body as an `Object`
   * @memberof DataStorageService
   */
  onLogout() {
    const url =
      this.BASE_URL +
      this.PRE_REG_URL +
      appConstants.APPEND_URL.auth +
      appConstants.APPEND_URL.logout;
    return this.httpClient.post(url, "");
  }

  verifyGCaptcha(captcha) {
    const url =
      this.BASE_URL + this.PRE_REG_URL + appConstants.APPEND_URL.captcha;
    return this.httpClient.post(url, captcha);
  }

  getIdentityJson() {
    let url = this.BASE_URL + this.PRE_REG_URL + `uispec/latest`;
    return this.httpClient.get(url);
  }

  getRegistrationCentersById(regCenterId, langCode: string) {
    return this.httpClient.get(
      this.BASE_URL +
        this.PRE_REG_URL +
        "proxy" +
        appConstants.APPEND_URL.master_data +
        appConstants.APPEND_URL.registration_centers_by_name +
        regCenterId +
        "/" +
        langCode
    );
  }

  getAppointmentDetails(preRegId) {
    return this.httpClient.get(
      this.BASE_URL +
        this.PRE_REG_URL +
        appConstants.APPEND_URL.booking_appointment +
        "/" +
        preRegId
    );
  }

  getDynamicFieldsandValues(langCode) {
    const url =
      this.BASE_URL +
      this.PRE_REG_URL +
      "proxy" +
      appConstants.APPEND_URL.master_data +
      `dynamicfields?langCode=${langCode}`;
    return this.httpClient.get(url);
  }

  getDynamicFieldsandValuesForAllLang(pageNumber: string) {
    const url =
      this.BASE_URL +
      this.PRE_REG_URL +
      "proxy" +
      appConstants.APPEND_URL.master_data +
      `dynamicfields?pageNumber=${pageNumber}&pageSize=10`;
    return this.httpClient.get(url);
  }

  updateDocRefId(fileDocumentId, preId, refNumber) {
    const url =
      `${this.BASE_URL}${this.PRE_REG_URL}` +
      `${appConstants.APPEND_URL.updateDocRefId}${fileDocumentId}` +
      `?${appConstants.PARAMS_KEYS.preRegistrationId}=${preId}` +
      `&${appConstants.PARAMS_KEYS.docRefId}=${refNumber}`;
    return this.httpClient.put(url, {});
  }

  logAudit(auditRequest: AuditModel) {
    let request = new RequestModel("", auditRequest);
    const url = this.BASE_URL + this.PRE_REG_URL + "logAudit";
    return this.httpClient.post(url, request);
  }

  updateApplicationStatus(prid: string, statusCode: string) {
    const requesturl =
      this.BASE_URL +
      this.PRE_REG_URL +
      `applications/prereg/status/${prid}?statusCode=${statusCode}`;
    return this.httpClient.put(requesturl, {});
  }

  getApplicationStatus(prid: string) {
    const requesturl =
      this.BASE_URL + this.PRE_REG_URL + `applications/prereg/status/${prid}`;
    return this.httpClient.get(requesturl);
  }

  getAllApplications(userId: string) {
    let url =
      this.BASE_URL + this.PRE_REG_URL + appConstants.APPEND_URL.allApplicants;
    return this.httpClient.get<Applicant[]>(url);
  }

  addlostUin(request: any) {
    const obj = new RequestModel(appConstants.IDS.newLostUin, request);
    let url =
      this.BASE_URL + this.PRE_REG_URL + appConstants.APPEND_URL.applicantsLostUIn;
    return this.httpClient.post(url, obj);
  }

  addUpdateRegistration(request: any) {
    const obj = new RequestModel(appConstants.IDS.newUpdateRegistration, request);
    let url =
      this.BASE_URL + this.PRE_REG_URL + appConstants.APPEND_URL.applicantsUpdateRegistration;
    return this.httpClient.post(url, obj);
  }
}
