# Assignment coverage and honest limitations

## Implemented in this prototype
- PostgreSQL persistence via Spring Data JPA; Docker-based Render deployment and Vercel SPA fallback.
- React/Vite/Tailwind interface with activity entry, scope/category selection, factor selection and batch summary.
- Scope 1, Scope 2 and all 15 Scope 3 category choices in the backend category model.
- Backend decimal calculation: activity quantity × selected factor × allocation fraction; non-positive quantities and allocation > 100% are rejected.
- BCrypt password hashing, Spring Security Basic authentication, ADMIN / STAKEHOLDER / REVIEWER roles, and stakeholder organization checks on activity creation and listing.
- Idempotent demo-user seeding (missing accounts are added even when the users table is not empty).
- Optional unique physical event key to reject repeated records for the same event; calculation factor value is snapshotted on each activity.
- Audit entries for activity creation, current-user endpoint, category availability status endpoint and audit read endpoint.
- Swagger/OpenAPI, Postman collection, PostgreSQL category reference SQL and deployment instructions.

## Known gaps — do not represent as complete
- Demo factors are illustrative values, not verified Climatiq factors. Replace them with a legally usable, source-cited factor subset before a formal evaluation. This bundle does not claim to have verified the latest Climatiq release.
- Full factor provenance fields (dataset version, geography, factor year, license, gas basis, lifecycle boundary and access date) are not yet modeled individually.
- The event key prevents duplicate event entry but does not implement a full transfer graph, shared-event multi-allocation ledger or global allocation cap across multiple organizations.
- No dedicated organizations/facilities/products/batches/transfers relational model yet; stage, organization and batch are currently activity fields. Product journey is not a full linked transfer graph.
- No automatic unit conversion, tonne-kilometre conversion engine, gas-specific GWP conversion or Scope 2 location-vs-market alternatives. Units must exactly match the selected factor.
- Category status endpoint transparently marks categories as DATA_UNAVAILABLE pending suitable evidence; applicability reasons are generic rather than individually configured.
- Summary views are activity sums; they do not yet reconcile organization inventory attribution separately from a unique batch event ledger or provide cradle-to-retail/full-life-cycle totals and per-unit intensity.
- No edit/delete activity API, pagination, formal reporting-period boundary, immutable recalculation revisions, comprehensive security/aggregation integration tests or exported test execution report.
- Reviewer has read access and cannot create activities. Administrator can inspect all activities. Stakeholder linked-traceability permissions remain limited to own-organization records.
- Seeded users share a common demo stakeholder password unless configured through environment variables before first creation. Set unique secrets for public deployment and rotate them before submission.

## Required before claiming acceptance
1. Replace illustrative factors with appropriate accessible Climatiq factors and record all provenance metadata.
2. Implement organization/facility/product/batch/transfer entities and a unique physical-event ledger with allocation reconciliation.
3. Add complete unit conversions, Scope 2 alternative methods, gas conversion where applicable and missing-factor handling.
4. Add test evidence for arithmetic, freight, allocation, double counting, missing factors, negative inputs, cross-org denial and role escalation.
5. Deploy to Render + Vercel + PostgreSQL and test using the actual production URLs and environment variables.
