# NOVA Mobile – Release Notes

## v0.1.0 – NTP1 Offline Program Foundation

Første Android-baseline.

- implementert låst NOVA dark mobile designretning
- implementert QR-skanning
- implementert NTP1 GET/confirm mot NOVA Desktop v0.3.56
- confirm sendes først etter vellykket lokal lagring
- implementert lokal SQLite-programlagring
- implementert Program → Uke → Dag → Øvelser
- implementert øvelsesdetalj med offline 1→2→3→2→1 SVG-animasjon
- bundlet desktopens komplette øvelseskatalog og offline artwork
- lagt inn LAN URL-validering, payload-limit og NTP1-validering
- klargjort struktur for senere workout logging/retur uten å implementere dette i v0.1.0

### Build fix 2
- Rettet feil Compose-import av `PaddingValues` i fem UI-skjermer.
- `PaddingValues` importeres nå fra `androidx.compose.foundation.layout`, som er korrekt Compose-pakke.
