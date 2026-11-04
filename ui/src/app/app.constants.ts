export const VERSION = '1.0';
export const RESPONSE = 'response';
export const METADATA = 'documentsMetaData';
export const ERROR = 'error';
export const NESTED_ERROR = 'errors';
export const ERROR_CODE = 'errorCode';
export const API_ERROR_CODES = 'API_ERROR_CODES';
export const DIALOG = 'dialog';
export const PRE_REGISTRATION_ID = 'pre_registration_id';
export const APPENDER = '/';
export const IDSchemaVersionLabel = 'IDSchemaVersion';

export const IDS = {
  newUser: 'mosip.pre-registration.demographic.create',
  newLostUin: 'mosip.pre-registration.lostuin.create',
  newUpdateRegistration: 'mosip.pre-registration.updateregistration.create',
  updateUser: 'mosip.pre-registration.demographic.update',
  transliteration: 'mosip.pre-registration.transliteration.transliterate',
  notification: 'mosip.pre-registration.notification.notify',
  cancelAppointment: 'mosip.pre-registration.appointment.cancel',
  booking: 'mosip.pre-registration.booking.book',
  qrCode: 'mosip.pre-registration.qrcode.generate',
  sendOtp: 'mosip.pre-registration.login.sendotp',
  validateOtp: 'mosip.pre-registration.login.useridotp',
  documentUpload: 'mosip.pre-registration.document.upload',
  applicantTypeId: 'mosip.applicanttype.fetch',
  captchaId: 'mosip.pre-registration.captcha.id.validate'
};

export const APPEND_URL = {
  config: 'config',
  send_otp: 'sendOtp',
  login: 'validateOtp',
  logout: 'invalidateToken',
  location_metadata: 'locations/locationhierarchy/',
  location_immediate_children: 'locations/immediatechildren/',
  applicants: 'applications/prereg',
  allApplicants: 'applications',
  applicantsLostUIn: 'applications/lostuin',
  applicantsUpdateRegistration: 'applications/updateregistration',
  location: '/masterdata/',
  gender: '/masterdata/gendertypes',
  resident: '/masterdata/individualtypes',
  transliteration: 'transliteration/transliterate',
  //applicantType: 'v1/applicanttype/',
  applicantType: '/masterdata/',
  validDocument: 'applicanttype/',
  getApplicantType: 'getApplicantType',
  post_document: 'documents/',
  document: 'documents/preregistration/',
  updateDocRefId: 'documents/document/',
  document_copy: 'document/documents/copy',
  nearby_registration_centers: 'getcoordinatespecificregistrationcenters/',
  registration_centers_by_name: 'registrationcenters/',
  booking_appointment: 'applications/appointment',
  booking_availability: 'applications/appointment/slots/availability/',
  delete_prereg: 'applications/prereg/',
  delete_lostuin: 'applications/lostuin/',
  delete_updateregistration: 'applications/updateregistration/',
  qr_code: 'qrCode/generate',
  notification: 'notification/v2',
  send_notification: 'notification/notify',
  master_data: '/masterdata/',
  auth: 'login/',
  cancelAppointment: 'applications/appointment/',
  captcha:'captcha/validatecaptcha'
};

export const PARAMS_KEYS = {
  getUsers: 'user_id',
  getUser: PRE_REGISTRATION_ID,
  deleteUser: PRE_REGISTRATION_ID,
  locationHierarchyName: 'hierarchyName',
  getDocument: PRE_REGISTRATION_ID,
  getDocumentCategories: 'languages',
  preRegistrationId: 'preRegistrationId',
  deleteFile: 'documentId',
  getAvailabilityData: 'registration_center_id',
  catCode: 'catCode',
  sourcePrId: 'sourcePrId',
  POA: 'POA',
  docRefId: 'refNumber'
};

export const ERROR_CODES = {
  noApplicantEnrolled: 'PRG_PAM_APP_005',
  noLocationAvailable: 'KER-MSD-026',
  userBlocked: 'PRG_PAM_LGN_013',
  invalidPin: 'KER-IOV-004',
  tokenExpired: 'KER-ATH-401',
  authenticationFailed: 'KER-401',
  invalidateToken: 'PRG_PAM_LGN_003',
  slotNotAvailable: 'PRG_BOOK_RCI_002',
  timeExpired: 'PRG_BOOK_RCI_026',
  otpAttemptExceeded : 'PRE-REG-OTP-409'
};

