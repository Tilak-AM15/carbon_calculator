# Evergreen Carbon Intelligence — PostgreSQL edition

Full-stack prototype for the Java Full Stack Developer Intern carbon-calculator assignment. **Database: PostgreSQL** (Supabase or another PostgreSQL provider), backend: Java 17 + Spring Boot 3.4, Spring Security/JPA, frontend: React + Vite + Tailwind, deployment: Render backend + Vercel frontend.

> **Prototype disclosure:** This ZIP improves the supplied project but is not a certified carbon inventory and does not satisfy every assignment acceptance criterion. Read `docs/assignment-gap-checklist.md`. In particular, factors shipped here are explicitly illustrative—not verified Climatiq factors—and a complete transfer graph, comprehensive unit conversions and full event-ledger allocation are not implemented. Do not present demo factors as real or the footprint as complete.

## Included
- Responsive carbon dashboard and activity entry flow.
- Backend-calculated decimal emissions, scope and all 15 Scope 3 category options.
- PostgreSQL persistence, factor value snapshots, allocation ratio validation and optional unique physical event keys to reject duplicate event records.
- BCrypt-hashed demo accounts, ADMIN / STAKEHOLDER / REVIEWER roles, stakeholder organization checks, audit entries, and a `/api/me` endpoint used by the frontend to set the signed-in user's organization.
- Category status endpoint that explicitly flags unavailable verified evidence rather than silently treating missing factors as zero.
- Swagger/OpenAPI, Postman collection, PostgreSQL category SQL, deployment config, and application notes.

## Demo accounts
Use these only for a non-production demo. Configure environment variables before first creation for unique passwords. If the default demo users already exist in a database, changing environment variables does **not** change their existing password hashes.

| Role | Username | Default password |
|---|---|---|
| Administrator | `admin` | `AdminDemo123!` |
| Supplier stakeholder | `supplier` | `SupplierDemo123!` |
| Manufacturer stakeholder | `manufacturer` | `SupplierDemo123!` |
| Logistics stakeholder | `logistics` | `SupplierDemo123!` |
| Distributor stakeholder | `distributor` | `SupplierDemo123!` |
| Wholesaler stakeholder | `wholesaler` | `SupplierDemo123!` |
| Retailer stakeholder | `retailer` | `SupplierDemo123!` |
| Waste-handler stakeholder | `wastehandler` | `SupplierDemo123!` |
| Reviewer/auditor | `reviewer` | `ReviewerDemo123!` |

The application now adds missing seeded users individually, even if other user records already exist. If a username already exists, its password and role are not overwritten. Rotate defaults for any publicly accessible deployment.

## Run locally
Prerequisites: Java 17+, Maven 3.9+, Node.js 20+, PostgreSQL.

### Backend
From `carbon-calculator-backend`:

```bash
export DB_URL='jdbc:postgresql://localhost:5432/carbon_calculator'
export DB_USERNAME='postgres'
export DB_PASSWORD='your_password'
export CORS_ALLOWED_ORIGINS='http://localhost:5173'
mvn spring-boot:run
```

Swagger UI: `http://localhost:8080/swagger-ui/index.html` (or `/swagger-ui.html`). OpenAPI JSON: `http://localhost:8080/v3/api-docs`. Health: `http://localhost:8080/actuator/health`.

### Frontend
From `carbon-calculator-frontend`:

```bash
npm install
cp .env.example .env
# Set VITE_API_BASE_URL=http://localhost:8080 in .env
npm run dev
```

## Deploy PostgreSQL on Supabase
1. Create a PostgreSQL project and open **Connect** in Supabase.
2. Copy the actual host, port, database, username and password from Supabase. Use the direct connection if reachable from Render; otherwise use the exact Session Pooler credentials shown by Supabase.
3. Set backend `DB_URL` to `jdbc:postgresql://HOST:PORT/DATABASE?sslmode=require`, plus `DB_USERNAME` and `DB_PASSWORD`. Do not put database secrets in frontend variables.
4. Core JPA tables are created/updated by `spring.jpa.hibernate.ddl-auto=update` for this prototype. `database/supabase-schema.sql` (or the PostgreSQL-compatible `database/schema-and-seed.sql`) can be run to seed the reference Scope 3 category table. Back up any important database before deploying schema changes.

