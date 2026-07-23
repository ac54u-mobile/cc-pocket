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

## Validation

`AppicaKitTest` locks the upstream token values and contrast polarity. Settings and desktop compilation exercise every migrated component. iOS is validated by the TrollStore Release archive workflow before delivery.
