# Motor

**Version:** 1.0  
**Developers:** Faisal Al Khrayef - Abdulaziz Alharbi - Mshari Alshammari

## Tech Stack

- **Backend:** Java 17, Spring Boot
- **Database:** MySQL (AWS RDS)
- **ORM:** Hibernate with JPA
- **API & Integrations:** REST APIs, OpenAI, Moyasar (Payments + Webhook), WhatsApp, Email
- **Tools:** Postman, Maven
- **Deployment:** AWS (Elastic Beanstalk, ECS)

---

## Overview

Motor is a comprehensive car management system that leverages Artificial Intelligence (AI) and Retrieval-Augmented Generation (RAG) to help users manage and maintain their vehicles intelligently.

### Key Features

- **AI-Powered Car Assistance**: Users can ask questions about their cars directly to the AI. Using RAG, the system retrieves relevant information from car manuals and generates accurate, contextual answers.
- **Smart Maintenance Management**: AI generates personalized maintenance tasks and schedules for each vehicle with timely reminders sent via WhatsApp or Email.
- **Subscription-Based Access**: AI features require Pro or Enterprise subscriptions, ensuring premium service quality.
- **Vehicle Ownership Transfer**: Complete service history transfers with vehicle ownership changes.
- **Document Management**: Upload and manage car manuals, insurance documents, and registration files.

---

## API Endpoints

The application provides **71 RESTful endpoints** across 10 controllers:

