# Releasing

## Cut a release

1. Update `app.version` in [`gradle.properties`](gradle.properties). It must be `MAJOR.MINOR.PATCH`
   with digits only. Android derives `versionCode` as `major * 10_000 + minor * 100 + patch`, so
   keep `minor` and `patch` below 100.
2. Commit and push the change.
3. Tag the commit and push the tag:

   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

The release workflow builds the desktop, Android and iOS artifacts and creates a draft GitHub
Release with the tag. Review it on GitHub, then publish it when it looks right.

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

## Unsigned builds

macOS and Windows desktop builds are unsigned and the IPA is unsigned, so Gatekeeper and SmartScreen
will warn on first launch. The iOS IPA must be sideloaded with a tool that accepts unsigned apps.
