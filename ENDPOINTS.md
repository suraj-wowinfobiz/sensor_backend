# backend-sensor endpoint map

The monolith currently owns these base API groups:

- `/api/v1/auth`
- `/api/v1/super-admins`
- `/api/v1/admins`
- `/api/v1/users`
- `/api/v1/vendors`
- `/api/v1/vendors-engineer`
- `/api/v1/access`
- `/api/v1/org`
- `/api/v1/org/site`
- `/api/v1/org/zone`
- `/api/v1/device`
- `/api/v1/device/ingestion`
- `/api/v1/sensors`
- `/api/v1/sensor-parameter`
- `/api/v1/sensor-type`
- `/api/v1/ingestion`
- `/api/v1/processing`
- `/api/v1/analytics`
- `/api/v1/audit-logs`
- `/api/v1/dashboard`
- `/api/v1/reports`
- `/api/v1/search`
- `/api/v1/stats`
- `/api/v1/alerts`
- `/api/v1/thresholds`
- `/api/v1/batch`
- `/api/v1/calibrations`
- `/api/v1/comments`
- `/api/v1/favorites`
- `/api/v1/integrations`
- `/api/v1/locations`
- `/api/v1/maintenance`
- `/api/v1/tags`
- `/api/v1/webhooks`

Controllers that also expose multiple `/api/v1/...` subpaths from a shared base:

- `ConfigurationController`
- `FileManagementController`
- `ImportExportController`
- `RolePermissionController`
- `ScheduleJobController`
- `SystemHealthController`

Quick notes:

- `backend-sensor` compiles as a standalone module with all of these controllers present in `src/main/java`.
- The old device-management ingestion endpoints were remapped to `/api/v1/device/ingestion/**` so they can coexist with the main ingestion service endpoints.
