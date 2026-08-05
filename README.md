# Carpenter Business Backend

Java 21 / Spring Boot 3.5 REST API using PostgreSQL, Flyway, JPA, Spring Security, OpenAPI and Actuator.

## Profiles and configuration

- `local` (default): local database, non-secure cookie, optional development seed users
- `test`: Testcontainers properties supplied by tests; no seed data
- `prod`: secure session cookie and no seed data

Datasource settings use `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD`. `FRONTEND_ORIGIN` controls the sole credentialed CORS origin. `FILE_UPLOAD_DIRECTORY` is reserved for the upcoming service-request attachment slice.

## Run and test

```bash
./mvnw spring-boot:run
./mvnw test
```

Docker must be available for PostgreSQL Testcontainers tests; otherwise JUnit skips them. Build an image with `docker build -t carpenter-backend .`.

## Database

Migrations are in `src/main/resources/db/migration`. Flyway is the schema authority; JPA validates mappings with `ddl-auto=validate`. Never amend an applied migration—add a versioned migration.

## API and authentication

All application endpoints use `/api`. Authentication is an HTTP session held by an HTTP-only cookie. POST/PUT/PATCH/DELETE requests after login require the CSRF header produced from `XSRF-TOKEN`. Registration and login establish the session; logout invalidates it. Passwords are BCrypt hashes and never appear in DTOs.

Swagger UI is `/swagger-ui.html`, OpenAPI JSON is `/v3/api-docs`, and health is `/actuator/health`. Responses use DTO records. Errors follow the documented `ApiError` contract.

## Package structure

Features (`auth`, `user`, `customer`, `servicerequest`, `appointment`, `quotation`, `order`, `project`, `invoice`, `payment`, `supplier`, `inventory`, `expense`, `reporting`, `notification`, `document`, `audit`) own their entities, repositories, services and DTOs. Cross-cutting configuration is in `config`, authentication helpers in `security`, shared persistence in `common`, and API errors in `exception`. New modules must retain this feature-first structure.

## Implemented feature slices

- Authentication: register, login, logout and session endpoints under `/api/auth`.
- Customer profiles: customers can view/update their own profile; the carpenter can list and view customers under `/api/customers`.
- Service requests: customers can create, view and edit early-stage requests; the carpenter can list all requests and update workflow status under `/api/service-requests`.
- Appointments: the carpenter can schedule, reschedule and progress appointments linked to customers, service requests or projects, while customers can view their own appointments under `/api/appointments`.
- Notifications: customers and the carpenter can view in-app notifications, unread counts and mark notifications as read under `/api/notifications`.
- Documents: the carpenter can manage document references linked to customers, requests, projects or invoices, while customers can view visible active documents under `/api/documents`.
- Audit logs: the carpenter can review server-recorded workflow actions and filter them by entity under `/api/audit-logs`.
- Quotations: the carpenter can create draft quotations from service requests, add priced items, submit them to customers, and customers can accept or reject under `/api/quotations`.
- Orders: accepting a pending quotation creates one order idempotently; the carpenter can manage order status and customers can view their own orders under `/api/orders`.
- Projects: accepted orders create projects automatically; the carpenter can manage project status, progress, dates and timeline updates, while customers can view their own project timeline under `/api/projects`.
- Invoices and payments: the carpenter can create and issue invoices for orders, customers can submit payment proof references, and the carpenter can approve or reject payments under `/api/invoices` and `/api/payments`.
- Suppliers and inventory: the carpenter can manage suppliers/materials, record stock transactions, allocate/return stock to projects, and track low-stock materials under `/api/suppliers`, `/api/materials` and `/api/stock-transactions`.
- Expenses: the carpenter can capture, edit, approve, reimburse and void business expenses linked to suppliers, projects or materials under `/api/expenses`.
- Reporting: the carpenter can view order, project, invoice, payment, expense and inventory rollups under `/api/reports/overview`.
