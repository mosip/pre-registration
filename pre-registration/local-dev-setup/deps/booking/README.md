# Booking local assets (unused)

Booking is no longer a Compose service. Native `/preregistration/v1/appointment/**` runs in the host `pre-registration-service` JVM (`io.mosip.preregistration.booking`), imported from [mosip-ref-impl issue-1633](https://github.com/mosip/mosip-ref-impl/tree/issue-1633/pre-registration-booking-service) with git history.

This folder is leftover overlay from the old `:9095` image. Do not start it. Slot tables stay in `mosip_prereg` (`reg_appointment`, `reg_available_slot`, `reg_appointment_consumed`); local slots for centre `10003` are inlined in [`../init.sql`](../init.sql).