| #   | Method | Endpoint | Contributor |
| --- | ------ | -------- | ----------- |
| 1   | GET    | `/api/v1/users/get` | Faisal |
| 2   | POST   | `/api/v1/users/register` | Faisal |
| 3   | PUT    | `/api/v1/users/update/{id}` | Faisal |
| 4   | DELETE | `/api/v1/users/delete/{id}` | Abdulaziz |
| 5   | POST   | `/api/v1/users/upload-license/{id}` | Faisal |
| 6   | GET    | `/api/v1/users/download-license/{id}` | Faisal |
| 7   | DELETE | `/api/v1/users/delete-license/{id}` | Faisal |
| 8   | GET    | `/api/v1/users/{id}/subscription` | Abdulaziz |
| 9   | DELETE | `/api/v1/users/{id}/card` | Abdulaziz |
| 10  | GET    | `/api/v1/cars/get` | Faisal |
| 11  | POST   | `/api/v1/cars/add` | Abdulaziz |
| 12  | PUT    | `/api/v1/cars/update/{id}` | Faisal |
| 13  | PUT    | `/api/v1/cars/update/{carId}/mileage` | Mshari |
| 14  | DELETE | `/api/v1/cars/delete/{id}` | Abdulaziz |
| 15  | GET    | `/api/v1/cars/get/user` | Abdulaziz |
| 16  | POST   | `/api/v1/cars/upload-registration/{id}` | Faisal |
| 17  | GET    | `/api/v1/cars/download-registration/{id}` | Faisal |
| 18  | DELETE | `/api/v1/cars/delete-registration/{id}` | Faisal |
| 19  | POST   | `/api/v1/cars/upload-insurance/{id}` | Faisal |
| 20  | GET    | `/api/v1/cars/download-insurance/{id}` | Faisal |
| 21  | DELETE | `/api/v1/cars/delete-insurance/{id}` | Faisal |
| 22  | GET    | `/api/v1/cars/maintenance-cost/{make}/{model}/yearly` | Abdulaziz |
| 23  | GET    | `/api/v1/cars/visit-frequency/{make}/{model}` | Abdulaziz |
| 24  | GET    | `/api/v1/cars/typical-mileage/{make}/{model}` | Abdulaziz |
| 25  | GET    | `/api/v1/cars/numbers/{userId}` | Abdulaziz |
| 26  | PUT    | `/api/v1/cars/{userId}/enforce-access` | Abdulaziz |
| 27  | GET    | `/api/v1/maintenances/get` | Faisal |
| 28  | POST   | `/api/v1/maintenances/add/{carId}` | Faisal |
| 29  | PUT    | `/api/v1/maintenances/update/{id}` | Faisal |
| 30  | DELETE | `/api/v1/maintenances/delete/{id}` | Abdulaziz |
| 31  | GET    | `/api/v1/maintenances/get/{carId}` | Abdulaziz |
| 32  | POST   | `/api/v1/maintenances/upload-invoice/{id}` | Faisal |
| 33  | GET    | `/api/v1/maintenances/download-invoice/{id}` | Faisal |
| 34  | DELETE | `/api/v1/maintenances/delete-invoice/{id}` | Faisal |
| 35  | GET    | `/api/v1/reminders/get` | Faisal |
| 36  | POST   | `/api/v1/reminders/add/{carId}` | Faisal |
| 37  | PUT    | `/api/v1/reminders/update/{id}` | Faisal |
| 38  | DELETE | `/api/v1/reminders/delete/{id}` | Faisal |
| 39  | GET    | `/api/v1/reminders/get/{carId}` | Faisal |
| 40  | POST   | `/api/v1/reminders/generate-maintenance/{carId}` | Faisal |
| 41  | POST   | `/api/v1/payments/card` | Mshari |
| 42  | POST   | `/api/v1/payments/subscription/user/{userId}/plan/`<br>`{planType}/billing/{billingCycle}` | Faisal |
| 43  | GET    | `/api/v1/payments/callback` | Faisal |
| 44  | POST   | `/api/v1/payments/webhook` | Faisal |
| 45  | GET    | `/api/v1/payments/status/{paymentId}` | Mshari |
| 46  | GET    | `/api/v1/payments/payment/{paymentId}` | Mshari |
| 47  | POST   | `/api/v1/payments/subscription/{userId}/cancel` | Faisal |
| 48  | GET    | `/api/v1/payments/subscription/{userId}/status` | Faisal |
| 49  | GET    | `/api/v1/payments/subscription/expiring` | Faisal |
| 50  | POST   | `/api/v1/car-ai/upload-manual/{carId}` | Faisal |
| 51  | POST   | `/api/v1/car-ai/ask/{carId}` | Faisal |
| 52  | GET    | `/api/v1/car-ai/car/{carId}/info` | Faisal |
| 53  | GET    | `/api/v1/car-ai/admin/documents` | Faisal |
| 54  | GET    | `/api/v1/car-ai/admin/search` | Faisal |
| 55  | GET    | `/api/v1/car-ai/admin/check-document-exists` | Faisal |
| 56  | POST   | `/api/v1/transfer-requests/{id}/accept` | Mshari |
| 57  | POST   | `/api/v1/transfer-requests/{id}/reject` | Mshari |
| 58  | POST   | `/api/v1/transfer-requests/{id}/cancel` | Mshari |
| 59  | GET    | `/api/v1/transfer-requests/{id}` | Mshari |
| 60  | GET    | `/api/v1/transfer-requests/incoming` | Mshari |
| 61  | GET    | `/api/v1/transfer-requests/outgoing` | Mshari |
| 62  | GET    | `/api/v1/transfer-requests/by-car/{carId}` | Mshari |
| 63  | GET    | `/api/v1/transfer-requests/by-status/{status}` | Mshari |
| 64  | POST   | `/api/v1/transfer-requests/direct/{carId}`<br>`/{toEmail}/{toPhone}` | Mshari |
| 65  | GET    | `/api/v1/marketing/get` | Mshari |
| 66  | POST   | `/api/v1/marketing/add` | Mshari |
| 67  | PUT    | `/api/v1/marketing/update/{id}` | Mshari |
| 68  | DELETE | `/api/v1/marketing/delete/{id}` | Mshari |
| 69  | GET    | `/api/v1/email/test` | Mshari |
| 70  | POST   | `/api/v1/s3/upload` | Faisal |
| 71  | GET    | `/api/v1/s3/download/{filename}` | Faisal |

