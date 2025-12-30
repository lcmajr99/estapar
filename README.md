# 🚗 Parking Event-Driven Backend

Backend desenvolvido em **Java + Spring Boot**, orientado a eventos, para simular e gerenciar o fluxo de veículos em um sistema de estacionamento urbano.

---

## 🧱 Tech Stack

- **Java 21**
- **Spring Boot 3**
- **Spring Web**
- **Spring Data JPA**
- **Hibernate**
- **PostgreSQL**
- **JUnit 5**
- **Mockito**
- **JaCoCo**
- **SonarCloud**
- **Maven**
- **Docker**
- **GitHub Actions**

---

## 📖 Project Description

The **Parking Event-Driven Backend** is a backend application designed to handle a real-world parking system scenario using an **event-driven architecture**.

The system receives events from an external simulator (via webhook) that represent the lifecycle of a vehicle inside a parking facility. These events are processed to maintain logical and physical consistency, apply business rules, calculate pricing, and generate revenue reports.

The project was built focusing on:

- Clean architecture
- Clear separation of responsibilities
- Transactional consistency
- Testability
- Code quality
- CI/CD integration

---

## 🧭 Event Flow

The system processes three main event types:

### ENTRY
- Creates a new parking session
- Validates idempotency
- Selects an available sector
- Applies dynamic pricing based on occupancy
- Does not assign a physical parking spot

### PARKED
- Resolves the real parking spot using GPS coordinates
- Reconciles logical and physical sectors
- Recalculates pricing if sector changes
- Handles physical conflicts
- Confirms parking state

### EXIT
- Calculates total parking time
- Applies free tolerance period (30 minutes)
- Calculates final amount
- Releases parking spot and sector occupancy
- Finalizes the parking session

---

## 💰 Revenue Module

The project exposes a revenue calculation feature that allows:

- Revenue lookup by date
- Optional filtering by sector
- Standardized response containing:
  - total amount
  - currency
  - timestamp

This logic is isolated in a dedicated service, following the **Single Responsibility Principle**.

---

## 🧩 Architecture Overview

The application avoids monolithic services by splitting responsibilities into dedicated components:

- `ParkingService` – Event router
- `EntryEventHandler` – Entry logic
- `ParkedEventHandler` – Physical parking logic
- `ExitEventHandler` – Exit and billing logic
- `RevenueService` – Revenue calculation
- Repository layer per aggregate

This approach ensures:
- Proper transactional boundaries
- No self-invocation issues with Spring proxies
- Easier unit testing
- Safer future evolution

---

## 🧪 Testing & Quality

- Unit tests per event handler
- Routing tests for event dispatching
- Code coverage generated with **JaCoCo**
- Static analysis and Quality Gate via **SonarCloud**
- Automated CI pipeline with **GitHub Actions**

---

## 🚀 CI/CD

On every **push** or **pull request**, the pipeline executes:

1. Build
2. Test execution
3. Coverage generation
4. SonarCloud analysis

The pipeline can be configured to block merges if quality gates fail.

---

## 🎯 Project Goal

This project was developed to demonstrate:

- Backend best practices
- Event-driven architecture
- Correct use of Spring transactions
- Clean and maintainable code
- Professional CI/CD and code quality workflow

---

## 👨‍💻 Developer

**Luis Carlos**  
Backend Developer  
Java • Spring Boot • Event-Driven Architecture
