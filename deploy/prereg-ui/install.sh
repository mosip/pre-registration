#!/bin/bash
# Installs all prereg-ui helm charts
## Usage: ./install.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
NS=prereg
CHART_VERSION=0.0.1-develop
PREREG_GATEWAY_CHART_VERSION=1.0.0-develop

echo Create $NS namespace
kubectl get ns $NS >/dev/null 2>&1 || kubectl create ns $NS

function installing_prereg_ui() {
  echo Istio label
  ## TODO: Istio proxy disabled for now as prereui does not come up if
  ## envoy filter container gets installed after prereg-uicontainer.
  kubectl label ns $NS istio-injection=disabled --overwrite
  helm repo update

  echo Copy configmaps
  COPY_UTIL=$SCRIPT_DIR/../copy_cm_func.sh
  $COPY_UTIL configmap global default $NS
  $COPY_UTIL configmap artifactory-share artifactory $NS
  $COPY_UTIL configmap config-server-share config-server $NS

  API_HOST=`kubectl get cm global -o jsonpath={.data.mosip-api-host}`
  PREREG_HOST=$(kubectl get cm global -o jsonpath="{.data.mosip-prereg-host}" | tr -d '\r\n')
  echo "Using PREREG_HOST: '$PREREG_HOST'"

  echo Install prereg-gateway
  helm -n $NS install prereg-gateway mosip/istio-addons \
  --set istio.name=prereg-gateway \
  --set istio.ingressController=ingressgateway \
  --set istio.host=$PREREG_HOST \
  --version $PREREG_GATEWAY_CHART_VERSION

  echo Installing prereg-ui
  helm -n $NS install prereg-ui mosip/prereg-ui --set prereg.apiHost=$PREREG_HOST --version $CHART_VERSION

  echo Installing prereg rate-control Envoyfilter
  kubectl apply -n $NS -f "$SCRIPT_DIR/rate-control-envoyfilter.yaml"

  kubectl -n $NS  get deploy -o name |  xargs -n1 -t  kubectl -n $NS rollout status

  echo Installed prereg-ui services
  return 0
}

# set commands for error handling.
set -e
set -o errexit   ## set -e : exit the script if any statement returns a non-true return value
set -o nounset   ## set -u : exit the script if you try to use an uninitialised variable
set -o errtrace  # trace ERR through 'time command' and other functions
set -o pipefail  # trace ERR through pipes
installing_prereg_ui  # calling function
