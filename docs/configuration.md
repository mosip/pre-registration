# Pre-registration Configuration Guide

## Property files
The configuration of the services is controlled by the following property files:
* [`application-default.propertes`](https://github.com/mosip/mosip-config/blob/1.2.0_v3/application-default.properties)
* [`pre-registration-default.propertes`](https://github.com/mosip/mosip-config/blob/1.2.0_v3/pre-registration-default.properties)


## Important configurations
|Property|Description|
|---|---|
|`preregistration.document.extention`|Document formats|
|`max.file.size`|Document max upload size|
|`preregistration.country.specific.zoneId`|Time zone|

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
          "fra": "Le genre"
        },
        "controlType": "dropdown",
        "inputRequired": true,
        "fieldType": "dynamic",
        "type": "simpleType",
        "validators": [],
        "required": true
      },
      {
        "id": "residenceStatus",
        "description": "Residence status",
        "labelName": {
          "eng": "Residence Status",
          "fra": "Statut de résidence"
        },
        "controlType": "dropdown",
        "inputRequired": true,
        "fieldType": "dynamic",
        "type": "simpleType",
        "validators": [],
        "required": true
      },
      {
        "id": "addressLine1",
        "description": "addressLine1",
        "labelName": {
          "eng": "Address Line1",
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
        "required": true
      },
      {
        "id": "province",
        "description": "province",
        "labelName": {
          "eng": "Province",
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
        "required": true
      },
      {
        "id": "city",
        "description": "city",
        "labelName": {
          "eng": "City",
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
        "required": true
      },
      {
        "id": "zone",
        "description": "zone",
        "labelName": {
          "eng": "Zone",
          "fra": "Zone"
        },
        "controlType": "dropdown",
        "inputRequired": true,
        "fieldType": "default",
        "type": "simpleType",
        "validators": [],
        "required": true
      },
      {
        "id": "postalCode",
        "description": "postalCode",
        "labelName": {
          "eng": "Postal Code",
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
        "required": true
      },
      {
        "id": "phone",
        "description": "phone",
        "labelName": {
          "eng": "Phone",
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


