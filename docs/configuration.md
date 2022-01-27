# Pre-registration Configuration Guide

## Property files
The configuration of the services is controlled by the following property files:
* [`application-default.propertes`](https://github.com/mosip/mosip-config/blob/1.2.0_v3/application-default.properties)
* [`pre-registration-default.propertes`](https://github.com/mosip/mosip-config/blob/1.2.0_v3/pre-registration-default.properties)


## Few of Important configurations
|Property|Description|
|---|---|
|`preregistration.document.extention`|Document formats|
|`max.file.size`|Document max upload size|
|`mosip.preregistration.document.scan`|To enable Virus Scan for document being uploaded|
|`preregistration.availability.sync`|Number  of days for which sync is done on the Booking Slots generated for a registration centre|
|`preregistration.availability.noOfDays`|Number of days for which booking slots will be available.|
|`preregistration.booking.offset`|Gap between the date of booking and the first appointment date to be shown|
|`preregistration.config.identityjson`|Mapping file for Pre-registration & ID Object|
|`preregistration.identity.name`|The field name from ID Schema which indicates user's name.|
|`preregistration.notification.nameFormat`|The field name from ID Schema which indicates user's name. Used in notifications.|
|`preregistration.recommended.centers.locCode`|The value depicts the location hierarchy code of the hierarchy based on which the recommended centers is loaded|
|`preregistration.contact.email`|Contact details of the Support for Pre-registration portal.|
|`preregistration.contact.phone`|Contact details of the Support for Pre-registration portal.|

## PreReg form customization
The fields shown in the PreReg UI form can be configured using UI specification JSON loaded in `ui_spec` table of `mosip_master` DB (refer to `commons` repo).  The UI specification must have fields from ID Schema.  Sample UI spec JSON is given below
```
{
	"identity": {
		"identity": [
			{
				"id": "IDSchemaVersion",
				"description": "ID Schema Version",
				"type": "number",
				"controlType": null,
				"fieldType": "default",
				"inputRequired": false,
				"validators": [],
				"required": true
			},
			{
				"id": "fullName",
				"description": "Enter Full Name",
				"labelName": {
					"eng": "Full Name",
					"ara": "الاسم الكامل",
					"fra": "Nom complet"
				},
				"controlType": "textbox",
				"inputRequired": true,
				"fieldType": "default",
				"type": "simpleType",
				"validators": [
					{
						"type": "regex",
						"validator": "^(?=.{0,50}$).*",
						"arguments": []
					}
				],
				"required": true,
				"transliteration": true
			},
			{
				"id": "dateOfBirth",
				"description": "Enter DOB",
				"labelName": {
					"eng": "Date Of Birth",
					"ara": "تاريخ الولادة",
					"fra": "Date de naissance"
				},
				"controlType": "ageDate",
				"inputRequired": true,
				"fieldType": "default",
				"type": "string",
				"validators": [],
				"required": true
			},
			{
				"id": "gender",
				"description": "Enter Gender",
				"labelName": {
					"eng": "Gender",
					"ara": "جنس",
					"fra": "Le genre"
				},
				"controlType": "dropdown",
				"inputRequired": true,
				"fieldType": "dynamic",
				"subType": "gender",
				"type": "simpleType",
				"validators": [],
				"required": true
			},
			{
				"id": "residenceStatus",
				"description": "Residence status",
				"labelName": {
					"eng": "Residence Status",
					"ara": "حالة الإقامة",
					"fra": "Statut de résidence"
				},
				"controlType": "dropdown",
				"inputRequired": true,
				"fieldType": "dynamic",
				"subType": "residenceStatus",
				"type": "simpleType",
				"validators": [],
				"required": true
			},
			{
				"id": "addressLine1",
				"description": "addressLine1",
				"labelName": {
					"eng": "Address Line1",
					"ara": "العنوان السطر 1",
					"fra": "Adresse 1"
				},
				"controlType": "textbox",
				"inputRequired": true,
				"fieldType": "default",
				"type": "simpleType",
				"validators": [
					{
						"type": "regex",
						"validator": "^(?=.{0,50}$).*",
						"arguments": []
					}
				],
				"required": true,
				"transliteration": true
			},
			{
				"id": "addressLine2",
				"description": "addressLine2",
				"labelName": {
					"eng": "Address Line2",
					"ara": "العنوان السطر 2",
					"fra": "Adresse 2"
				},
				"controlType": "textbox",
				"inputRequired": true,
				"fieldType": "default",
				"type": "simpleType",
				"validators": [
					{
						"type": "regex",
						"validator": "^(?=.{0,50}$).*",
						"arguments": []
					}
				],
				"required": false,
				"transliteration": true
			},
			{
				"id": "addressLine3",
				"description": "addressLine3",
				"labelName": {
					"eng": "Address Line3",
					"ara": "العنوان السطر 3",
					"fra": "Adresse 3"
				},
				"controlType": "textbox",
				"inputRequired": true,
				"fieldType": "default",
				"type": "simpleType",
				"validators": [
					{
						"type": "regex",
						"validator": "^(?=.{0,50}$).*",
						"arguments": []
					}
				],
				"required": false,
				"transliteration": true
			},
			{
				"id": "region",
				"description": "region",
				"labelName": {
					"eng": "Region",
					"ara": "منطقة",
					"fra": "Région"
				},
				"controlType": "dropdown",
				"inputRequired": true,
				"fieldType": "default",
				"type": "simpleType",
				"validators": [
					{
						"type": "regex",
						"validator": "^(?=.{0,50}$).*",
						"arguments": []
					}
				],
				"parentLocCode": "MOR",
				"locationHierarchyLevel": 1,
				"required": true
			},
			{
				"id": "province",
				"description": "province",
				"labelName": {
					"eng": "Province",
					"ara": "المحافظة",
					"fra": "Province"
				},
				"controlType": "dropdown",
				"inputRequired": true,
				"fieldType": "default",
				"type": "simpleType",
				"validators": [
					{
						"type": "regex",
						"validator": "^(?=.{0,50}$).*",
						"arguments": []
					}
				],
				"locationHierarchyLevel": 2,
				"required": true
			},
			{
				"id": "city",
				"description": "city",
				"labelName": {
					"eng": "City",
					"ara": "مدينة",
					"fra": "Ville"
				},
				"controlType": "dropdown",
				"inputRequired": true,
				"fieldType": "default",
				"type": "simpleType",
				"validators": [
					{
						"type": "regex",
						"validator": "^(?=.{0,50}$).*",
						"arguments": []
					}
				],
				"locationHierarchyLevel": 3,
				"required": true
			},
			{
				"id": "zone",
				"description": "zone",
				"labelName": {
					"eng": "Zone",
					"ara": "منطقة",
					"fra": "Zone"
				},
				"controlType": "dropdown",
				"inputRequired": true,
				"fieldType": "default",
				"type": "simpleType",
				"validators": [],
				"locationHierarchyLevel": 4,
				"required": true
			},
			{
				"id": "postalCode",
				"description": "postalCode",
				"labelName": {
					"eng": "Postal Code",
					"ara": "الكود البريدى",
					"fra": "code postal"
				},
				"controlType": "dropdown",
				"inputRequired": true,
				"fieldType": "default",
				"type": "string",
				"validators": [
					{
						"type": "regex",
						"validator": "^[(?i)A-Z0-9]{5}$|^NA$",
						"arguments": []
					}
				],
				"locationHierarchyLevel": 5,
				"required": true
			},
			{
				"id": "phone",
				"description": "phone",
				"labelName": {
					"eng": "Phone",
					"ara": "هاتف",
					"fra": "Téléphone"
				},
				"controlType": "textbox",
				"inputRequired": true,
				"fieldType": "default",
				"type": "string",
				"validators": [
					{
						"type": "regex",
						"validator": "^([6-9]{1})([0-9]{9})$",
						"arguments": []
					}
				],
				"required": true
			},
			{
				"id": "email",
				"description": "email",
				"labelName": {
					"eng": "Email",
					"ara": "البريد الإلكتروني",
					"fra": "Email"
				},
				"controlType": "textbox",
				"inputRequired": true,
				"fieldType": "default",
				"type": "string",
				"validators": [
					{
						"type": "regex",
						"validator": "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-zA-Z]{2,})$",
						"arguments": []
					}
				],
				"required": true
			},
			{
				"id": "proofOfAddress",
				"description": "proofOfAddress",
				"labelName": {
					"ara": "إثبات العنوان",
					"fra": "Address Proof",
					"eng": "Address Proof"
				},
				"controlType": "fileupload",
				"inputRequired": true,
				"validators": [],
				"subType": "POA",
				"required": false
			},
			{
				"id": "proofOfIdentity",
				"description": "proofOfIdentity",
				"labelName": {
					"ara": "إثبات الهوية",
					"fra": "Identity Proof",
					"eng": "Identity Proof"
				},
				"controlType": "fileupload",
				"inputRequired": true,
				"validators": [],
				"subType": "POI",
				"required": true
			},
			{
				"id": "proofOfRelationship",
				"description": "proofOfRelationship",
				"labelName": {
					"ara": "إثبات العلاقة",
					"fra": "Relationship Proof",
					"eng": "Relationship Proof"
				},
				"controlType": "fileupload",
				"inputRequired": true,
				"validators": [],
				"subType": "POR",
				"required": true
			},
			{
				"id": "proofOfDateOfBirth",
				"description": "proofOfDateOfBirth",
				"labelName": {
					"ara": "دليل DOB",
					"fra": "DOB Proof",
					"eng": "DOB Proof"
				},
				"controlType": "fileupload",
				"inputRequired": true,
				"validators": [],
				"subType": "POB",
				"required": true
			},
			{
				"id": "proofOfException",
				"description": "proofOfException",
				"labelName": {
					"ara": "إثبات الاستثناء",
					"fra": "Exception Proof",
					"eng": "Exception Proof"
				},
				"controlType": "fileupload",
				"inputRequired": true,
				"validators": [],
				"subType": "POE",
				"required": true
			},
			{
				"id": "proofOfException-1",
				"description": "proofOfException",
				"labelName": {
					"ara": "إثبات الاستثناء 2",
					"fra": "Exception Proof",
					"eng": "Exception Proof"
				},
				"controlType": "fileupload",
				"inputRequired": true,
				"validators": [],
				"subType": "POE",
				"required": true
			}
		],
		"locationHierarchy": [
			"region",
			"province",
			"city",
			"zone",
			"postalCode"
		]
	}
}
``` 


