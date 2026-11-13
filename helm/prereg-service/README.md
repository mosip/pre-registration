# PreReg Application

Helm chart for the consolidated MOSIP Pre-Registration HTTP service (application, datasync, captcha, and booking in one Deployment). Image: `mosipqa/pre-registration-service`. Istio prefixes include `/preregistration/v1/appointment`, `/preregistration/v1/sync`, `/preregistration/v1/captcha`, and the legacy swagger catalog (`/application-service`, `/sync/datasync-service`, `/captcha-service`, `/appointment/booking-service`).

## Install
```console
$ kubectl create namespace prereg
$ helm repo add mosip https://mosip.github.io
$ helm -n prereg install my-release mosip/prereg-application
```

