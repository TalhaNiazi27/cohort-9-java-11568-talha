# Contact Manager Project Plan

# Week 6: Full Integration Polish & Documentation - Implementation Plan

## Goal Description
Enhance the UI/UX with professional polish (transitions, animations, loading states) and produce comprehensive documentation and presentation materials so you are fully prepared to present this project to your mentors.

## Proposed Changes

### UI & Aesthetics Polish
- Add loading skeletons to the Dashboard to replace the text "Loading contacts..."
- Add subtle hover states and micro-interactions (e.g., scale-up effect on buttons, row highlighting in tables).
- Ensure all color variables are strictly adhered to across light/dark contexts.

### Documentation & Presentation
- **[NEW] walkthrough.md**: A detailed breakdown of the architecture, key features, and visual evidence (screenshots/recordings) of the app functioning.
- **[NEW] presentation_guide.md**: An "Explain Like I'm 5" presentation script designed specifically for presenting your internship project. It will cover the "Why", "What", and "How" in an easy-to-digest format.

## Open Questions

> [!IMPORTANT]
> **Aesthetics**: Are there any specific animations or styling effects (e.g., glassmorphism intensity, specific colors) you want prioritized for the final polish?

> [!TIP]
> **Presentation**: Do you know how much time you have for your final presentation? I can tailor the script length accordingly.

## Verification Plan

### Manual Verification
- Visual inspection of all hover states, animations, and loading skeletons.
- Review of the generated markdown files to ensure they are accurate, professional, and easy to read.

---

# Contact Management System (CMS) Implementation Plan

This project implements a secure, web-based contact management system using a Spring Boot backend, a SQL database, and a React.js frontend. It enables users to register, log in, manage their profile, and perform full CRUD operations on contacts with paginated listings, search filters, and import/export capabilities (CSV and vCard).

The repository has been successfully cloned and is currently empty, meaning we will initialize both the backend and frontend from scratch using a dual-folder structure:
* `backend/` (Spring Boot Java Application)
* `frontend/` (React.js SPA)

---

## User Review Required & Design Choices

> [!IMPORTANT]
> **CodeRabbit Quality Rules Added:**
> According to `.coderabbit.yaml`, the automated reviewer has custom validation rules:
> 1. **Strict Exception Handling:** Any missing exception handling is flagged as **HIGH**. We must ensure all potentially failing operations (database queries, network requests, JSON parsing, file I/O) are properly wrapped in `try-catch` blocks, validated, and managed via a global `@ControllerAdvice` handler.
> 2. **Generics & Typing:** Raw types and unchecked casts are flagged as **MEDIUM**. We will strictly use Java generics (e.g., `ResponseEntity<UserResponse>` instead of raw `ResponseEntity`, `List<Contact>` instead of raw `List`).
> 3. **OOP Principles:** CodeRabbit reviews code for proper Object-Oriented Programming (OOP) design. We will use interfaces for services, strict encapsulation (private fields with getters/setters), and separate data models (DTOs) from database entities.

---

## Technical Design & Architecture

### Database Schema Design
We will structure the schema with proper relational design using cascade deletes to clean up orphans.

```mermaid
erDiagram
    USERS {
        Long id PK
        String email UNIQUE "Optional if Phone used"
        String phone UNIQUE "Optional if Email used"
        String passwordHash
        DateTime createdAt
    }
    CONTACTS {
        Long id PK
        Long userId FK
        String firstName
        String lastName
        String title
        DateTime createdAt
        DateTime updatedAt
    }
    EMAILS {
        Long id PK
        Long contactId FK
        String emailAddress
        String label "e.g., Work, Personal"
    }
    PHONES {
        Long id PK
        Long contactId FK
        String phoneNumber
        String label "e.g., Work, Home, Personal"
    }
    
    USERS ||--o{ CONTACTS : "owns"
    CONTACTS ||--o{ EMAILS : "has"
    CONTACTS ||--o{ PHONES : "has"
```

### Folder Structure Layout
```text
/ (Workspace Root)
├── .coderabbit.yaml
├── README.md
├── backend/            <-- Spring Boot Maven Project
│   ├── src/
│   └── pom.xml         <-- Backend Maven configuration
└── frontend/           <-- React.js Vite Project
    ├── src/
    └── package.json    <-- Frontend configuration
```

