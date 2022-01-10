# DB Scripts

## Overview
This folder containers various SQL scripts to create database and tables in postgres. 

## Run
These scripts are automatically run with [DB init](https://github.com/mosip/mosip-infra/blob/1.2.0-rc2/deployment/v3/external/postgres/cluster/init_db.sh) of sandbox deployment. 

To build `postgres-init` docker run the scripts on command line refer [here](https://github.com/mosip/mosip-infra/blob/1.2.0-rc2/build/postgres-init/README.md)

Default data that's populated in the tables is present under [`dml`](mosip_prereg/dml) folder 

## Database
The modules uses `mosip_prereg` database with following tables:

* [anonymous_profile](mosip_prereg/ddl/anonymous_profile.sql) 
  The data here is anonymised.
* [applicant_demographic](mosip_prereg/ddl/applicant_demographic.sql)
* [applicant_demographic_consumed](mosip_prereg/ddl/applicant_demographic_consumed.sql)
* [applicant_document](mosip_prereg/ddl/applicant_document.sql)
* [applicant_document_consumed](mosip_prereg/ddl/applicant_document_consumed.sql)
* [applications](mosip_prereg/ddl/applications.sql)
* [batch_job_execution](mosip_prereg/ddl/batch_job_execution.sql)
* [batch_job_execution_context](mosip_prereg/ddl/batch_job_execution_context.sql)
* [batch_job_execution_param](mosip_prereg/ddl/batch_job_execution_param.sql)
* [batch_job_instance](mosip_prereg/ddl/batch_job_instance.sql)
* [batch_step_execution](mosip_prereg/ddl/batch_step_execution.sql)
* [batch_step_execution_context](mosip_prereg/ddl/batch_step_execution_context.sql)
* [intf_processed_prereg_list](mosip_prereg/ddl/intf_processed_prereg_list.sql)
* [otp_transaction](mosip_prereg/ddl/otp_transaction.sql)
* [processed_prereg_list](mosip_prereg/ddl/processed_prereg_list.sql)
* [reg_appointment](mosip_prereg/ddl/reg_appointment.sql)
* [reg_appointment_consumed](mosip_prereg/ddl/reg_appointment_consumed.sql)
* [reg_available_slot](mosip_prereg/ddl/reg_available_slot.sql)

