# NTP1 – NOVA Training Program Transfer v1

NTP1 is the versioned desktop-to-mobile snapshot format for NOVA training programs.

## Transport

Desktop starts a temporary HTTP server on the local LAN and exposes a one-time URL:

`GET /nova/training/send/<token>`

The QR code contains only this URL. The token expires after five minutes. Responses use `Cache-Control: no-store`.

After the phone has downloaded and persisted the snapshot it confirms:

`POST /nova/training/send/<token>/confirm`

Body:

```json
{ "exportId": "<NTP1 export id>" }
```

Desktop reports success only after this confirmation.

## Snapshot

The payload contains:

- `schema: "NTP1"`
- `version: 1`
- a unique `exportId`
- source app/version metadata
- program identity, template identity, progression and current position
- the complete generated plan (`weeks -> days -> exercises`)
- for each exercise: stable exercise/artwork IDs, names, sets, reps, planned load and progression metadata
- an exercise manifest for the future mobile app

Exercise artwork is not transferred with every program. The future mobile app should bundle the same offline artwork library and resolve `artworkId` locally.

## Authority model

Desktop is the plan authority. Mobile receives a complete generated snapshot and must not regenerate RYP, 5/3/1, custom progression or other program logic independently.

A later return format for completed workouts will be specified separately so planned and actual training remain distinct datasets.