### REST API Endpoints

#### Authentication & Profile
- `POST /api/auth/register` - Create a new user account (using email or phone).
- `POST /api/auth/login` - Authenticate user and return JWT token.
- `POST /api/auth/logout` - Invalidate current token.
- `GET /api/auth/me` - Retrieve current logged-in user profile.
- `POST /api/auth/change-password` - Reset password (modal action).

#### Contact Management
- `GET /api/contacts?page=0&size=10&search=john` - Paginated and searchable list of contacts.
- `POST /api/contacts` - Create a new contact.
- `GET /api/contacts/{id}` - View detailed contact card.
- `PUT /api/contacts/{id}` - Update contact details.
- `DELETE /api/contacts/{id}` - Delete contact.

#### Import & Export
- `GET /api/contacts/export/csv` - Export contacts as CSV file.
- `GET /api/contacts/export/vcf` - Export contacts as vCard (.vcf) format.
- `POST /api/contacts/import` - Upload CSV or .vcf file to bulk-import contacts.

---

## Proposed Weekly Roadmap (Git Branch Strategy)

We will open a separate Git branch and Pull Request for each phase of the project:

### Week 1 (July 18 – July 25): Init & Authentication Backend
* **Branch:** `feature/setup-auth-backend`
* **Tasks:**
  * Initialize the Spring Boot backend framework under `/backend`.
  * Set up JPA configurations, H2/SQL Server profile setup, and security configurations.
  * Create `User` entity, repository, and service.
  * Implement Backend Registration & Login APIs (using BCrypt for passwords and JWT for stateless auth).
  * Build the `@ControllerAdvice` global exception handler to meet CodeRabbit's strict requirements.
  * Write unit tests for authentication logic (JUnit 5 + Mockito).

### Week 2 (July 25 – August 1): Contacts Backend CRUD & Pagination
* **Branch:** `feature/contacts-backend`
* **Tasks:**
  * Create entities for `Contact`, `Email`, and `Phone`.
  * Set up database relational mapping (`@OneToMany` with orphan removal).
  * Implement Paginated Search endpoints using Spring Data JPA Specifications.
  * Write controllers, services, and DTOs with thorough exception validation.
  * Write integration and unit tests for contacts management backend.

### Week 3 (August 1 – August 8): React Frontend Setup & Authentication
* **Branch:** `feature/frontend-auth`
* **Tasks:**
  * Initialize React.js (via Vite) under `/frontend`.
  * Design a beautiful, custom CSS design system (Tailored Theme, Light/Dark support, sleek cards, layout components).
  * Implement API connection Layer (Axios/Fetch) with token interceptors.
  * Create Login, Register, and Profile screens with forms, state validation, and error handles.

### Week 4 (August 8 – August 15): Contact Management Dashboard
* **Branch:** `feature/frontend-contacts`
* **Tasks:**
  * Build the Dashboard workspace with total contact count, label distribution graphs/widgets, and paginated table list.
  * Create interactive Modals for creating contacts, updating contacts (pre-populated forms), and deleting contacts (confirmation popup).
  * Wire search bar to trigger search API queries.

### Week 5 (August 15 – August 22): Advanced Features (Import/Export) & Quality Control
* **Branch:** `feature/import-export-sonarqube`
* **Tasks:**
  * Write backend parser services for CSV files and standard vCard (.vcf) formats.
  * Implement frontend import/export controls (drag-and-drop file upload, export buttons).
  * Conduct code quality verification, SonarQube rule checks, and fix any warning issues flagged.

### Week 6 (August 22 – August 29): Full Integration Polish & Documentation
* **Branch:** `feature/polish-docs`
* **Tasks:**
  * Add transitions, hover animations, loading skeletons, and refine aesthetics.
  * Create a comprehensive `walkthrough.md` with walkthrough videos and documentation.
  * Produce the "Explain Like I'm 5" presentation guide so you can present the code to your mentors effortlessly.

---

## Verification Plan

### Automated Tests
* Run JUnit & Mockito backend unit tests: `cd backend && ./mvnw test`.
* Run frontend unit/compilation checks.

### Manual Verification
* Visual verification of registration, login, and UI states.
* Export and import files and verify they render correctly in Microsoft Excel and Apple/Google Contacts.
