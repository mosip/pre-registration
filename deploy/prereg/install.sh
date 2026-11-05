#!/bin/bash
# Installs all prereg helm charts
## Usage: ./install.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
NS=prereg
CHART_VERSION=0.0.1-develop

COPY_UTIL=$SCRIPT_DIR/../copy_cm_func.sh

echo Create $NS namespace
kubectl get ns $NS >/dev/null 2>&1 || kubectl create ns $NS

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

  echo Installing prereg-application (consolidated HTTP service, includes booking)
  helm -n $NS install prereg-application mosip/prereg-application --version $CHART_VERSION

  echo Installing prereg-batchjob
  helm -n $NS install prereg-batchjob mosip/prereg-batchjob --version $CHART_VERSION

  echo Installing pridgenerator
  helm -n $NS install pridgenerator mosip/pridgenerator --version $CHART_VERSION

  echo Installing prereg-ui
  "$SCRIPT_DIR/../prereg-ui/install.sh"

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
