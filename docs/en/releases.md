# Release Management & CI/CD Automation

The **Pillars of Fortune** (`nadamu-pillar`) repository utilizes automated Continuous Integration (CI) and Continuous Deployment/Release (CD) pipelines powered by **GitHub Actions**.

---

## 1. CI/CD Architecture

Two primary workflows are located in `.github/workflows/`:

```
                      ┌─────────────────────────────────┐
                      │          GitHub Events          │
                      └────────────────┬────────────────┘
                                       │
            ┌──────────────────────────┴──────────────────────────┐
            ▼                                                     ▼
   Push / PR to `master`                                 Tag push `v*` or
            │                                            `workflow_dispatch`
            ▼                                                     │
┌─────────────────────────┐                                       ▼
│   CI (.github/ci.yml)   │                           ┌─────────────────────────┐
├─────────────────────────┤                           │ Release (.github/       │
│ • Checkout              │                           │          release.yml)   │
│ • Java 21 (Corretto)    │                           ├─────────────────────────┤
│ • ./gradlew check test  │                           │ • Checkout (full depth) │
│ • ./gradlew build       │                           │ • Version resolution    │
└─────────────────────────┘                           │ • Java 21 (Corretto)    │
                                                      │ • ./gradlew check test  │
                                                      │ • ./gradlew build       │
                                                      │   -Pversion=${VERSION}  │
                                                      │ • GitHub Release        │
                                                      │ • Upload .jar asset     │
                                                      └─────────────────────────┘
```

---

## 2. Dynamic Gradle Versioning

Dynamic version resolution is implemented in [`build.gradle.kts`](file:///workspace/nadamu-pillar/build.gradle.kts):

```kotlin
group = "space.nadamu"
version = project.findProperty("pluginVersion")?.toString()
    ?: project.findProperty("version")?.toString()?.takeIf { it != "unspecified" }
    ?: "1.0-SNAPSHOT"
```

### Behavior:
- **Local development**: without flags, version defaults to `1.0-SNAPSHOT`.
- **Release builds**: passing `-Pversion=1.0.0` (or `-PpluginVersion=1.0.0`) dynamically sets the project version, the generated jar filename (`NadamuPillar-1.0.0.jar`), and the `version: 1.0.0` entry in `plugin.yml`.

---

## 3. Releasing a New Version

There are two methods to publish a release:

### Method 1: Git Tag (Recommended)

1. Ensure all changes are committed and pushed to `master`:
   ```bash
   git status
   git push origin master
   ```
2. Create an annotated Git tag (the `v` prefix is mandatory):
   ```bash
   git tag -a v1.0.1 -m "Release v1.0.1: summary of changes"
   ```
3. Push the tag to GitHub:
   ```bash
   git push origin v1.0.1
   ```
4. GitHub Actions will automatically:
   - Extract the tag `v1.0.1` and compute version `1.0.1`.
   - Run verification checks (`check`, `test`).
   - Build the release jar: `NadamuPillar-1.0.1.jar`.
   - Auto-generate Release Notes from commits and PRs.
   - Publish the release on [GitHub Releases](https://github.com/DanyaNADAMU/nadamu-pillar/releases) with the `.jar` asset attached.

### Method 2: Manual Trigger via GitHub Actions UI

1. Open the repository's **Actions** tab: `https://github.com/DanyaNADAMU/nadamu-pillar/actions`.
2. Select the **Release** workflow from the left sidebar.
3. Click the **Run workflow** dropdown.
4. Enter the tag name in `Tag name to release` (e.g., `v1.0.1`).
5. Click **Run workflow**.

---

## 4. Workflow Specifications

### CI (`.github/workflows/ci.yml`)
- **Triggers**: push to `master` and pull requests targeting `master`.
- **Runner**: `ubuntu-latest`, Amazon Corretto Java 21, `gradle/actions/setup-gradle@v4` with dependency caching.
- **Commands**:
  ```bash
  ./gradlew check test build
  ```

### Release (`.github/workflows/release.yml`)
- **Triggers**: `v*` tag pushes or manual `workflow_dispatch`.
- **Permissions**: `contents: write` (for release publishing and asset upload).
- **Release Action**: `softprops/action-gh-release@v2`.
- **Assets**: `build/libs/*.jar`.
- **Changelog**: automatically generated (`generate_release_notes: true`).
