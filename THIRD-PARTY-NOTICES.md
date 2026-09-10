# Third-party notices

## BodyApps 3D Body Visualiser / OpnTec-bodyapps-viz
The embedded human base meshes and legacy visualiser source are derived from the `OpnTec/bodyapps-viz` project and are distributed under GNU LGPL v3 as provided by that repository. A copy of the upstream license is included at `src/renderer/assets/body3d/BODYAPPS-LICENSE-LGPL-3.0.txt`.

## Three.js r67
The isolated body viewer includes the legacy Three.js r67 runtime required to read the upstream Three.js JSON geometry format. The runtime is kept inside the isolated body viewer and is not used by the rest of the application.

## Workout Guide / Bryl Lim / Everkinetic
NOVA v0.3.36 uses the open exercise artwork distributed by `@bryllim/workout-guide` v1.0.0. The package contains 302 exercises with three transparent 512×512 SVG frames per exercise. Workout Guide code/documentation is MIT licensed; visual assets are CC BY-SA 4.0. Upstream pose artwork is credited to Everkinetic under CC BY-SA 4.0.

The complete upstream license and attribution files are copied into `src/renderer/assets/exercises/_license/` by `npm run sync:exercise-art`.

Repository: https://github.com/bryllim/workout-guide
License: https://creativecommons.org/licenses/by-sa/4.0/

NOVA does not incorporate GymMane GPL application code. GymMane is used as the functional reference and credits the same Workout Guide / Everkinetic artwork layer.

## Bundled exercise artwork (v0.3.37)
NOVA bundles local exercise artwork derived from Workout Guide / Bryl Lim and Everkinetic, via GymMane's offline vector-path representation. The exercise artwork is distributed under CC BY-SA 4.0. NOVA converts each local vector-path frame into a standalone SVG solely for in-app offline rendering. Attribution and license status are retained here and in the exercise media metadata.


## QRCode for JavaScript

Copyright (c) 2009 Kazuhiko Arase. Licensed under the MIT License. Used locally for NOVA LAN transfer QR generation.
