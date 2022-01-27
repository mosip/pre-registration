# pre-registration-batchjob

This BatchJob is used to run few tasks on scheduled intervals. The tasks supported are:
* Generating slots for booking appointments in registration centres
* Moving the applications to Expired status once the appointment date is passed
* Moving the applications to Consumed status once the application is processed by MOSIP.
* Keeping the applications and appointments data in sync. 

## Design
[Design](https://github.com/mosip/pre-registration/blob/master/design/pre-registration/pre-registration-batch-job.md)