# Pre-registration Configuration Guide

## Property files
The configuration of the services is controlled by the following property files that are accessible in this [repository](https://github.com/mosip/mosip-config/tree/master).
Please refer to the required released tagged version for configuration
:
* `application-default.propertes`
* `pre-registration-default.propertes`

See [Module Configuration](https://docs.mosip.io/1.2.0/modules/module-configuration) for location of these files.

## Few important configurations
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

