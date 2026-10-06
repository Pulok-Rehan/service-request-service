// MongoDB seed script for ServiceRequestConfig collection
// Run via mongosh: mongosh "mongodb://localhost:27017/service_request" seed-services.js

db.ServiceRequestConfig.drop();

db.ServiceRequestConfig.insertMany([
  {
    "serviceName": "MOBILE_CHANGE",
    "displayName": "Change Mobile Number",
    "description": "Request to update your primary account mobile number",
    "active": true,
    "multipart": false,
    "listBased": false,
    "targetListField": null,
    "listIdentifierField": null,
    "allowedActions": ["EDIT"],
    "section": "PERSONAL_DETAILS",
    "sectionDisplayName": "Personal Details",
    "sectionOrder": 1,
    "displayOrder": 1,
    "targetAudience": "BOTH",
    "fields": [
      {
        "fieldName": "newMobileNumber",
        "label": "New Mobile Number",
        "dataType": "NUMBER",
        "required": true,
        "accountFieldPath": "mobileNumber",
        "file": false
      }
    ],
    "apiConfig": {
      "targetUrl": "http://10.20.242.239:9092/api/v1/accounts/{accountId}/mobile",
      "httpMethod": "PUT",
      "headers": {
        "Content-Type": "application/json"
      },
      "pathParams": ["accountId"],
      "queryParams": {},
      "bodyTemplate": {
        "mobileNumber": "${fieldValues.newMobileNumber}"
      }
    },
    "approvalLevels": [
      {
        "level": 1,
        "levelName": "Branch RM Review",
        "allowedRoles": ["ROLE_RM", "ROLE_BRANCH_MANAGER"],
        "specificApproverIds": []
      }
    ],
    "createdAt": new Date(),
    "updatedAt": new Date()
  },
  {
    "serviceName": "EMAIL_CHANGE",
    "displayName": "Change Email Address",
    "description": "Request to update your registered email address",
    "active": true,
    "multipart": false,
    "listBased": false,
    "targetListField": null,
    "listIdentifierField": null,
    "allowedActions": ["EDIT"],
    "section": "PERSONAL_DETAILS",
    "sectionDisplayName": "Personal Details",
    "sectionOrder": 1,
    "displayOrder": 2,
    "targetAudience": "BOTH",
    "fields": [
      {
        "fieldName": "newEmailAddress",
        "label": "New Email Address",
        "dataType": "EMAIL",
        "required": true,
        "accountFieldPath": "emailAddress",
        "file": false
      }
    ],
    "apiConfig": {
      "targetUrl": "http://10.20.242.239:9092/api/v1/accounts/{accountId}/email",
      "httpMethod": "PUT",
      "headers": {
        "Content-Type": "application/json"
      },
      "pathParams": ["accountId"],
      "queryParams": {},
      "bodyTemplate": {
        "emailAddress": "${fieldValues.newEmailAddress}"
      }
    },
    "approvalLevels": [
      {
        "level": 1,
        "levelName": "Branch RM Review",
        "allowedRoles": ["ROLE_RM", "ROLE_BRANCH_MANAGER"],
        "specificApproverIds": []
      }
    ],
    "createdAt": new Date(),
    "updatedAt": new Date()
  },
  {
    "serviceName": "ADDRESS_CHANGE",
    "displayName": "Change Address",
    "description": "Request to update your residential address details",
    "active": true,
    "multipart": false,
    "listBased": false,
    "targetListField": null,
    "listIdentifierField": null,
    "allowedActions": ["EDIT"],
    "section": "CONTACT_INFO",
    "sectionDisplayName": "Contact Information",
    "sectionOrder": 2,
    "displayOrder": 1,
    "targetAudience": "BOTH",
    "fields": [
      { "fieldName": "addressLine1", "label": "Address Line 1", "dataType": "TEXT", "required": true, "accountFieldPath": "addressLine1", "file": false },
      { "fieldName": "addressLine2", "label": "Address Line 2", "dataType": "TEXT", "required": false, "accountFieldPath": "addressLine2", "file": false },
      { "fieldName": "city", "label": "City", "dataType": "TEXT", "required": true, "accountFieldPath": "city", "file": false },
      { "fieldName": "state", "label": "State", "dataType": "TEXT", "required": true, "accountFieldPath": "state", "file": false },
      { "fieldName": "zipCode", "label": "Zip Code", "dataType": "TEXT", "required": true, "accountFieldPath": "zipCode", "file": false },
      { "fieldName": "country", "label": "Country", "dataType": "TEXT", "required": true, "accountFieldPath": "country", "file": false }
    ],
    "apiConfig": {
      "targetUrl": "http://10.20.242.239:9092/api/v1/accounts/{accountId}/address",
      "httpMethod": "PUT",
      "headers": { "Content-Type": "application/json" },
      "pathParams": ["accountId"]
    },
    "approvalLevels": [
      { "level": 1, "levelName": "RM Initial Verification", "allowedRoles": ["ROLE_RM"] },
      { "level": 2, "levelName": "Compliance Head Approval", "allowedRoles": ["ROLE_COMPLIANCE", "ROLE_ADMIN"] }
    ],
    "createdAt": new Date(),
    "updatedAt": new Date()
  },
  {
    "serviceName": "TIN_CHANGE",
    "displayName": "Change TIN Information",
    "description": "Add or update your Tax Identification Number and certificate",
    "active": true,
    "multipart": true,
    "listBased": false,
    "targetListField": null,
    "listIdentifierField": null,
    "allowedActions": ["ADD", "EDIT"],
    "section": "TAX_INFO",
    "sectionDisplayName": "Tax Information",
    "sectionOrder": 3,
    "displayOrder": 1,
    "targetAudience": "BOTH",
    "fields": [
      { "fieldName": "tinNumber", "label": "TIN Number", "dataType": "STRING", "required": true, "accountFieldPath": "tinNumber", "file": false },
      { "fieldName": "tinCertificate", "label": "TIN Certificate Document", "dataType": "FILE", "required": false, "accountFieldPath": "tinCertificate", "file": true }
    ],
    "apiConfig": {
      "targetUrl": "http://10.20.242.239:9092/api/v1/accounts/{accountId}/tin",
      "httpMethod": "PUT",
      "headers": { "Content-Type": "application/json" },
      "pathParams": ["accountId"]
    },
    "approvalLevels": [
      { "level": 1, "levelName": "Branch Operations Review", "allowedRoles": ["ROLE_OPS", "ROLE_RM"] },
      { "level": 2, "levelName": "Tax Compliance Approval", "allowedRoles": ["ROLE_COMPLIANCE", "ROLE_ADMIN"] }
    ],
    "createdAt": new Date(),
    "updatedAt": new Date()
  },
  {
    "serviceName": "BANK_CHANGE",
    "displayName": "Change Bank Account",
    "description": "Update bank account number, bank name, routing number, or branch",
    "active": true,
    "multipart": true,
    "listBased": false,
    "targetListField": null,
    "listIdentifierField": null,
    "allowedActions": ["EDIT"],
    "section": "BANK_INFO",
    "sectionDisplayName": "Bank Information",
    "sectionOrder": 4,
    "displayOrder": 1,
    "targetAudience": "BOTH",
    "fields": [
      { "fieldName": "bankAccountNumber", "label": "Bank Account Number", "dataType": "STRING", "required": true, "accountFieldPath": "bankAccountNumber", "file": false },
      { "fieldName": "bankName", "label": "Bank Name", "dataType": "TEXT", "required": true, "accountFieldPath": "bankName", "file": false },
      { "fieldName": "branchName", "label": "Branch Name", "dataType": "TEXT", "required": true, "accountFieldPath": "branchName", "file": false },
      { "fieldName": "routingNumber", "label": "Routing Number", "dataType": "STRING", "required": true, "accountFieldPath": "routingNumber", "file": false },
      { "fieldName": "chequeLeaf", "label": "Cheque Leaf Document", "dataType": "FILE", "required": false, "accountFieldPath": "chequeLeaf", "file": true }
    ],
    "apiConfig": {
      "targetUrl": "http://10.20.242.239:9092/api/v1/accounts/{accountId}/bank",
      "httpMethod": "PUT",
      "headers": { "Content-Type": "application/json" },
      "pathParams": ["accountId"]
    },
    "approvalLevels": [
      { "level": 1, "levelName": "Branch Manager Review", "allowedRoles": ["ROLE_BRANCH_MANAGER", "ROLE_RM"] },
      { "level": 2, "levelName": "Settlement Accounts Head Approval", "allowedRoles": ["ROLE_SETTLEMENT", "ROLE_ADMIN"] }
    ],
    "createdAt": new Date(),
    "updatedAt": new Date()
  },
  {
    "serviceName": "NOMINEE_ADD",
    "displayName": "Add Nominee",
    "description": "Add a new nominee to your account",
    "active": true,
    "multipart": true,
    "listBased": true,
    "targetListField": "nominees",
    "listIdentifierField": "nid",
    "allowedActions": ["ADD"],
    "section": "NOMINEE",
    "sectionDisplayName": "Nominee Information",
    "sectionOrder": 5,
    "displayOrder": 1,
    "targetAudience": "BOTH",
    "fields": [
      { "fieldName": "name", "label": "Nominee Name", "dataType": "TEXT", "required": true, "accountFieldPath": "name", "file": false },
      { "fieldName": "relation", "label": "Relation", "dataType": "TEXT", "required": true, "accountFieldPath": "relation", "file": false },
      { "fieldName": "nid", "label": "Nominee NID Number", "dataType": "STRING", "required": true, "accountFieldPath": "nid", "file": false },
      { "fieldName": "percentage", "label": "Percentage Share", "dataType": "NUMBER", "required": true, "accountFieldPath": "percentage", "file": false },
      { "fieldName": "mobileNumber", "label": "Mobile Number", "dataType": "NUMBER", "required": false, "accountFieldPath": "mobileNumber", "file": false },
      { "fieldName": "address", "label": "Address", "dataType": "TEXT", "required": false, "accountFieldPath": "address", "file": false },
      { "fieldName": "nomineePhoto", "label": "Nominee Photo", "dataType": "FILE", "required": false, "accountFieldPath": "nomineePhoto", "file": true }
    ],
    "apiConfig": {
      "targetUrl": "http://10.20.242.239:9092/api/v1/accounts/{accountId}/nominees",
      "httpMethod": "POST",
      "headers": { "Content-Type": "application/json" },
      "pathParams": ["accountId"]
    },
    "approvalLevels": [
      { "level": 1, "levelName": "Branch RM Review", "allowedRoles": ["ROLE_RM", "ROLE_BRANCH_MANAGER"] },
      { "level": 2, "levelName": "Central Operations Approval", "allowedRoles": ["ROLE_OPS", "ROLE_ADMIN"] }
    ],
    "createdAt": new Date(),
    "updatedAt": new Date()
  }
]);

print("Successfully seeded ServiceRequestConfig collection with " + db.ServiceRequestConfig.countDocuments() + " services.");
