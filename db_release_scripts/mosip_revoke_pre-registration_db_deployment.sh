### -- ---------------------------------------------------------------------------------------------------------
### -- Script Name		: Revoke DB deploy
### -- Deploy Module 	: MOSIP PRE-REG
### -- Purpose    		: To revoke MOSIP Database alter scripts for the release, Scripts revoke changes for pre-reg databases.      
### -- Create By   		: Sadanandegowda
### -- Created Date		: 07-Jan-2020
### -- 
### -- Modified Date        Modified By         Comments / Remarks
### -- -----------------------------------------------------------------------------------------------------------

### -- -----------------------------------------------------------------------------------------------------------

#! bin/bash
echo "`date` : You logged on to DB deplyment server as : `whoami`"
echo "`date` : MOSIP Database objects release deployment revoke started.... Release Number : $1"

echo "=============================================================================================================="
bash ./mosip_prereg/prereg_revoke_db_deploy.sh ./mosip_prereg/prereg_release_deploy.properties $1
echo "=============================================================================================================="

echo "`date` : MOSIP DB Release Deployment revoke for pre-reg databases is completed, Please check the logs at respective logs directory for more information"
 
