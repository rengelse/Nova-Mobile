# NOVA Mobile v0.2.0

Android companion for NOVA trening og strekkodeskanning av faktisk matinntak.

## Omfang

- NTP1 Desktop → Android via lokal QR/LAN-session
- QR-scanner med CameraX + ZXing
- NTP1-validering før lagring
- SQLite-lagring av komplette, immutable programsnapshots
- confirm til desktop først etter vellykket lokal DB-commit
- Program → Uke → Dag → Øvelser
- øvelsesdetalj med offline 3-frame SVG-animasjon
- 304 øvelser / 302 artwork-sett / 906 SVG-framefiler bundlet lokalt
- flere lagrede programmer kan beholdes og aktiveres
- ingen backend, login eller cloud
- NOVA_BARCODE_1 pairing mot desktop for EAN-8 / UPC / EAN-13
- produktstrekkoden sendes kun til desktop; Kassalapp-tokenet ligger aldri på mobilen

## NTP1 authority

NOVA Desktop er sannhetskilden. Mobilen regenererer ikke 5/3/1, RYP, GVT eller annen programlogikk.

## Bygg

Prosjektet er konfigurert for Android Studio / AGP 9.4.0, Gradle 9.6, JDK 17, compileSdk 37 og targetSdk 36.

`gradle-wrapper.properties` og launcher-skript følger med. Dersom `gradle-wrapper.jar` ikke finnes i prosjektet, åpne prosjektet i Android Studio eller kjør lokalt:

`gradle wrapper --gradle-version 9.6.0`

Deretter:

`./gradlew assembleDebug`

## Runtime

PC og telefon må være på samme Wi-Fi/LAN. QR-adressen må være en privat IPv4-adresse og følge:

`http://<LAN-IP>:<port>/nova/training/send/<token>`

Mobilen gjør GET → validering → lokal SQLite-commit → POST confirm.

## NOVA_BARCODE_1

Når desktop viser QR for faktisk matinntak, skanner mobilen pairing-QR-en i samme Overfør-skjerm. Appen oppdager `/nova/intake/barcode/<token>`, verifiserer lokal LAN-session og går automatisk over til produktskanning. Produktets EAN sendes til desktop som JSON; desktop gjør Kassalapp-/Matvaretabellen-oppslaget.
