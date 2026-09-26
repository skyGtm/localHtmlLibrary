# Local HTML Library

Minimal offline Android HTML library/reader.

## V1 architecture

- HTML files remain in a user-selected folder.
- App-owned metadata is stored as `library.json` in a separately selected user folder.
- No INTERNET permission.
- Recursive `.html` / `.htm` discovery.
- Two-column square grid.
- Pin/unpin.
- Local WebView with JavaScript, DOM storage and relative local assets.
- Reading position stored in external app-data folder.
- ZIP export contains metadata and the complete HTML library tree.
- GitHub Actions builds a debug APK.

### Android limitation

A Storage Access Framework tree is a URI, not a normal filesystem path. Android WebView's internal profile cannot be relocated to that URI simply by choosing a folder. Therefore the app keeps its own portable state externally, while WebView's implementation-managed cache/profile remains private to the app. The backup is designed around data the app controls rather than copying opaque WebView internals.

## GitHub Actions

`.github/workflows/build-debug.yml` generates the Gradle wrapper and runs `assembleDebug`. The APK is uploaded as the `local-html-library-debug` artifact.