## Deploy backend on Render
- Create a Web Service from the `carbon-calculator-backend` repository root, using the included Dockerfile.
- Configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `CORS_ALLOWED_ORIGINS`, and strong unique initial demo passwords (`DEMO_ADMIN_PASSWORD`, `DEMO_STAKEHOLDER_PASSWORD`, `DEMO_REVIEWER_PASSWORD`).
- `CORS_ALLOWED_ORIGINS` must be the exact frontend origin, for example `https://your-project.vercel.app` (no trailing path). Comma-separated origins are supported.
- Check `/actuator/health`, `/swagger-ui/index.html` and `/v3/api-docs` after deployment. Free instances may sleep and the first request may be delayed.

## Deploy frontend on Vercel
- Import `carbon-calculator-frontend` as a separate repository/project; framework Vite, build `npm run build`, output `dist`.
- Set `VITE_API_BASE_URL=https://YOUR_RENDER_SERVICE.onrender.com` for the Vercel deployment, then redeploy.
- Add the final Vercel HTTPS origin to Render's `CORS_ALLOWED_ORIGINS`, then redeploy the backend.

## API overview
All `/api/**` routes require HTTP Basic authentication. Swagger/OpenAPI describes the live backend API.

| Method | Endpoint | Access / purpose |
|---|---|---|
| GET | `/api/me` | Authenticated user's username, role and organization |
| GET | `/api/categories` | Scope/category catalogue |
| GET | `/api/category-status` | Explicit DATA_UNAVAILABLE status/reason for categories pending verified factors |
| GET | `/api/factors` | Factor catalogue with source notes |
| GET | `/api/activities` | Admin/reviewer all activities; stakeholder own organization only |
| POST | `/api/activities` | Admin/stakeholder create; stakeholder can only write own organization |
| GET | `/api/batches/{batchId}/summary` | Batch summary; stakeholders see own-organization rows only |
| GET | `/api/audit` | Admin/reviewer all audit events; stakeholder own organization only |
| GET | `/actuator/health` | Public health check |

Example POST body (choose a factor ID returned by `/api/factors`):

```json
{
  "organizationName": "Demo Supplier",
  "batchId": "BATCH-001",
  "stageName": "Raw material supplier",
  "activityName": "Purchased raw material",
  "scope": "SCOPE_3",
  "categoryCode": "CAT_1",
  "quantity": 100,
  "unit": "kg",
  "factorId": 2,
  "allocationNumerator": 100,
  "allocationDenominator": 100,
  "eventKey": "BATCH-001-SUPPLIER-MATERIAL-01",
  "methodNote": "Demo only; replace illustrative factor with a cited source factor."
}
```

Emissions formula: `quantity × factorSnapshot × (allocationNumerator / allocationDenominator)`. Units must exactly match the selected factor's activity unit; automatic unit conversion is not implemented. A supplied `eventKey` must be unique. Use a stable key for a physical event to guard against accidental duplicate entry. Allocation numerator cannot exceed denominator.

## API verification
Import `postman/carbon-calculator-collection.json` into Postman and set `baseUrl` to your backend URL. The collection uses a demo account; change its Basic Auth credentials to test other roles. Verify 401 unauthenticated access, 403 cross-organization writes, 400 invalid quantity/allocation, successful create and summary, duplicate event key (409), reviewer write denial and `/api/category-status`. Save actual results separately; the ZIP does not claim tests have been executed against a live deployment.

## Architecture / schema notes
The current JPA schema includes `app_users`, `emission_factors`, `emission_activities` and `audit_events`. Activity rows snapshot the factor value and calculation inputs. This is not yet the complete organization/facility/product/batch/transfer relational schema requested by the assignment. See `docs/architecture-and-working-guide.md` and `docs/assignment-gap-checklist.md`.

## Submission deliverables
- Frontend source and build/deploy instructions: `carbon-calculator-frontend/`
- Backend source, Dockerfile, environment configuration: `carbon-calculator-backend/`
- PostgreSQL reference schema scripts: `database/`
- Importable Postman collection: `postman/carbon-calculator-collection.json`
- OpenAPI notes, working guide and ER diagram: `docs/`

Keep the two code directories in separate GitHub repositories if the evaluator requires separate frontend and backend repositories. Do not commit real database passwords, API keys or production secrets.
"# carbon_calculatora" 
