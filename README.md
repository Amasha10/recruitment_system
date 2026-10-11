# Recruitment System — Admin Panel (React + Spring Boot)

```
recruitment-system/
├── backend/    Spring Boot 3 REST API (Java 17, Spring Security, JPA, H2/MySQL)
└── frontend/   React 18 + Vite admin UI
```

## How to run (two terminals)

You need **JDK 17+**, **Node.js 18+** and **MySQL 8** (check with `java -version`, `node -v`, `mysql --version`).

### 0. Database (MySQL)
The backend connects to `localhost:3306` as `root` / `password` and creates the `recruitment_db` database and tables on first start.
To use other credentials, edit `backend/src/main/resources/application.properties` or set environment variables:

```powershell
$env:DB_USERNAME="root"; $env:DB_PASSWORD="your-password"
# optional: $env:DB_URL="jdbc:mysql://host:3306/recruitment_db?createDatabaseIfNotExist=true"
```

No MySQL installed? Run with the built-in H2 database instead: `mvn spring-boot:run -Dspring-boot.run.profiles=h2`
(in IntelliJ: set *Active profiles* to `h2` in the run configuration).

### 1. Backend (port 8080)
- **IntelliJ:** File → Open → `backend` folder → wait for Maven → run `RecruitmentApplication`.
- **Terminal:** `cd backend` then `mvn spring-boot:run`

Wait for `Started RecruitmentApplication`.

### 2. Frontend (port 5173)
```bash
cd frontend
npm install      # first time only
npm run dev
```
Open **http://localhost:5173** and log in with `admin@recruit.lk` / `Admin@123`.

Vite forwards every `/api/...` call to `localhost:8080`, so both must be running.

| Role | Email | Password |
|---|---|---|
| Admin | admin@recruit.lk | Admin@123 |
| Recruiter | nimal.recruiter@recruit.lk | Password@123 |
| Candidate | kavindu@gmail.com | Password@123 (shows "Access denied") |
| Disabled | ruwan@gmail.com | Password@123 (login blocked) |

Backend tests: `cd backend && mvn test`

## Architecture

```
React (5173)  ──fetch /api/* (session cookie)──►  Spring Boot (8080)
  pages/    Login, Dashboard, Users, UserForm,       controller/  REST endpoints
            UserDetail, Settings, AuditLog           service/     business rules
  context/  AuthContext (session), ToastContext      event/       Observer pattern
  api/      client.js (fetch wrapper), adminApi.js   repository/  Spring Data JPA → H2 / MySQL
```

### REST API (all `/api/admin/**` require the ADMIN role)
| Method | Endpoint | Purpose |
|---|---|---|
| POST | /api/auth/login | Log in (creates session) |
| GET | /api/auth/me | Current user |
| POST | /api/auth/logout | Log out |
| GET | /api/admin/dashboard | Counts, users by role, newest users, recent activity |
| GET | /api/admin/users?keyword=&role=&status=&page= | Search / filter / paginate |
| GET | /api/admin/users/{id} | User details |
| POST | /api/admin/users | Create user |
| PUT | /api/admin/users/{id} | Update user / change role |
| PATCH | /api/admin/users/{id}/status | Enable / disable |
| POST | /api/admin/users/{id}/reset-password | Reset password |
| DELETE | /api/admin/users/{id} | Delete user |
| GET / PUT | /api/admin/settings | Read / update system settings |
| GET | /api/admin/audit?action=&page= | Audit log |

Errors always come back as `{ status, message, fieldErrors }`, which the React forms show under each field.

## Business rules (good Q&A material)
- Role-based access is enforced on **both** sides: React shows "Access denied" to non-admins, and Spring Security returns 403 for `/api/admin/**`.
- Passwords are BCrypt-hashed and never returned by the API.
- An admin cannot disable, delete, demote or change the email of their own account.
- The last active admin can never be removed.
- Unique email, Sri Lankan phone format, password policy (min length from Settings + upper/lower/digit).
- Validation runs in React for instant feedback **and** on the server (Bean Validation) for security.

## Design pattern: Observer (5 marks)

**Problem.** Every admin action must be written to the audit trail, and some must notify the affected user.
Putting that code in every service method couples user management to auditing and notifications.

**Solution.** Services are the **Subject**: they only publish an `AdminActionEvent`.
Independent **Observers** subscribe and react:

```
AdminUserService / SystemSettingService          (Subject)
        │ publishEvent(new AdminActionEvent(...))
        ▼
Spring ApplicationEventPublisher
        ├──► AuditLogListener             → saves an AuditLog row      (Observer 1)
        └──► AccountNotificationListener  → notifies the affected user (Observer 2)
```

Files: `backend/src/main/java/lk/sliit/recruitment/event/*`, and `publish(...)` in `service/AdminUserService.java`.

**Benefits:** loose coupling; Open/Closed (a new reaction is a new listener class, no service changes);
Single Responsibility; listeners join the same transaction, so a failed action leaves no audit row.

**Live demo:** change a user's role in the UI → open Audit Log → "Role changed" and "User updated" entries appear,
and the backend console prints a `[NOTIFY]` line from the second observer.

## Troubleshooting
- **"Cannot reach the server"** in the UI → the backend isn't running on 8080.
- **Port in use** → backend: add `server.port=8081` to `application.properties` and change the proxy target in `frontend/vite.config.js`.
- **`Communications link failure`** on startup → MySQL isn't running on port 3306.
- **`Access denied for user 'root'`** → set `DB_USERNAME` / `DB_PASSWORD` (see step 0).
- **Reset data** → MySQL: `DROP DATABASE recruitment_db;` then restart. H2 profile: stop the backend and delete `backend/data/`.
