# Service Request Microservice

A standalone, configuration-driven Spring Boot 3.x + MongoDB microservice for managing client service change requests (Mobile Number, Email Address, Address, TIN, Nominee Add/Edit).

## Architecture Overview

```
                 FRONTEND
                    |
                    v
        SERVICE REQUEST SERVICE
                    |
          +---------+---------+
          |                   |
          v                   v
       MongoDB              MinIO
          |
          | REST (RestTemplate)
          v
   ONBOARDING SERVICE
          |
          v
   AccountDetails / Nominees / Bank
```

- **Independent Standalone Microservice**: Does NOT connect directly to Onboarding Microservice's MongoDB database.
- **Configuration-Driven Architecture**: Service definitions, display names, categories, fields, validation regex, data types, file flags, and account field mappings are stored in the MongoDB collection `ServiceRequestConfig`.
- **MinIO Storage Integration**: File fields (`tinCertificate`, `nomineeNidFront`, etc.) are uploaded to MinIO. Only object names are stored in MongoDB; presigned URLs are dynamically generated when returning request details to admin/user.
- **Audit & History**: Full audit trail stored in `ServiceRequestAudit` collection.
- **Concurrency & Old/New Value Tracking**: Resolves existing values from the Onboarding Service upon request creation, and re-verifies them upon admin approval. If account values changed prior to approval, returns `ACCOUNT_CHANGED_SINCE_REQUEST` (409 Conflict).

---

## Technical Stack

- **Java 17+**
- **Spring Boot 3.4.1**
- **Spring Data MongoDB**
- **Bean Validation (`spring-boot-starter-validation`)**
- **Lombok**
- **RestTemplate** (inter-service REST communication - NO OpenFeign)
- **MinIO Java SDK (8.5.17)**
- **Maven**
- **SLF4J Logging**

---

## Configuration Properties (`application.properties`)

```properties
server.port=8088
spring.application.name=service-request-service

# MongoDB Database Configuration
spring.data.mongodb.uri=mongodb://10.7.93.19:27017/service_request
spring.data.mongodb.database=service_request

# Onboarding Microservice Base URL
onboarding.service.url=http://10.20.242.239:9092

# MinIO File Storage Configuration
minio.endpoint=http://10.7.93.19:7000
minio.bucket=clientportal
minio.access-key=admin
minio.secret-key=bracepl@123

# File upload limits & retention
service-request.file.max-size=10MB
service-request.file.retention-days=30
```

---

## API Endpoints

### 1. Configuration API
#### GET `/service-request/config`
Retrieves available service request types grouped by category/section (e.g. Personal Details, Contact Information, Tax Information, Nominee Information).

### 2. User Submission & History APIs
#### POST `/service-request/submit`
Supports both `application/json` and `multipart/form-data`.

##### Example JSON Submission (Mobile Change):
```json
{
  "serviceName": "MOBILE_CHANGE",
  "accountId": "665f1234abcd",
  "investorCode": "INV12345",
  "mobileNumber": "01711111111",
  "email": "user@example.com",
  "action": "EDIT",
  "fields": {
    "newMobileNumber": "01822222222"
  }
}
```

##### Example Multipart Submission (TIN Change / Nominee Add):
- **Content-Type**: `multipart/form-data`
- **Part `requestData`** (JSON string):
  ```json
  {
    "serviceName": "TIN_CHANGE",
    "accountId": "665f1234abcd",
    "investorCode": "INV12345",
    "mobileNumber": "01711111111",
    "action": "EDIT",
    "fields": {
      "tinNumber": "123456789012"
    }
  }
  ```
- **Part `tinCertificate`**: File attachment (`.pdf` or `.jpg`).

#### GET `/service-request/my-requests`
Query parameters: `accountId`, `mobileNumber`, `investorCode`, `status`, `page`, `size`.

---

### 3. Admin APIs

#### GET `/admin/service-request`
Retrieves paginated service requests with filters: `status`, `serviceName`, `investorCode`, `accountId`, `mobileNumber`, `page`, `size`.

#### GET `/admin/service-request/{id}`
Returns request detail with presigned MinIO URLs for all file fields in `oldValues` and `newValues`.

#### POST `/admin/service-request/{id}/approve`
Approves a pending service request:
1. Re-fetches current account details from Onboarding Service.
2. Verifies stored `oldValues` match current account values.
3. Sends updates via `PUT /onboarding/service-request/apply`.
4. Marks request `APPROVED`.

#### POST `/admin/service-request/{id}/reject`
Rejects a pending request with remarks:
```json
{
  "reviewedBy": "admin@bracepl.com",
  "remark": "NID document details do not match account records."
}
```

---

## Sample cURL Requests

### Get Service Configurations
```bash
curl -X GET "http://localhost:8088/service-request/config"
```

### Submit Mobile Number Change Request (JSON)
```bash
curl -X POST "http://localhost:8088/service-request/submit" \
  -H "Content-Type: application/json" \
  -d '{
        "serviceName": "MOBILE_CHANGE",
        "accountId": "665f1234abcd",
        "investorCode": "INV12345",
        "mobileNumber": "01711111111",
        "email": "user@example.com",
        "action": "EDIT",
        "fields": {
          "newMobileNumber": "01822222222"
        }
      }'
```

### Submit Nominee Add Request (Multipart)
```bash
curl -X POST "http://localhost:8088/service-request/submit" \
  -F 'requestData={
        "serviceName": "NOMINEE_ADD",
        "accountId": "665f1234abcd",
        "investorCode": "INV12345",
        "mobileNumber": "01711111111",
        "action": "ADD",
        "fields": {
          "name": "John Doe",
          "relation": "Brother",
          "nid": "1234567890",
          "percentage": 50
        }
      };type=application/json' \
  -F 'nomineeNidFront=@/path/to/nid_front.jpg' \
  -F 'nomineePhoto=@/path/to/photo.jpg'
```

### Admin Approve Request
```bash
curl -X POST "http://localhost:8088/admin/service-request/SR-12345678/approve" \
  -H "Content-Type: application/json" \
  -d '{
        "reviewedBy": "admin@bracepl.com",
        "adminRemark": "Approved after document verification"
      }'
```

### Admin Reject Request
```bash
curl -X POST "http://localhost:8088/admin/service-request/SR-12345678/reject" \
  -H "Content-Type: application/json" \
  -d '{
        "reviewedBy": "admin@bracepl.com",
        "remark": "NID photo copy unclear"
      }'
```

---

## Build & Test Instructions

### Compile & Run Tests
```bash
./mvnw clean test
```

### Run Application Locally
```bash
./mvnw spring-boot:run
```
