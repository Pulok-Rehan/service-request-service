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
    "fields": [
      {
        "fieldName": "newMobileNumber",
        "label": "New Mobile Number",
        "dataType": "NUMBER",
        "required": true,
        "validationRegex": "^01[3-9]\\d{8}$",
        "accountFieldPath": "mobileNumber",
        "file": false
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
    "fields": [
      {
        "fieldName": "newEmailAddress",
        "label": "New Email Address",
        "dataType": "EMAIL",
        "required": true,
        "validationRegex": null,
        "accountFieldPath": "emailAddress",
        "file": false
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
    "fields": [
      { "fieldName": "addressLine1", "label": "Address Line 1", "dataType": "TEXT", "required": true, "accountFieldPath": "addressLine1", "file": false },
      { "fieldName": "addressLine2", "label": "Address Line 2", "dataType": "TEXT", "required": false, "accountFieldPath": "addressLine2", "file": false },
      { "fieldName": "city", "label": "City", "dataType": "TEXT", "required": true, "accountFieldPath": "city", "file": false },
      { "fieldName": "state", "label": "State", "dataType": "TEXT", "required": true, "accountFieldPath": "state", "file": false },
      { "fieldName": "zipCode", "label": "Zip Code", "dataType": "TEXT", "required": true, "accountFieldPath": "zipCode", "file": false },
      { "fieldName": "country", "label": "Country", "dataType": "TEXT", "required": true, "accountFieldPath": "country", "file": false }
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
    "fields": [
      { "fieldName": "tinNumber", "label": "TIN Number", "dataType": "STRING", "required": true, "accountFieldPath": "tinNumber", "file": false },
      { "fieldName": "tinCertificate", "label": "TIN Certificate Document", "dataType": "FILE", "required": false, "accountFieldPath": "tinCertificate", "file": true }
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
    "sectionOrder": 4,
    "displayOrder": 1,
    "fields": [
      { "fieldName": "name", "label": "Nominee Name", "dataType": "TEXT", "required": true, "accountFieldPath": "name", "file": false },
      { "fieldName": "relation", "label": "Relation", "dataType": "TEXT", "required": true, "accountFieldPath": "relation", "file": false },
      { "fieldName": "nid", "label": "Nominee NID Number", "dataType": "STRING", "required": true, "accountFieldPath": "nid", "file": false },
      { "fieldName": "percentage", "label": "Percentage Share", "dataType": "NUMBER", "required": true, "accountFieldPath": "percentage", "file": false },
      { "fieldName": "mobileNumber", "label": "Mobile Number", "dataType": "NUMBER", "required": false, "accountFieldPath": "mobileNumber", "file": false },
      { "fieldName": "address", "label": "Address", "dataType": "TEXT", "required": false, "accountFieldPath": "address", "file": false },
      { "fieldName": "city", "label": "City", "dataType": "TEXT", "required": false, "accountFieldPath": "city", "file": false },
      { "fieldName": "state", "label": "State", "dataType": "TEXT", "required": false, "accountFieldPath": "state", "file": false },
      { "fieldName": "zipCode", "label": "Zip Code", "dataType": "TEXT", "required": false, "accountFieldPath": "zipCode", "file": false },
      { "fieldName": "country", "label": "Country", "dataType": "TEXT", "required": false, "accountFieldPath": "country", "file": false },
      { "fieldName": "residency", "label": "Residency", "dataType": "TEXT", "required": false, "accountFieldPath": "residency", "file": false },
      { "fieldName": "nomineeNidFront", "label": "Nominee NID Front Photo", "dataType": "FILE", "required": false, "accountFieldPath": "nomineeNidFront", "file": true },
      { "fieldName": "nomineeNidBack", "label": "Nominee NID Back Photo", "dataType": "FILE", "required": false, "accountFieldPath": "nomineeNidBack", "file": true },
      { "fieldName": "nomineePhoto", "label": "Nominee Passport Photo", "dataType": "FILE", "required": false, "accountFieldPath": "nomineePhoto", "file": true },
      { "fieldName": "nomineeSignature", "label": "Nominee Signature", "dataType": "FILE", "required": false, "accountFieldPath": "nomineeSignature", "file": true },
      { "fieldName": "minor", "label": "Is Minor", "dataType": "BOOLEAN", "required": false, "accountFieldPath": "minor", "file": false },
      { "fieldName": "guardianName", "label": "Guardian Name", "dataType": "TEXT", "required": false, "accountFieldPath": "guardianName", "file": false },
      { "fieldName": "relationshipWithNominee", "label": "Relationship with Nominee", "dataType": "TEXT", "required": false, "accountFieldPath": "relationshipWithNominee", "file": false },
      { "fieldName": "guardianNidNumber", "label": "Guardian NID Number", "dataType": "STRING", "required": false, "accountFieldPath": "guardianNidNumber", "file": false },
      { "fieldName": "guardianNidFront", "label": "Guardian NID Front", "dataType": "FILE", "required": false, "accountFieldPath": "guardianNidFront", "file": true },
      { "fieldName": "guardianNidBack", "label": "Guardian NID Back", "dataType": "FILE", "required": false, "accountFieldPath": "guardianNidBack", "file": true },
      { "fieldName": "guardianSignature", "label": "Guardian Signature", "dataType": "FILE", "required": false, "accountFieldPath": "guardianSignature", "file": true }
    ],
    "createdAt": new Date(),
    "updatedAt": new Date()
  },
  {
    "serviceName": "NOMINEE_EDIT",
    "displayName": "Edit Nominee",
    "description": "Edit an existing nominee's details",
    "active": true,
    "multipart": true,
    "listBased": true,
    "targetListField": "nominees",
    "listIdentifierField": "nid",
    "allowedActions": ["EDIT"],
    "section": "NOMINEE",
    "sectionDisplayName": "Nominee Information",
    "sectionOrder": 4,
    "displayOrder": 2,
    "fields": [
      { "fieldName": "name", "label": "Nominee Name", "dataType": "TEXT", "required": true, "accountFieldPath": "name", "file": false },
      { "fieldName": "relation", "label": "Relation", "dataType": "TEXT", "required": true, "accountFieldPath": "relation", "file": false },
      { "fieldName": "nid", "label": "Nominee NID Number", "dataType": "STRING", "required": true, "accountFieldPath": "nid", "file": false },
      { "fieldName": "percentage", "label": "Percentage Share", "dataType": "NUMBER", "required": true, "accountFieldPath": "percentage", "file": false },
      { "fieldName": "mobileNumber", "label": "Mobile Number", "dataType": "NUMBER", "required": false, "accountFieldPath": "mobileNumber", "file": false },
      { "fieldName": "address", "label": "Address", "dataType": "TEXT", "required": false, "accountFieldPath": "address", "file": false },
      { "fieldName": "city", "label": "City", "dataType": "TEXT", "required": false, "accountFieldPath": "city", "file": false },
      { "fieldName": "state", "label": "State", "dataType": "TEXT", "required": false, "accountFieldPath": "state", "file": false },
      { "fieldName": "zipCode", "label": "Zip Code", "dataType": "TEXT", "required": false, "accountFieldPath": "zipCode", "file": false },
      { "fieldName": "country", "label": "Country", "dataType": "TEXT", "required": false, "accountFieldPath": "country", "file": false },
      { "fieldName": "residency", "label": "Residency", "dataType": "TEXT", "required": false, "accountFieldPath": "residency", "file": false },
      { "fieldName": "nomineeNidFront", "label": "Nominee NID Front Photo", "dataType": "FILE", "required": false, "accountFieldPath": "nomineeNidFront", "file": true },
      { "fieldName": "nomineeNidBack", "label": "Nominee NID Back Photo", "dataType": "FILE", "required": false, "accountFieldPath": "nomineeNidBack", "file": true },
      { "fieldName": "nomineePhoto", "label": "Nominee Passport Photo", "dataType": "FILE", "required": false, "accountFieldPath": "nomineePhoto", "file": true },
      { "fieldName": "nomineeSignature", "label": "Nominee Signature", "dataType": "FILE", "required": false, "accountFieldPath": "nomineeSignature", "file": true },
      { "fieldName": "minor", "label": "Is Minor", "dataType": "BOOLEAN", "required": false, "accountFieldPath": "minor", "file": false },
      { "fieldName": "guardianName", "label": "Guardian Name", "dataType": "TEXT", "required": false, "accountFieldPath": "guardianName", "file": false },
      { "fieldName": "relationshipWithNominee", "label": "Relationship with Nominee", "dataType": "TEXT", "required": false, "accountFieldPath": "relationshipWithNominee", "file": false },
      { "fieldName": "guardianNidNumber", "label": "Guardian NID Number", "dataType": "STRING", "required": false, "accountFieldPath": "guardianNidNumber", "file": false },
      { "fieldName": "guardianNidFront", "label": "Guardian NID Front", "dataType": "FILE", "required": false, "accountFieldPath": "guardianNidFront", "file": true },
      { "fieldName": "guardianNidBack", "label": "Guardian NID Back", "dataType": "FILE", "required": false, "accountFieldPath": "guardianNidBack", "file": true },
      { "fieldName": "guardianSignature", "label": "Guardian Signature", "dataType": "FILE", "required": false, "accountFieldPath": "guardianSignature", "file": true }
    ],
    "createdAt": new Date(),
    "updatedAt": new Date()
  }
]);

print("Successfully seeded ServiceRequestConfig collection with initial service definitions.");
