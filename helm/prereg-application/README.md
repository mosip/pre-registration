# PreReg Application

Helm chart for the consolidated MOSIP Pre-Registration HTTP service (application, datasync, captcha, and booking in one Deployment). Image: `mosipqa/pre-registration-service`. Istio prefixes include `/preregistration/v1/appointment`, `/preregistration/v1/sync`, and `/preregistration/v1/captcha`.

## Install
```console
$ kubectl create namespace prereg
$ helm repo add mosip https://mosip.github.io
$ helm -n prereg install my-release mosip/prereg-application
```

