# Creator Co-Op: Deployment, CI/CD & Operations Guide

---

## 1. Building Production Release Bundles

### 1.1 Command Execution
To generate the production-ready Android App Bundle (`.aab`):
```bash
gradle :app:bundleRelease
```

### 1.2 Output Verification
- **File Location**: `app/build/outputs/bundle/release/app-release.aab`
- **Typical Size**: ~15 MB
- **Configuration**: R8 full-mode code minification and resource shrinking enabled.

---

## 2. ProGuard & R8 Optimization Rules (`app/proguard-rules.pro`)

```proguard
# Jetpack Compose
-keep class androidx.compose.material.icons.** { *; }

# Room Database
-keep class * extends androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.paging.**

# Kotlinx Serialization
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Android Architecture & ViewModels
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
```

---

## 3. App Links Verification Setup (`assetlinks.json`)

Deploy the following payload to `https://creator-studio.app/.well-known/assetlinks.json`:

```json
[
  {
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
      "namespace": "android_app",
      "package_name": "com.aistudio.creatorcoop.app",
      "sha256_cert_fingerprints": [
        "YOUR_GOOGLE_PLAY_APP_SIGNING_SHA256_FINGERPRINT"
      ]
    }
  }
]
```

---

## 4. Google Play Console Data Safety Reference

| Data Category | Collection Type | Ephemeral | Purpose |
| :--- | :--- | :--- | :--- |
| **Name & Email** | Collected (Local + Optional Cloud Sync) | No | Account Identification, Profile Management |
| **User Identifiers** | Collected | No | Syndicate Role Assignment & Team Workspaces |
| **Photos & Avatars** | Collected (Optional) | No | Portfolio Showcase & Profile Customization |
| **Team Messages** | Collected (Optional) | No | Collaborative Workspace Communications |
| **App Diagnostics** | Local / Optional | Yes | Offline Crash Logging & Telemetry Health |
| **Device Push Tokens** | Collected (Optional) | No | High-Priority Agreement & Pitch Notifications |

- **Encryption in Transit**: Supported (TLS 1.3 / HTTPS on all endpoints).
- **Data Deletion Mechanism**: Fully supported via in-app GDPR Cascading Deletion.
