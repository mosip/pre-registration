# PRIDgenerator

Helm chart for the MOSIP PRID generator. The service lives in this pre-registration repository as a **separate** JVM (`pridgenerator-service`, port 8100). Public prefix `/v1/pridgenerator` is unchanged. Image: `mosipqa/pridgenerator-service`. Kubernetes Service name stays `kernel-pridgenerator-service` so `mosip.io.prid.url` keeps resolving.

Do not fold this chart into `prereg-service`.

## Install

```console
$ kubectl create namespace prereg
$ helm repo add mosip https://mosip.github.io
$ helm -n prereg install pridgenerator mosip/pridgenerator
```

Health: `/v1/pridgenerator/actuator/health`. Fetch: `GET /v1/pridgenerator/prid`.

## Uninstall

```console
helm -n prereg delete pridgenerator
```
