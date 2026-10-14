# API test report template

Run these against the deployed backend and record actual HTTP status, response and date. Do not mark a test passed based on expected behavior alone.

| Test | Setup/request | Expected | Actual | Pass? |
|---|---|---|---|---|
| Health | `GET /actuator/health` without auth | 200, status UP | Pending execution | Pending |
| Unauthenticated API | `GET /api/categories` without auth | 401 | Pending execution | Pending |
| Valid login | `GET /api/me` as supplier | 200, STAKEHOLDER, Demo Supplier | Pending execution | Pending |
| Valid calculation | POST activity with quantity 100, factor 1.2, allocation 1/1 | 120 kg CO2e | Pending execution | Pending |
| Invalid quantity | POST quantity 0 or negative | 400 | Pending execution | Pending |
| Invalid allocation | numerator greater than denominator | 400 | Pending execution | Pending |
| Unit mismatch | Select factor and submit another unit | 400 | Pending execution | Pending |
| Duplicate physical event | POST same eventKey twice | Second response 409 | Pending execution | Pending |
| Cross-organization write | supplier submits organizationName `Demo Retailer` | 403 | Pending execution | Pending |
| Reviewer write | reviewer POSTs activity | 403 | Pending execution | Pending |
| Scope/category mismatch | `scope=SCOPE_1`, `categoryCode=CAT_1` | 400 | Pending execution | Pending |
| Batch summary | GET summary after create | Contains saved row and computed total | Pending execution | Pending |

The arithmetic case uses a demo factor only for reproducible API testing; it is not evidence of a real-world emission factor. Also test deployment persistence by creating an activity, restarting the backend, and confirming the record remains in PostgreSQL.
