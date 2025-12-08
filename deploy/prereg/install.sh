#!/bin/bash
# Installs all prereg helm charts
## Usage: ./install.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

NS=prereg
CHART_VERSION=1.3.0-develop
PREREG_BOOKING_CHART_VERSION=1.3.0-develop

COPY_UTIL=../copy_cm_func.sh

echo Create $NS namespace
kubectl create ns $NS

function installing_prereg() {
  echo Istio label
  ## TODO: Istio proxy disabled for now as prereui does not come up if
  ## envoy filter container gets installed after prereg container.
  kubectl label ns $NS istio-injection=disabled --overwrite
  helm repo update

  echo Copy configmaps
  $COPY_UTIL configmap global default $NS
  $COPY_UTIL configmap artifactory-share artifactory $NS
  $COPY_UTIL configmap config-server-share config-server $NS

  echo Installing prereg-application
  helm -n $NS install prereg-application mosip/prereg-application --version $CHART_VERSION

  echo Installing prereg-booking
  helm -n $NS install prereg-booking mosip/prereg-booking --version $PREREG_BOOKING_CHART_VERSION

  echo Installing prereg-datasync
  helm -n $NS install prereg-datasync mosip/prereg-datasync --version $CHART_VERSION

  echo Installing prereg-batchjob
  helm -n $NS install prereg-batchjob mosip/prereg-batchjob --version $CHART_VERSION

  kubectl -n $NS  get deploy -o name |  xargs -n1 -t  kubectl -n $NS rollout status

  echo Installed prereg services
  return 0
}

# set commands for error handling.
set -e
set -o errexit   ## set -e : exit the script if any statement returns a non-true return value
set -o nounset   ## set -u : exit the script if you try to use an uninitialised variable
set -o errtrace  # trace ERR through 'time command' and other functions
set -o pipefail  # trace ERR through pipes
installing_prereg   # calling function
