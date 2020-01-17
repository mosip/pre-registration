### -- ---------------------------------------------------------------------------------------------------------
### -- Script Name		: Release DB deployment
### -- Deploy Module 	: MOSIP PRE-REG
### -- Purpose    		: To deploy MOSIP Database alter scripts for the release, Scripts deploy changes for pre-reg databases.       
### -- Create By   		: Sadanandegowda
### -- Created Date		: 07-Jan-2020
### -- 
### -- Modified Date        Modified By         Comments / Remarks
### -- -----------------------------------------------------------------------------------------------------------

### -- -----------------------------------------------------------------------------------------------------------

#! bin/bash
echo "`date` : You logged on to DB deplyment server as : `whoami`"
echo "`date` : MOSIP Database objects deployment for the release started.... Release Number : $1"

echo "=============================================================================================================="
bash ./mosip_prereg/prereg_release_db_deploy.sh ./mosip_prereg/prereg_release_deploy.properties $1
echo "=============================================================================================================="

echo "`date` : MOSIP DB Release Deployment for pre-reg databases is completed, Please check the logs at respective logs directory for more information"
 
