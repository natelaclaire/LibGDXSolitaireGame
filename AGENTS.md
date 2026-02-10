# Repository Guidelines

## Project Structure & Module Organization
- `core/src/main/java/com/natelaclaire/solitaire`: shared game logic (libGDX `ApplicationAdapter`).
- `lwjgl3/src/main/java/...`: desktop launcher (LWJGL3) and startup helpers.
- `android/src/main/java/...`: Android launcher, manifests, and resources under `android/res`.
- `html/src/main/java/...`: GWT launcher and module definitions; web assets in `html/webapp`.
- `assets/`: game art and runtime assets bundled across platforms.
- `build/`, `*/build/`: Gradle build outputs.

## Build, Test, and Development Commands
Run via the Gradle wrapper:
- `./gradlew lwjgl3:run` — run the desktop app.
- `./gradlew lwjgl3:jar` — build a runnable desktop jar at `lwjgl3/build/libs`.
- `./gradlew build` — build all modules.
- `./gradlew html:dist` — compile GWT output to `html/build/dist`.
- `./gradlew html:superDev` — run GWT SuperDev at `http://localhost:8080/html`.
- `./gradlew test` — run unit tests (none are currently defined).

## Coding Style & Naming Conventions
- Java 4-space indentation, standard libGDX/Java formatting (see `core/src/main/java/.../SolitaireGame.java`).
- Package naming: `com.natelaclaire.solitaire`.
- Class names in `UpperCamelCase`; methods/fields in `lowerCamelCase`.
- Assets referenced by filename (e.g., `libgdx.png`) should live in `assets/`.

## Testing Guidelines
- No testing framework is configured yet. If adding tests, follow Gradle conventions:
  - Put tests in `core/src/test/java/...`.
  - Name tests `*Test.java` or `*Tests.java`.
  - Run with `./gradlew test`.

## Commit & Pull Request Guidelines
- Git history currently contains only `Initial commit`; no established commit-message convention. Prefer concise, present-tense summaries (e.g., “Add card layout system”).
- PRs should include:
  - A clear summary of behavior changes.
  - Steps to run locally (command + platform).
  - Screenshots or short clips for visual changes.

## Configuration & Platform Notes
- Android builds require the Android SDK; ensure `local.properties` points to your SDK path.
- HTML builds require GWT; use `html:superDev` for faster iteration.
