# AGENTS.md

## Project

Modern Android app built with:

- Kotlin
- Jetpack Compose
- MVVM
- Hilt
- Room
- Coroutines + Flow

---

## Principles

- **Read-only**: Do not modify or create any files unless specifically instructed to do so.
- Keep changes minimal.
- Prefer existing patterns over introducing new ones.
- Fix root causes instead of adding workarounds.
- Avoid unnecessary dependencies. Use latest dependencies.

---

## Architecture

UI
→ ViewModel
→ Repository
→ Data Sources (Room / Network)

ViewModels contain UI state.

Repositories coordinate data.

Composable functions remain as stateless as practical.

---

## Kotlin

- Prefer immutable data.
- Prefer `val`.
- Keep functions small.
- Use expression bodies when clearer.
- Avoid `!!`.
- Avoid global mutable state.

---

## Coroutines

- Never block the main thread.
- Use structured concurrency.
- Expose `Flow` from repositories.
- Convert to `StateFlow` inside ViewModels.

---

## Compose

- Hoist state.
- Keep composables focused.
- Remember expensive objects.
- Use Material 3 components.
- Prefer previews for new screens.

---

## Dependency Injection

Use Hilt.

Inject dependencies through constructors whenever possible.

---

## Testing

When modifying logic:

- Update existing tests.
- Add tests for new behavior when practical.

---

## Before Finishing

Verify:

- Project builds.
- No unused imports.
- No dead code.
- Formatting is clean.