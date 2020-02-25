# Approach for Data Sync Service

**Background**
- Exposing the API to Registration client will provide with the list of Pre-Registration IDs for which they wants to get Pre-Registration Data.
- Expose the API to Registration Processor will provide with the list of Pre-Registration IDs for which it wants to update Pre-Registration status.

The key requirements are
- Create the REST API to Registration client, list of Pre-Registration IDs for which it wants to get Pre-Registration Data.
- Another REST API to Registration client, they will provide Pre-Registration Id to get the pre-registration data in zipped format. which consisting of ID.json and Documents.
- Create the REST API to Registration Processor will provide with the list of Pre-Registration IDs for which they consumed.

The key non-functional requirements are
- Security :
    - The Pre-Registration securely share the pre-registration data to registration client.
    - Response signature.
- Log the each state of the data sync:
    -   As a security measures the PII information should not be logged.
- Audit :
    - User ID, RC ID, Transaction ID, Time-stamp should be stored into the DB for audit purpose.
    - Pre-reg Id and important detail of the applicant should not be audited.
	- Registration processor ID, Transaction ID, Time-stamp should be stored into the DB for audit purpose.
- Exception :
    -   Any exception occurred during the pre-registration data sync, the same will be reported to the registration client in a understandable exception.

	
**NFRs -**

1. Pre registration would expose Data-sync service, this is REST over HTTPS.
2. Registration client would call the service
3. the result would be fetched from DB and zipped information delivered.
4. security architecture would depict the security scenarios other than HTTPS.
5. multiple clients and pre registration is source of data.
6. registration client should be on-line to get the data.
7. No intermediate server to support mobility of registration client.
8. HTTPS provides end point security.

 

**Solution**

**Reterive all pre-registration Ids :**

1. Create a REST API as '/sync' POST Method, which accept the Data Sync JSON object from the request body.
2. The JSON object contains Registration Center ID, Appointment Date Range(Start Date, End Date).
3. The System will generate a Transaction ID and  fetch all the Pre-Registrations within the Date Range(Start Range, End Date) and for the Registration Center ID received.
4. The System will calculate the count of the Pre-Registration IDs being sent.
5. The System will send the List of Pre-Registration Ids, count of Pre-Registrations and transaction id in response entity .
6. Audit the exception/start/exit of the each stages of the data sync mechanism using AuditManager component.

**Class Diagram**

![pre-registration-data-sync-service-sync-all](_images/_class_diagram/data-sync-service.png)

**Sequence Diagram**

![pre-registration-data-sync-service-sync -all](_images/_sequence_diagram/dataSync-retrieve-all-preRegIds.png)




**Reterive Pre-Registrations:**

1. Create a REST API as '/sync' GET Method, which accept the pre-registration id from the request path parameter.
2. The System will generate a Transaction ID and do the following operation need to be happens:
	 Step1: fetch the demographic JSON object and appointment date time and decrypt JSON object, if successful go to next step otherwise throw an exception.
	 Step2: fetch all the document metadata and prepare the JSON structure and append it to the ID JSON object. fetch the perticular document, if successful go to next step otherwise throw an exception.
	 Step3: prepare a zip file and ResponseDTO.{zip file structure need to discuss}
3.   Audit the exception/start/exit of the each stages of the data sync mechanism using AuditManager component.

**Class Diagram**

![pre-registration-data-sync-service-sync-data](_images/_class_diagram/data-sync-service.png)

**Sequence Diagram**

![pre-registration-data-sync-service-sync-data](_images/_sequence_diagram/dataSync-retrieve-data.png)




**Store all pre-registration Ids :**

1. Create a REST API as '/sync/consumedPreRegIds' POST method accept the JSON object from the registration-processor.
2. The Registration Processor will provide the List of Pre-Registration IDs received by it(from Registration Client). 
3. The System will generate a Transaction ID and store all the Pre-Registration ids in "prereg-i_processed_prereg_list" table and update in "prereg-processed_prereg_list" table.
4. The "prereg-i_processed_prereg_list" table is not permanent, for maintenance purpose database team can truncate this table.
5. A batch job need to be running to update the application.demographic table with "Processed" status.
6. Once Pre-Registration successfully processed. System will send an Acknowledgment of the Receipt ("need to be check BA(Vyas)")
7. Audit the exception/start/exit of the each stages of the reverse data sync mechanism using AuditManager component.

**Class Diagram**

![pre-registration-data-sync-service-sync -consumed](_images/_class_diagram/data-sync-service-consumed.png)

**Sequence Diagram**

![pre-registration-data-sync-service-sync -consumed](_images/_sequence_diagram/dataSync-consumed-preRegId.png)


**Success / Error Code** 
   - While processing the Pre-Registration if there is any error or successfully then send the respective success or error code to the UI from API layer as  Response object.

  Code   |       Type  | Message|
-----|----------|-------------|
  0000      |             Success |   Packet Successfully created


**Dependency Modules**

Component Name | Module Name | Description | 
-----|----------|-------------|
  Audit Manager     |   Kernel        |    To audit the process while data sync.
  Exception Manager  |  Kernel     |       To prepare the user defined exception and render to the user.
  Log        |          Kernel         |   To log the process.
  Database Access   |    Kernel      |      To get the database connectivity
