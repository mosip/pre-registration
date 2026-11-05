# pre-registration-batchjob

Job JVM :9097. Not folded into the HTTP service. Chart: `helm/prereg-batchjob`.
Slots, Expired, Consumed, appointment sync. Calls frozen `/preregistration/v1/appointment/**` on :9090.
Local: `./run-local.sh init|start|smoke|stop`. How-to: README.md.