---

## APIs Used

| API                 | Purpose                                               |
| ------------------- | ----------------------------------------------------- |
| **OpenAI**          | AI advice, insights, and guidance                     |
| **Moyasar**         | Payment processing                                    |
| **Moyasar Webhook** | Payment callbacks and subscription handling           |
| **WhatsApp**        | Sends WhatsApp written messages                       |
| **Email**           | Sends email written messages                          |
| **RAG (FastAPI)**   | Document processing and AI-powered question answering |

---

## Core Features

### 🤖 AI Features (Subscription Required)

- **Manual Upload**: Upload car manuals for AI processing
- **Question Answering**: Ask AI questions about your car using uploaded manuals
- **Maintenance Generation**: AI generates personalized maintenance schedules
- **Document Management**: Check if manuals exist for specific cars

### 🚗 Car Management

- Add, update, and delete cars
- Upload/download registration and insurance documents
- Track mileage and maintenance costs
- Get car statistics

### 💳 Payment & Subscription

- Pro and Enterprise subscription plans
- Moyasar payment integration
- Subscription management and cancellation
- Payment status tracking

### 🔄 Vehicle Transfer

- Transfer car ownership between users
- Complete service history transfer
- Accept/reject transfer requests
- Direct transfers via email/phone

### 📋 Maintenance & Reminders

- Add manual maintenance records
- Upload/download maintenance invoices
- Generate AI-powered maintenance reminders
- WhatsApp and email notifications

---

## Database Entities

- **User** - User accounts and profiles
- **Car** - Vehicle information and documents
- **Maintenance** - Service records and invoices
- **Reminder** - Maintenance reminders and notifications
- **Marketing** - Promotional content and offers
- **Payment** - Payment transactions and history
- **Subscription** - User subscription plans and status
- **CarTransferRequest** - Vehicle ownership transfer requests

---

## Database Schema

<img width="1113" height="627" alt="Database Schema" src="https://github.com/user-attachments/assets/ef584b28-8ecd-41ed-80ad-dccc93c792d8" />

---

## Use Case Diagram

<img width="1253" height="1374" alt="Motor Use Case Diagram" src="https://github.com/user-attachments/assets/5af9bc9a-2222-4d5d-8fe7-b34bd874681a" />

---

## UI Design

**Figma Prototype**: [Motor Design System](https://www.figma.com/proto/VxHscp7GQyGOPRy0FQYy6E/Motor?page-id=0%3A1&node-id=1-2&p=f&viewport=25591%2C802%2C0.48&t=X96C0JFMyio1af9B-1&scaling=contain&content-scaling=fixed&starting-point-node-id=1%3A2)

---

## Security & Access Control

- **Authentication**: JWT-based user authentication
- **Authorization**: Role-based access control (USER/ADMIN)
- **Subscription Validation**: AI features require active Pro/Enterprise subscriptions
- **Data Protection**: Users can only access their own cars and data
- **Secure Payments**: Moyasar integration with webhook validation

---

## Key Notes

- ✅ **Payment Integration**: Fully integrated with Moyasar for subscription management
- ✅ **AI-Powered Features**: OpenAI and RAG APIs provide intelligent car assistance
- ✅ **Subscription Model**: Pro and Enterprise plans with AI feature access control
- ✅ **RESTful Design**: All endpoints follow REST conventions and are tested via Postman
- ✅ **Document Management**: S3 integration for secure file storage
- ✅ **Notification System**: WhatsApp and email integration for reminders
- ✅ **Vehicle Transfer**: Complete ownership transfer with service history

---

## Getting Started

1. **Clone the repository**
2. **Set up MySQL database** (AWS RDS recommended)
3. **Configure environment variables** for APIs and database
4. **Run the Spring Boot application**
5. **Test endpoints** using Postman collection

---

_Built with ❤️ by Faisal Al Khrayef, Abdulaziz Alharbi, and Mshari Alshammari_

