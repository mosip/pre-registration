### -- ---------------------------------------------------------------------------------------------------------
### -- Script Name		: MOSIP ALL DB Artifacts deployment script
### -- Deploy Module 	: MOSIP PRE-REG Module
### -- Purpose    		: To deploy MOSIP All Modules Database DB Artifacts.       
### -- Create By   		: Sadanandegowda DM
### -- Created Date		: 07-Jan-2020
### -- 
### -- Modified Date        Modified By         Comments / Remarks
### -- -----------------------------------------------------------------------------------------------------------

#! bin/bash
echo "`date` : You logged on to DB deplyment server as : `whoami`"
echo "`date` : MOSIP Database objects deployment started...."

echo "=============================================================================================================="
bash ./mosip_prereg/mosip_prereg_db_deploy.sh ./mosip_prereg/mosip_prereg_deploy.properties
echo "=============================================================================================================="


echo "`date` : MOSIP DB Deployment for pre-reg databases is completed, Please check the logs at respective logs directory for more information"
 
