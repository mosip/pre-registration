# Pre-registration module

## Deployment in K8 cluster with other MOSIP services:
### Pre-requisites
* Set KUBECONFIG variable to point to existing K8 cluster kubeconfig file:
    ```
    export KUBECONFIG=~/.kube/<k8s-cluster.config>
    ```
### Install Pre-registration module
 ```
    cd deploy/prereg
    $ ./install.sh
   ```
### Delete
  ```
    cd deploy/prereg
    $ ./delete.sh
   ```
### Restart
  ```
    cd deploy/prereg
    $ ./restart.sh
   ```
### Install Keycloak client
  ```
    cd deploy/keycloak
    $ ./keycloak_init.sh
   ```

### Install Apitestrig
```
    cd deploy/apitest-auth
    $ ./install.sh
```
