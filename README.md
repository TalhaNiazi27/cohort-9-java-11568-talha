# Contact Management System

A full-stack web application for securely managing contacts, built as a final assignment for the 10Pearls Java Fullstack Internship (Cohort 9) by Talha Niazi.

## 🚀 Features

- **User Authentication**: Secure Registration and Login using JWT stored in HttpOnly cookies.
- **Contact Management**: Create, Read, Update, and Delete contacts with support for multiple phone numbers and emails per contact.
- **Search & Pagination**: Server-side pagination and real-time search filtering.
- **Import/Export**: Bulk import and export contacts using `.csv` and `.vcf` (vCard) formats.
- **Security**: Robust protection against CSRF and XSS attacks, with strict backend validation.
- **Modern UI**: A responsive, glassmorphism-styled dashboard built with React.

## 🛠️ Tech Stack

### Backend
- **Java 17 & Spring Boot 3**
- **Spring Security** (Stateless JWT Authentication, CSRF Protection)
- **Spring Data JPA & Hibernate**
- **H2 In-Memory Database** (for easy local setup)
- **OpenCSV & Ezvcard** (for Import/Export processing)

### Frontend
- **React.js (Vite)**
- **Axios** (Configured for credentials and XSRF tokens)
- **Custom CSS** (CSS Variables, Flexbox, Animations)

## ⚙️ Getting Started

### Prerequisites
- Java 17+ installed
- Node.js 18+ installed
- Maven (optional, wrapper is included)

### Running the Backend

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Run the Spring Boot application using the Maven wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
3. The backend API will start on `http://localhost:8080`.

### Running the Frontend

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install the dependencies:
   ```bash
   npm install
   ```
3. Start the Vite development server:
   ```bash
   npm run dev
   ```
4. Open your browser and navigate to the URL provided by Vite (usually `http://localhost:5173`).

## 🏗️ Architecture Highlights

- **Cookie-Based Authentication**: JWTs are stored in HTTP-Only, Secure cookies rather than LocalStorage to prevent XSS attacks.
- **CSRF Tokens**: The backend issues an `XSRF-TOKEN` cookie, which the Axios frontend automatically reads and returns in the `X-XSRF-TOKEN` header for all state-changing requests.
- **Global Exception Handling**: A centralized `@RestControllerAdvice` ensures consistent and secure JSON error responses across the entire API, preventing stack-trace leaks.
- **Relational Integrity**: The database uses strict foreign key constraints with orphan removal to ensure clean deletion of nested contact details (emails and phones).

## 🧪 Testing

The backend includes a comprehensive suite of JUnit 5 tests covering Repositories, Services, and Controllers (with Spring Security MockMvc).

To run the test suite:
```bash
cd backend
./mvnw test
```
