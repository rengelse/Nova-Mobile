# NOVA_INTAKE_1

Mobile stores barcode scans locally without product lookup:
- `scanId`
- `ean`
- `scannedAt`
- local sync status

Desktop creates a short-lived private-LAN session:
`http://<private-ip>:<port>/nova/intake/import/<token>`

Mobile POSTs all unsynced scans in one batch. Desktop validates/deduplicates and performs Kassalapp + Matvaretabellen resolution.
