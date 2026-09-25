# NOVA_BARCODE_1

Desktop creates a short-lived LAN session:

`http://<private-ip>:<port>/nova/intake/barcode/<token>`

Mobile scans this pairing QR, verifies the endpoint with GET, then scans an EAN-8 / UPC-A / UPC-E / EAN-13 product barcode and sends:

```json
{"type":"barcode","ean":"7038010054971"}
```

The desktop resolves the EAN through Kassalapp. The API token never leaves the desktop. Missing nutrition fields may be completed from a high-confidence Matvaretabellen match; uncertain matches require user confirmation on desktop.
