# ll-todo Development Guide

## 1. How to Share Code

### Option A: Share via GitHub (recommended)

```powershell
git status
git checkout -b feat/your-change
git add .
git commit -m "feat: your change"
git push -u origin feat/your-change
```

Then share the repository URL or Pull Request URL.

### Option B: Share as zip source package

```powershell
# Run from parent folder of Compose-ToDo
Compress-Archive -Path .\Compose-ToDo\* -DestinationPath .\Compose-ToDo-source.zip -Force
```

Before zipping, remove large generated folders if possible:
- `.gradle`
- `build`
- `tmp`

### Option C: Share patch only

```powershell
git format-patch -1
```

Share the generated `.patch` file. Receiver can apply it with:

```powershell
git am your.patch
```

## 2. Local Environment

- JDK: 17
- Android SDK:
  - `platforms;android-35`
  - `build-tools;35.0.0`
  - `platform-tools`
- Use project Gradle Wrapper (`gradlew` / `gradlew.bat`)

Windows terminal setup:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

## 3. Build Commands

```powershell
git clone https://github.com/wisnukurniawan/Compose-ToDo.git
cd Compose-ToDo
.\gradlew.bat :app:assembleDebug --no-configuration-cache
```

Outputs:
- Debug APK: `app\build\outputs\apk\debug\app-debug.apk`
- Release APK: `app\build\outputs\apk\release\app-release.apk`

## 4. Install to Device

```powershell
adb devices
adb -s <deviceId> install -r -t -g app\build\outputs\apk\debug\app-debug.apk
adb -s <deviceId> shell monkey -p com.lltodo.app.debug -c android.intent.category.LAUNCHER 1
```

Install release:

```powershell
adb -s <deviceId> install -r -g app\build\outputs\apk\release\app-release.apk
```

## 5. Common Issues

### `configuration-cache.lock` in use

```powershell
.\gradlew.bat --stop
```

Then build again.

### Build cache corruption suspected

```powershell
.\gradlew.bat clean :app:assembleDebug --no-build-cache --rerun-tasks --no-configuration-cache
```

### Device install fails
- Confirm USB debugging and authorization
- Restart adb:

```powershell
adb kill-server
adb start-server
```

- If signature conflicts, uninstall old app first

## 6. Distribution Advice

For sharing to other Android phones:
1. Build `:app:assembleRelease`
2. Share `app-release.apk`
3. Attach version info + change summary

If install fails on receiver device, most common reason is signature mismatch with an already installed package.
