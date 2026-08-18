<!-- Sync Impact Report
Version: 1.0.0 (Initial adoption)
Ratification: 2026-08-18
Status: ADOPTED - Complete constitution for Inventory Tracker project

This constitution establishes four core principles governing the design and implementation of the Inventory Tracker web application, along with explicit governance and amendment procedures.
-->

# Inventory Tracker Constitution

## Technical Overview

Inventory Tracker is a web application for managing private personal or small-business inventory. The system consists of a browser-based frontend, a backend application exposing the application API, and a relational database for persistent data. The backend is also responsible for enforcing business rules, user data isolation, and real-time low-stock notifications.

## Core Principles

### I. Simplicity First

The application MUST remain intentionally simple.

- Implement only behavior required by the current specification.
- Prefer straightforward solutions over generalized or highly abstract designs.
- Prefer built-in framework capabilities over custom wrappers or infrastructure.
- Do not introduce additional architectural layers, indirection, factories, generic frameworks, or extension mechanisms without a concrete requirement.
- Do not add speculative functionality for possible future use.
- When several solutions satisfy the requirement, prefer the one with fewer concepts, dependencies, and moving parts.

Complexity is acceptable only when it solves an existing requirement that cannot be handled cleanly by the simpler design.

### II. Technology Stack

The project MUST use the following primary technology stack:

- **Backend:** Java with Spring Boot.
- **Database:** PostgreSQL.
- **Database migrations:** Flyway. Database schema changes MUST be represented by versioned Flyway migrations rather than manual schema changes.
- **Persistence access:** Spring Data repositories.
- **Frontend:** Angular.

New major frameworks or infrastructure components MUST NOT be introduced unless required by a concrete feature or technical constraint.

### III. Backend Layered Package Structure

Backend code MUST be organized by technical layer. The primary packages are:

- **`model`** — API-facing data transfer objects.
  - Request-only objects SHOULD be named `SomethingRequestDTO`.
  - Response-only objects SHOULD be named `SomethingResponseDTO`.
  - Objects intentionally used in both directions MAY be named `SomethingDTO`.
  - DTOs MAY contain simple declarative input validation, such as Jakarta Validation annotations.
  - DTOs MUST NOT contain business logic.

- **`entity`** — domain and database entities.
  - A domain concept persisted in the database MUST use a single entity class.
  - Do not create separate persistence-entity and domain-model classes for the same concept.
  - Entities MAY contain state and behavior that naturally belong to that entity, but orchestration of application use cases belongs in services.

- **`controller`** — REST API entry points.
  - Controllers MUST accept and validate HTTP requests, map request data as needed, call services, and return HTTP responses.
  - Controllers MUST NOT contain core business logic or perform direct repository access.

- **`service`** — application and business logic.
  - Services MUST implement business rules and coordinate application operations.
  - Services MAY call repositories and other services where required.
  - Transaction boundaries SHOULD normally be defined at the service layer.

- **`repository`** — persistence access.
  - Repositories MUST use Spring Data.
  - Repositories are responsible for persistence queries and data access, not business-rule orchestration.

The normal dependency direction is:

`controller -> service -> repository`

DTOs from `model` define API boundaries, while `entity` classes represent persisted domain state. Additional backend layers or parallel representations of the same domain model MUST NOT be introduced without explicit justification.

### IV. Centralized Exception Processing

Backend HTTP exception handling MUST be centralized in a single Spring `@ControllerAdvice` class.

- Controllers MUST NOT duplicate exception-to-response mapping.
- Services and other backend components SHOULD throw meaningful application/domain exceptions and allow the centralized advice to translate them into HTTP error responses.
- Repeated `try/catch` blocks used only to construct HTTP responses SHOULD be avoided.
- Validation failures, business-rule violations, missing resources, access violations, and unexpected server errors SHOULD be converted into consistent API error responses by the same advice.
- If a dedicated error DTO is needed, it MUST follow the DTO naming and placement rules defined for the `model` package.

## Architecture Constraints

All feature specifications and implementation plans MUST preserve the architecture described by this constitution unless the constitution is explicitly amended first.

The design MUST favor direct use of Spring Boot, Spring Data, PostgreSQL, Flyway, and Angular capabilities. Additional abstractions are not considered improvements by default; every new abstraction MUST have a concrete purpose in the current application.

The backend MUST remain a conventional layered application. Feature work MUST NOT silently replace the layered structure with hexagonal architecture, clean architecture, CQRS, event sourcing, separate domain/persistence models, microservices, or similar architectural patterns unless this constitution is amended to require them.

## Governance

This constitution defines project-wide rules that apply to specifications, plans, tasks, and implementation.

- New work MUST be checked for compliance with these principles before implementation.
- A feature specification or implementation plan MUST NOT override the constitution implicitly.
- Any intentional exception to these rules MUST be documented and justified before implementation.
- Changes to these principles require an explicit constitution amendment.
- Constitution versions follow semantic versioning:
  - **MAJOR** — removal or incompatible redefinition of an existing principle.
  - **MINOR** — addition of a new principle or material expansion of project governance.
  - **PATCH** — clarification or wording change that does not alter the architectural intent.

**Version:** 1.0.0  
**Ratified:** 2026-08-18  
**Last Amended:** 2026-08-18
