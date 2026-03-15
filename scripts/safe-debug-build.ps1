param(
    [string]$DeviceId = "",
    [string]$JdkHome = "C:\Program Files\Java\jdk-17",
    [int]$BuildRetry = 3
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $ProjectRoot

if (-not (Test-Path (Join-Path $JdkHome "bin\java.exe"))) {
    throw "JDK 17 not found at: $JdkHome"
}

$env:JAVA_HOME = $JdkHome
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

function Invoke-ExternalChecked {
    param(
        [Parameter(Mandatory = $true)][string]$FilePath,
        [string[]]$Arguments = @(),
        [bool]$IgnoreExitCode = $false
    )

    & $FilePath @Arguments
    $exitCode = $LASTEXITCODE
    if (-not $IgnoreExitCode -and $exitCode -ne 0) {
        throw "Command failed with exit code ${exitCode}: $FilePath $($Arguments -join ' ')"
    }
}

Write-Host "[1/5] Safe debug build (no cache + rerun tasks)..."
$buildSucceeded = $false
$lastBuildError = $null

for ($attempt = 1; $attempt -le $BuildRetry; $attempt++) {
    try {
        if ($attempt -gt 1) {
            Write-Host "Retry build ($attempt/$BuildRetry)..."
        }

        Invoke-ExternalChecked -FilePath ".\gradlew.bat" -Arguments @("--stop") -IgnoreExitCode $true

        Invoke-ExternalChecked -FilePath ".\gradlew.bat" -Arguments @(
            "safeDebugBuild",
            "--no-build-cache",
            "--rerun-tasks",
            "--no-configuration-cache"
        )
        $buildSucceeded = $true
        break
    } catch {
        $lastBuildError = $_
        Start-Sleep -Seconds 2
    }
}

if (-not $buildSucceeded) {
    throw "Safe debug build failed after $BuildRetry attempts. Last error: $lastBuildError"
}

$apkPath = Join-Path $ProjectRoot "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $apkPath)) {
    throw "Debug APK not found: $apkPath"
}

if ([string]::IsNullOrWhiteSpace($DeviceId)) {
    $firstDevice = & adb devices | Select-String "^\S+\s+device$" | Select-Object -First 1
    if (-not $firstDevice) {
        throw "No connected authorized device found."
    }
    $DeviceId = ($firstDevice.ToString().Trim() -split "\s+")[0]
}

Write-Host "[2/5] Installing APK to device: $DeviceId"
& adb -s $DeviceId install -r -t -g $apkPath

Write-Host "[3/5] Launch smoke test..."
& adb -s $DeviceId logcat -c
& adb -s $DeviceId shell am force-stop com.lltodo.app.debug
& adb -s $DeviceId shell am start -n com.lltodo.app.debug/com.wisnu.kurniawan.composetodolist.runtime.MainActivity | Out-Null
Start-Sleep -Seconds 4

Write-Host "[4/5] Checking crash log..."
$fatal = & adb -s $DeviceId logcat -d | Select-String -Pattern "FATAL EXCEPTION|Process: com.lltodo.app.debug|NoClassDefFoundError|ClassNotFoundException"
if ($fatal) {
    $fatal | ForEach-Object { $_.Line } | Write-Output
    throw "Startup crash detected."
}

Write-Host "[5/5] Checking resumed activity..."
$resumed = & adb -s $DeviceId shell dumpsys activity activities | Select-String -Pattern "ResumedActivity: ActivityRecord.*com\.lltodo\.app\.debug"
if (-not $resumed) {
    throw "App is not resumed in foreground."
}

Write-Host "Safe debug build and startup smoke test passed."
