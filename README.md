CamYan — repository

This repo currently contains:
- app/ (module with HookEntry.java)
- smali/ (smali sources)

Generated basic Gradle files (settings.gradle, build.gradle, app/build.gradle).
To build locally you need a working Android SDK + Gradle.

If you want to build on CI, set up GitHub Actions to install SDK and run ./gradlew assemble (or use actions/setup-java + gradle).
