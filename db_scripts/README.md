# Pre-Reg Database

## Introduction
This folder containers various SQL scripts to create database and tables in postgres.  These scripts are automatically run with [DB init](https://github.com/mosip/mosip-infra/blob/1.2.0-rc2/deployment/v3/external/postgres/cluster/init_db.sh) of sandbox deployment.

Default data that's populated in the tables is present under [`dml`](mosip_prereg/dml) folder 

## Database
The modules uses `mosip_prereg` database with following tables:

* [anonymous_profile](mosip_prereg/ddl/prereg-anonymous_profile.sql): Data for reports and statistics.  The data here is anonymised.
* [applicant_demographic](mosip_prereg/ddl/prereg-applicant_demographic.sql): Demographic data entered by the user.
* [applicant_demographic_consumed](mosip_prereg/ddl/prereg-applicant_demographic_consumed.sql)
* [applicant_document](mosip_prereg/ddl/prereg-applicant_document.sql): Documents uploaded by user.
* [applicant_document_consumed](mosip_prereg/ddl/prereg-applicant_document_consumed.sql)
* [applications](mosip_prereg/ddl/prereg-applications.sql)
* [batch_job_execution](mosip_prereg/ddl/prereg-batch_job_execution.sql)
* [batch_job_execution_context](mosip_prereg/ddl/prereg-batch_job_execution_context.sql)
* [batch_job_execution_params](mosip_prereg/ddl/prereg-batch_job_execution_params.sql)
* [batch_job_instance](mosip_prereg/ddl/prereg-batch_job_instance.sql)
* [batch_step_execution](mosip_prereg/ddl/prereg-batch_step_execution.sql)
* [batch_step_execution_context](mosip_prereg/ddl/prereg-batch_step_execution_context.sql)
* [intf_processed_prereg_list](mosip_prereg/ddl/prereg-intf_processed_prereg_list.sql)
* [language_transliteration](mosip_prereg/ddl/prereg-language_transliteration.sql)
* [otp_transaction](mosip_prereg/ddl/prereg-otp_transaction.sql)
* [pre_registration_transaction](mosip_prereg/ddl/prereg-pre_registration_transaction.sql)
* [prid_seed](mosip_prereg/ddl/prereg-prid_seed.sql)
* [prid_seq](mosip_prereg/ddl/prereg-prid_seq.sql)
* [processed_prereg_list](mosip_prereg/ddl/prereg-processed_prereg_list.sql)
* [reg_appointment](mosip_prereg/ddl/prereg-reg_appointment.sql)
* [reg_appointment_consumed](mosip_prereg/ddl/prereg-reg_appointment_consumed.sql)
* [reg_available_slot](mosip_prereg/ddl/prereg-reg_available_slot.sql)
* [transaction_type](mosip_prereg/ddl/prereg-transaction_type.sql)       
