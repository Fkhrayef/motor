# motor

**Version:** 1.0
**Developers:**  Faisal Al Khrayef - Abdulaziz Alharbi - Mshari Alshammari

**Tech Stack:**

* **Backend:** Java 17, Spring Boot
* **Database:** MySQL (AWS RDS)
* **ORM:** Hibernate with JPA
* **API & Integrations:** REST APIs, OpenAI, Moyasar (Payments + Webhook), WhatsApp, Email
* **Tools:** Postman, Maven
* **Deployment:** AWS (Elastic Beanstalk, ECS)

---

## Overview

Motor is a car management system that leverages Artificial Intelligence (AI) and Retrieval-Augmented Generation (RAG) to help users manage and maintain their vehicles.
Users can ask questions about their cars directly to the AI. Using RAG, the system first retrieves the most relevant information from the car’s catalogue (or a user-uploaded one) and then generates clear, accurate answers.
The AI can also generate maintenance tasks and schedules for each vehicle, with timely reminders sent via WhatsApp or Email.
Platform Features
Create personal accounts and update profile information.
Add and manage cars based on subscription plans.
Transfer vehicle ownership to another user, along with the complete service history.

---

## Core Modules & Endpoints I worked on

| Module              | Endpoint                   | Description                              | Contrubitor |
|---------------------| -------------------------- | ---------------------------------------- |-------------|
| **AI Integeration** | `validateBusinessIdea`     | Validate startup ideas                   | Faisal      |
| **Payment**         | `processPayment`           | Initiate payments                        | Mshari      |
| **Startup**         | `addFounderToStartup`      | Add a founder or co-founder to a startup | Abdulaziz   |

---

## APIs Used

| API             | Purpose                                                  |
|-----------------|----------------------------------------------------------|
| OpenAI          | AI advice, insights, guidance |
| Moyasar         | Payment processing                                       |
| Moyasar Webhook | Payment callbacks and subscription handling              |
| WhatsApp        | Sends whatsApp written messages                          |
| Email           | Sends email written messages                             |


---

## Entities

* **User** 
* **Car**
* **Maintenance**
* **Reminder**
* **Marketing**
* **Payment**
* **Subscription**
* **CarTransferRequest**

---

## DB Diagram

<img width="1113" height="627" alt="image" src="https://github.com/user-attachments/assets/ef584b28-8ecd-41ed-80ad-dccc93c792d8" />

---

## Notes

* Payment and subscription handling fully integrated with Moyasar.
* AI-powered guidance leverages OpenAI APIs for personalized advice.
* Endpoints are RESTful and tested via Postman.



