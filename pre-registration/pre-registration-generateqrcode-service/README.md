# pre-registration-generateqrcode-service

This service details used by Pre-Registration portal to generate QR Code.

 

## Design

[Design](https://github.com/mosip/pre-registration/blob/master/design/pre-registration/pre-registration-generate-qr-code-service.md)

 

## Default Port and Context Path

  * server.port=9091
  * server.servlet.context-path=/preregistration/v1/qrCode



## URL

* https://{dns-name}:9091/preregistration/v1/qrCode/swagger-ui.html


## Roles to Access the URL

* INDIVIDUAL



## API Dependencies
	
|Dependent Module |  Dependent Services  | API |
| ------------- | ------------- | ------------- |
| commons/kernel  | kernel-masterdata-service  | /documenttypes/{documentcategorycode}/{langcode}|
|  |   | /validdocuments/{languagecode} |
