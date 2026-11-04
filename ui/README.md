## Overview

Reference Pre-Registration portal (Angular **7** SPA). This folder is `ui/` in [mosip/pre-registration](https://github.com/mosip/pre-registration), imported from [mosip/pre-registration-ui](https://github.com/mosip/pre-registration-ui) `develop`. Helm: `helm/prereg-ui`. Cluster scripts: `deploy/prereg-ui`. Backend: host `pre-registration-service` on `:9090` (`/preregistration/v1`, including booking). 

## Features
The Pre-Registration portal Features are :
1. Resident can login into PreRegistration with email id / phone number 
2. Resident can create multiple PreRegistration application. 
3. For each PreRegistration application, resident can give demographic data in multiple languages. 
4. For each PreRegistration application, resident can upload various proof documents. 
5. For each PreRegistration application, resident can search for a Registration Center and search for suitable appointment slots. 
6. For each PreRegistration application, resident can book an appointment at suitable Registration Center 
7. For every appointment booking, resident can get the acknowledgement as well as a notification via email id / phone number. 
8. Resident can cancel the appointment or reschedule it as well.

### Prerequisites

Before you begin, ensure you have the following installed:

The code is **Angular 7** (not AngularJS).

- Node **14.x** / npm **6.x** (recommend Node 14.17.3, npm 6.14.13).
- Angular CLI **7.x** to match `@angular/core` 7.2. Do not install CLI 13 against this tree.
  - <code>npm install -g @angular/cli@7</code>
  - <code>ng --version</code>
  
- Check out the source code from GIT – To download the source code from git. Follow the following steps to download source code on your system.
  - <code>git clone https://github.com/mosip/pre-registration.git</code> then <code>cd ui</code>

### For Production build:

- Build the code – Follow the following steps to build the source code on your system.
  - Navigate to `ui/` in this repository. Then run the following command in that directory
  - <code>ng build "--prod" "--base-href" "." "--output-path=dist" </code>(to build the code)

- Build Docker Image – Follow the following steps to build docker image on your system.
  - <code>docker build -t name . </code>(to build the docker image, replace <code>name</code> with the name of the image you want, &quot;.&quot; Signifies the current directory from where the docker file has to be read.
  - Example: <code>docker build -t preregui .</code>
- Run the docker image – Follow the following steps to build docker image on your system.
  - <code>docker run -d -p 8080:8080 --name container-name image-name</code> (detached; host:container. The image exposes **8080**.)
  - Example: <code>docker run -d -p 8080:8080 --name nginx preregui</code>
- Now you can access the user interface over the internet via browser.
  - Example: <code>http://localhost:8080/pre-registration-ui/#/eng</code>

### For Local build:

Prereqs: Node **14.x** / npm **6.x** (Angular 7 / webpack 4). Node 17+ (including 22) works if `NODE_OPTIONS=--openssl-legacy-provider` is set — `run-local` does that. Do **not** `npm audit fix --force` or upgrade npm to 12; this tree is Angular 7. Docker deps up; host `pre-registration-service` on `:9090`.

From `ui/`:

```text
run-local.bat
./run-local.sh
```

Commands: `init` (npm install), `start` (default — background HTTP `ng serve` + proxy to `:9090`; returns after compile), `smoke`, `stop`, `help`. Logs: `ui/.local/logs/prereg-ui.log`. The pdfjs `Can't resolve zlib` warning is expected.

- Open `http://127.0.0.1:4200`.
- `src/assets/config.json` is already `BASE_URL=http://127.0.0.1:4200/proxyapi/` — the scripts do not edit it.
- `proxy.conf.json` is gitignored; start writes it (`/proxyapi` → `http://localhost:9090`). Override with `PREREG_URL`. Skip health checks with `SKIP_PREFLIGHT=1`.
- `npm install` writes to `ui/.local/node_modules` (gitignored). `run-local` junctions `ui/node_modules` there so Angular CLI still resolves `./node_modules`.
- Without the script: `ng serve --proxy-config proxy.conf.json` (copy `proxy.conf.example.json` to `proxy.conf.json`). Plain `ng serve` without the proxy hits CORS against the backend.
  
## Deployment

### Kubernetes

To deploy Pre-Registration services on a Kubernetes cluster, refer to the [Sandbox Deployment Guide](https://docs.mosip.io/1.2.0/deploymentnew/v3-installation).

## Documentation

For more detailed documents for repositories, you can [check here](https://github.com/mosip/documentation/tree/1.2.0/docs).

### Product Documentation

To learn more about Pre-Registration portal from a functional perspective and use case scenarios, refer to our main documentation: [Click here](https://docs.mosip.io/1.2.0/id-lifecycle-management/identity-issuance/pre-registration).

## Contribution & Community

• To learn how you can contribute code to this application, [click here](https://docs.mosip.io/1.2.0/community/code-contributions).

• If you have questions or encounter issues, visit the [MOSIP Community](https://community.mosip.io/) for support.

• For any GitHub issues: [Report here](https://github.com/mosip/pre-registration/issues)

## License

This project is licensed under the [Mozilla Public License 2.0](../LICENSE).
