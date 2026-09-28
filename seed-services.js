// MongoDB initialization / seed script for 'services' collection
// Compatible with ServiceCategory, ServiceRequestDefinition, FormField, and RequestBodyConfig entities.

// Replace 'your_database_name' with your actual database name if executing via mongosh:
// use your_database_name;

db.services.drop();

db.services.insertMany([
  {
    "_id": ObjectId("69f9c60043f263b26a7a6f2e"),
    "name": "Personal Information",
    "order": 1,
    "active": true,
    "description": "Update your personal information",
    "icon": "USER",
    "_class": "com.beacepl.service_request_service.entity.ServiceCategory",
    "serviceRequestList": [
      {
        "name": "Address",
        "order": 1,
        "active": true,
        "action": "UPDATE",
        "apiEndpoint": "http://localhost:8085/service-request/submit",
        "description": "Update permanent address, present address",
        "formFields": [
          {
            "name": "Present Address",
            "parameterName": "addressLine1",
            "uiType": "text",
            "required": true
          },
          {
            "name": "Utility Bill Copy",
            "parameterName": "billCopy",
            "uiType": "image",
            "required": false
          }
        ],
        "body": {
          "type": "POST",
          "requiredFields": [
            "mobileNumber",
            "email",
            "serviceName",
            "request",
            "otp"
          ]
        }
      },
      {
        "name": "Personal Information",
        "order": 2,
        "active": true,
        "action": "UPDATE",
        "apiEndpoint": "http://localhost:8085/service-request/submit",
        "description": "Update name, date of birth, father' name, mother's name",
        "formFields": [
          {
            "name": "Name",
            "parameterName": "name",
            "uiType": "text",
            "required": true
          },
          {
            "name": "Date of Birth",
            "parameterName": "dateOfBirth",
            "uiType": "text",
            "required": true
          },
          {
            "name": "Father's Name",
            "parameterName": "fathersName",
            "uiType": "text",
            "required": false
          },
          {
            "name": "Mother's Name",
            "parameterName": "mothersName",
            "uiType": "text",
            "required": false
          }
        ],
        "body": {
          "type": "POST",
          "requiredFields": [
            "mobileNumber",
            "email",
            "serviceName",
            "request",
            "otp"
          ]
        }
      }
    ]
  },
  {
    "_id": ObjectId("6a02bbac70ae07f7967988fa"),
    "name": "Bank Information",
    "order": 2,
    "active": true,
    "description": "Update bank and account details",
    "icon": "BANK",
    "_class": "com.beacepl.service_request_service.entity.ServiceCategory",
    "serviceRequestList": [
      {
        "name": "Bank Information",
        "order": 1,
        "active": true,
        "action": "UPDATE",
        "apiEndpoint": "http://localhost:8085/service-request/submit",
        "description": "Update bank, branch, account number",
        "formFields": [
          {
            "name": "Bank Name",
            "parameterName": "bankName",
            "uiType": "text",
            "required": true
          },
          {
            "name": "Account Number",
            "parameterName": "accountNo",
            "uiType": "text",
            "required": true
          },
          {
            "name": "Branch Name",
            "parameterName": "branchName",
            "uiType": "text",
            "required": true
          },
          {
            "name": "Routing Number",
            "parameterName": "routingNumber",
            "uiType": "text",
            "required": true
          },
          {
            "name": "Upload Cheque Leaf",
            "parameterName": "chequeLeaf",
            "uiType": "image",
            "required": false
          }
        ],
        "body": {
          "type": "POST",
          "requiredFields": [
            "mobileNumber",
            "email",
            "serviceName",
            "request",
            "otp"
          ]
        }
      }
    ]
  },
  {
    "_id": ObjectId("6a741095c1fe9ad3676772db"),
    "name": "Contact Information",
    "order": 3,
    "active": true,
    "description": "Update your email address, mobile number",
    "icon": "CONTACT",
    "_class": "com.beacepl.service_request_service.entity.ServiceCategory",
    "serviceRequestList": [
      {
        "name": "Mobile Number",
        "order": 1,
        "active": true,
        "action": "UPDATE",
        "apiEndpoint": "http://localhost:8085/service-request/submit",
        "description": "Update your mobile number",
        "formFields": [
          {
            "name": "Mobile Number",
            "parameterName": "mobileNumber",
            "uiType": "text",
            "required": true
          }
        ],
        "body": {
          "type": "POST",
          "requiredFields": [
            "mobileNumber",
            "email",
            "serviceName",
            "request",
            "otp"
          ]
        }
      },
      {
        "name": "Email Address",
        "order": 2,
        "active": true,
        "action": "UPDATE",
        "apiEndpoint": "http://localhost:8085/service-request/submit",
        "description": "Update your email address",
        "formFields": [
          {
            "name": "Email Address",
            "parameterName": "emailAddress",
            "uiType": "text",
            "required": true
          }
        ],
        "body": {
          "type": "POST",
          "requiredFields": [
            "mobileNumber",
            "email",
            "serviceName",
            "request",
            "otp"
          ]
        }
      }
    ]
  }
]);

print("Successfully inserted service categories into 'services' collection!");
