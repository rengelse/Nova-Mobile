# NOVA Mobile architecture

UI (Compose)
→ NovaRepository
→ transfer / local DB / exercise catalog

Transfer:
QR → URL validation → GET NTP1 → payload validation → SQLite transaction → POST confirm

Desktop remains plan authority. Stored NTP1 snapshots are immutable source material for the mobile view.

Future CompletedWorkout data must be stored separately from ProgramPlan snapshots.
