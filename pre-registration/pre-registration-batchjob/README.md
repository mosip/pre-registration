# pre-registration-batchjob

Scheduled Job on port **9097**. Stays its own JVM after consolidation (not folded into `pre-registration-service`). Chart: `helm/prereg-batchjob`.

Tasks:

* Generating slots for booking appointments in registration centres
* Moving the applications to Expired status once the appointment date is passed
* Moving the applications to Consumed status once the application is processed by MOSIP.
* Keeping the applications and appointments data in sync.

Layout: [docs/design/service-consolidation.md](../../docs/design/service-consolidation.md). Local: `./run-local.sh init | start | smoke | stop`.