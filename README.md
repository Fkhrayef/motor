# Motor

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

## Usecase Diagram

<img width="1253" height="1374" alt="Motor_Use_Case_Diagram" src="https://github.com/user-attachments/assets/5af9bc9a-2222-4d5d-8fe7-b34bd874681a" />

---

## Figma Design

Link: https://www.figma.com/proto/VxHscp7GQyGOPRy0FQYy6E/Motor?page-id=0%3A1&node-id=1-2&p=f&viewport=25591%2C802%2C0.48&t=X96C0JFMyio1af9B-1&scaling=contain&content-scaling=fixed&starting-point-node-id=1%3A2

---

## Notes

* Payment and subscription handling fully integrated with Moyasar.
* AI-powered guidance leverages OpenAI APIs for personalized advice.
* Endpoints are RESTful and tested via Postman.