export const CONFIG_KEYS = {
  mosip_notification_type: 'mosip.notificationtype',
  mosip_default_location: 'mosip.kernel.idobjectvalidator.masterdata.locations.locationNotAvailable',
  mosip_country_code: 'mosip.country.code',
  preregistration_nearby_centers: 'preregistration.nearby.centers',
  preregistration_timespan_rebook: 'preregistration.timespan.rebook',
  mosip_login_mode: 'mosip.login.mode',
  preregistration_identity_name : 'preregistration.identity.name',
  mosip_regex_email: 'mosip.id.validation.identity.email',
  mosip_regex_phone: 'mosip.id.validation.identity.phone',
  mosip_left_to_right_orientation: 'mosip.left_to_right_orientation',
  mosip_kernel_otp_expiry_time: 'mosip.kernel.otp.expiry-time',
  mosip_kernel_otp_default_length: 'mosip.kernel.otp.default-length',
  preregistration_recommended_centers_locCode: 'preregistration.recommended.centers.locCode',
  preregistration_availability_noOfDays: 'preregistration.availability.noOfDays',
  mosip_regex_referenceIdentityNumber: 'mosip.id.validation.identity.referenceIdentityNumber',
  mosip_regex_postalCode: 'mosip.id.validation.identity.postalCode',
  mosip_regex_DOB: 'mosip.id.validation.identity.dateOfBirth',
  mosip_default_dob_day: 'mosip.default.dob.day',
  mosip_default_dob_month: 'mosip.default.dob.month',
  preregistration_address_length: 'mosip.id.validation.identity.addressLine1.[*].value',
  preregistration_fullname_length: 'mosip.id.validation.identity.fullName.[*].value',
  mosip_id_validation_identity_age: 'mosip.id.validation.identity.age',
  mosip_preregistration_auto_logout_idle: 'mosip.preregistration.auto.logout.idle',
  mosip_preregistration_auto_logout_timeout: 'mosip.preregistration.auto.logout.timeout',
  mosip_preregistration_auto_logout_ping: 'mosip.preregistration.auto.logout.ping',
  preregistration_document_alllowe_files: 'preregistration.documentupload.allowed.file.type',
  preregistration_document_alllowe_file_size: 'preregistration.documentupload.allowed.file.size',
  preregistration_document_alllowe_file_name_lenght: 'preregistration.documentupload.allowed.file.nameLength',
  google_recaptcha_site_key: 'google.recaptcha.site.key',
  mosip_adult_age:'mosip.adult.age',
  preregistration_preview_fields:'preregistration.preview.fields',
  mosip_mandatory_languages: 'mosip.mandatory-languages',
  mosip_optional_languages: 'mosip.optional-languages',
  mosip_min_languages_count: 'mosip.min-languages.count',
  mosip_max_languages_count: 'mosip.max-languages.count',
  preregistration_contact_email: 'preregistration.contact.email',
  preregistration_contact_phone: 'preregistration.contact.phone',
};

export const DASHBOARD_RESPONSE_KEYS = {
  bookingRegistrationDTO: {
    dto: 'bookingMetadata',
    regDate: 'appointment_date',
    time_slot_from: 'time_slot_from',
    time_slot_to: 'time_slot_to'
  },
  applicant: {
    preId: 'preRegistrationId',
    fullname: 'fullName',
    statusCode: 'statusCode',
    postalCode: 'postalCode',
    basicDetails: 'basicDetails',
    demographicMetadata: 'demographicMetadata'
  },
  allApplicationsResp: {
    applicationId: 'applicationId',
    appointmentDate: 'appointmentDate',
    slotFromTime: 'slotFromTime',
    slotToTime: 'slotToTime',
    registrationCenterId: 'registrationCenterId',
    allApplications: 'allApplications',
    bookingStatusCode: 'bookingStatusCode',
    bookingType: 'bookingType'
  }
};

export const DEMOGRAPHIC_RESPONSE_KEYS = {
  locations: 'locations',
  preRegistrationId: 'preRegistrationId',
  genderTypes: 'genderType',
  residentTypes: 'individualTypes'
};

export const APPLICATION_STATUS_CODES = {
  incomplete: 'Application_Incomplete',
  pending: 'Pending_Appointment',
  prefetched: 'Prefetched',
  booked: 'Booked',
  expired: 'Expired',
  cancelled: 'Cancelled'
};

export const APPLICANT_TYPE_ATTRIBUTES = {
  // individualTypeCode: 'individualTypeCode',
  // dateofbirth: 'dateofbirth',
  // genderCode: 'genderCode',
  biometricAvailable: 'biometricAvailable'
};

export const notificationDtoKeys = {
  notificationDto: 'NotificationRequestDTO',
  langCode: 'langCode',
  file: 'attachment'
};


export const DOCUMENT_UPLOAD_REQUEST_DOCUMENT_KEY = 'file';
export const DOCUMENT_UPLOAD_REQUEST_DTO_KEY = 'Document request';

export const PREVIEW_DATA_APPEND_URL = 'demographic/v0.1/applicationData';

export const MONTHS = ['', 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

export const controlTypeGender = 'gender';
export const controlTypeResidenceStatus = 'residenceStatus';

export const DATA_CAPTURE_LANGUAGES = "dataCaptureLanguages";
export const DATA_CAPTURE_LANGUAGE_LABELS = "dataCaptureLanguagesLabels";
export const LANGUAGE_CODE_VALUES = "languageCodeValue";
export const MODIFY_USER = "modifyUser";
export const NEW_APPLICANT = "newApplicant";
export const MODIFY_USER_FROM_PREVIEW = "modifyUserFromPreview";
export const NEW_APPLICANT_FROM_PREVIEW = "addingUserFromPreview";

export const FORCE_LOGOUT = "forceLogout";
export const FORCE_LOGOUT_YES = "yes";

export const FIELD_TYPE_STRING = "string";
export const FIELD_TYPE_SIMPLE_TYPE = "simpleType";

export const APPLICATION_TYPE = "applicationType";
export const NEW_PREREGISTRATION = "NEW_PREREGISTRATION";
export const LOST_FORGOTTEN_UIN = "LOST_FORGOTTEN_UIN";
export const UPDATE_REGISTRATION = "UPDATE_REGISTRATION";
