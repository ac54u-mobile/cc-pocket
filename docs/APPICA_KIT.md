# CCPocketAppicaKit

`CCPocketAppicaKit` is CC Pocket's Compose Multiplatform design layer, visually based on Appica UI 1.0.0. It is a native Kotlin implementation: the app does not embed React or a WebView.

## Source specification

The values in `AppicaKit.kt` are taken from the CSS variables served by `https://appica.dev/ui`:

- base radius: `0.875rem` → `14.dp`
- border width: `1.dp`
- default transition: `150ms`
- light background / foreground: `#FFFFFF` / `#4A5565`
- dark background / foreground: `#030712` / `#D1D5DC`
- light border / primary: `#E5E7EB` / `#101828`
- dark border / primary: `#1E2939` / `#FFFFFF`

CC Pocket's terracotta, Codex teal, and OpenCode purple remain product identity colors and may be supplied explicitly to Appica components.

## Architecture

- `AppicaPalette` / `AppicaTok`: light and dark semantic colors.
- `AppicaMetrics`: radius, size, border, and motion scales.
- `AppicaKit.kt`: surfaces, buttons, switches, inline controls, and overlay primitives.
- `AppicaComponents.kt`: badges, chips, checkbox, radio, progress, alerts, and tabs.
- `SettingsKit.kt`: settings-specific compositions backed by Appica primitives.

New settings UI must use these components rather than declaring page-local card, border, switch, or selection styles.

## Migrated mobile surfaces

- Settings hub: compact navigation title, search, grouped cards, icon tiles, and empty search state.
- Settings details: compact context banner, Appica selection lists, segmented controls, switches, hints, and danger actions.
- Usage, scheduled tasks, folder sharing, join flow, and bridges: shared navigation and background hierarchy.
- Projects and sessions: compact headers, Appica search, bordered project cards, and consistent list surfaces.
- Chat: neutral page chrome, compact session header, right-aligned user cards, bordered tool cards, and Appica composer controls.
- Session overlays: searchable model picker, session switcher, quick actions, background jobs, and permission sheets.
- Work surfaces: file viewer and attachments, diff, terminal, workflows, schedules, usage, bridges, shared folders, and Fleet inbox.
- Entry surfaces: onboarding, pairing, directory selection, device management, and app-lock chrome.

Large display titles are intentionally not part of the mobile language. Hierarchy comes from spacing, type weight, borders, state colors, and grouping rather than oversized text.

## Validation

`AppicaKitTest` locks the upstream token values and contrast polarity. `ShowcaseRender` includes deterministic settings, chat, model-picker, session, permission, usage, diff, sharing, and Fleet frames for visual review. Desktop compilation exercises the shared Compose implementation. iOS is validated by the TrollStore Release archive workflow before delivery.
