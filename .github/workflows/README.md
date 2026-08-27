# ROMITUBE GitHub Actions

- `android-build.yml`: builds a debug APK on pushes, pull requests, and manual runs.
- `android-release.yml`: builds an installable release APK on manual runs or `v*` tags.

Release signing:
- If `KEYSTORE_BASE64`, `STORE_PASSWORD`, `KEY_PASSWORD`, and optionally `KEY_ALIAS` are configured, the workflow uses the repository keystore.
- Otherwise it creates a temporary CI keystore so the release APK is still signed and installable. Do not use the temporary key for production updates.

The project intentionally uses the Gradle distribution provisioned by `gradle/actions/setup-gradle@v6`, so a local `gradlew` wrapper is not required for these CI workflows.
