# Pre-registration Configuration Guide

## Property files

Cluster / QA configuration lives in [mosip-config](https://github.com/mosip/mosip-config) (use the release branch that matches the build, not only `master`).

Laptop runs use the copies under [`pre-registration/local-dev-setup/deps/mosip-config/`](../pre-registration/local-dev-setup/deps/mosip-config/):

* `application-default.properties`
* `pre-registration-default.properties`

Host JVM profile `local` also loads `pre-registration-service/src/main/resources/application-local.properties`.

See [Module Configuration](https://docs.mosip.io/1.2.0/id-lifecycle-management/supporting-services/module-configuration) for where these files sit in a MOSIP deployment.

## Few important configurations

| Property | Description |
|---|---|
| `preregistration.document.extention` | Document formats |
| `max.file.size` | Document max upload size |
| `mosip.preregistration.document.scan` | Virus scan for uploaded documents |
| `preregistration.availability.sync` | Number of days for which sync is done on booking slots |
| `preregistration.availability.noOfDays` | Number of days for which booking slots will be available |
| `preregistration.booking.offset` | Gap between the date of booking and the first appointment date shown |
| `preregistration.config.identityjson` | Mapping file for Pre-registration and ID Object |
| `preregistration.identity.name` | ID Schema field that indicates the user's name |
| `preregistration.notification.nameFormat` | ID Schema name field used in notifications |
| `preregistration.recommended.centers.locCode` | Location hierarchy code for recommended centres (UI `ui.config.params`) |
| `preregistration.contact.email` | Support email on the Pre-registration portal (UI) |
| `preregistration.contact.phone` | Support phone on the Pre-registration portal (UI) |
| `mosip.preregistration.captcha.id.validate` | ID returned in the `id` field of `POST /captcha/validatecaptcha` responses (default `mosip.pre-registration.captcha.id.validate`) |
