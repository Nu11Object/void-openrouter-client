# Releasing

Releases are built by the [Release workflow](.github/workflows/release.yml) whenever a `v*` tag is
pushed. It builds every platform, attaches the packages and checksums to a **draft** GitHub Release
and leaves the publishing to you.

## Cut a release

1. On an up-to-date `main`, update `app.version` in [`gradle.properties`](gradle.properties). It
   must be `MAJOR.MINOR.PATCH` with digits only. Android derives `versionCode` as
   `major * 10_000 + minor * 100 + patch`, so keep `minor` and `patch` below 100.
2. Commit and push the change.
3. Tag the commit and push the tag:

   ```bash
   git tag -a v1.0.0 -m "Void 1.0.0"
   git push origin v1.0.0
   ```

   The tag must be `v` plus `app.version`, otherwise the workflow stops at its first job. A suffix
   (`v1.0.0-rc1`) is allowed and marks the release as a pre-release; the packages still use the
   plain `app.version`.
4. Wait for the workflow to finish (about 25 minutes, see the Actions tab). The draft appears under
   Releases with these files:
    - `Void-<version>-android.apk`
    - `Void-<version>-windows-x64.msi`
    - `Void-<version>-macos-arm64.dmg`
    - `Void-<version>-linux-x64.deb` and `.rpm`
    - `Void-<version>-ios-unsigned.ipa`
    - `SHA256SUMS.txt`
5. Edit the release notes, check the files (`sha256sum -c SHA256SUMS.txt`) and publish.

Releases are created as drafts on purpose. To publish automatically, set `RELEASE_DRAFT` to
`"false"` in the workflow.

### Fixing a bad release

Before publishing, delete the draft and the tag, fix the problem and tag again:

```bash
gh release delete v1.0.0 --cleanup-tag --yes
git tag -d v1.0.0
```

After publishing, do not reuse the version; release the next patch instead.

## Test builds without a release

Run **Actions → Release → Run workflow**. The packages are kept as run artifacts for 3 days; no tag
or release is created.

## Android signing

The Android release job reads these repository secrets:

| Secret                      | Purpose                         |
|-----------------------------|---------------------------------|
| `ANDROID_KEYSTORE_BASE64`   | Base64-encoded release keystore |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password               |
| `ANDROID_KEY_ALIAS`         | Key alias                       |
| `ANDROID_KEY_PASSWORD`      | Key password                    |

Create a keystore:

```bash
keytool -genkeypair -v -keystore release.jks -alias voidapp \
  -keyalg RSA -keysize 2048 -validity 10000
```

Encode it for GitHub:

```bash
base64 -w0 release.jks   # Linux
base64 -i release.jks    # macOS
```

Paste the output into `ANDROID_KEYSTORE_BASE64`. Keep the keystore and its passwords backed up
outside the repository; losing them means future releases cannot update the installed app.

To sign a release build locally, export the same values as environment variables
(`ANDROID_KEYSTORE_PATH` is the path to the keystore file) and run the release task:

```bash
export ANDROID_KEYSTORE_PATH=/path/to/release.jks
export ANDROID_KEYSTORE_PASSWORD=...
export ANDROID_KEY_ALIAS=voidapp
export ANDROID_KEY_PASSWORD=...
./gradlew :androidApp:assembleRelease
```

Without them the release APK is unsigned.

## Unsigned builds

macOS and Windows desktop builds are unsigned and the IPA is unsigned, so Gatekeeper and SmartScreen
will warn on first launch. The iOS IPA must be sideloaded with a tool that accepts unsigned apps.
Signing and notarizing them would need paid developer accounts and extra CI secrets, and is not set
up.
