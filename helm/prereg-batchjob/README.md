# PreReg Batchjob

Helm chart for installing Pre-Registration Batchjob service.

## Install
```console
$ kubectl create namespace prereg
$ helm repo add mosip https://mosip.github.io
$ helm -n prereg install my-release mosip/prereg-batchjob
```

