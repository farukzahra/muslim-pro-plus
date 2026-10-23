# Instala o build debug atual em todos os dispositivos conectados e confirma,
# por hash SHA-256, que o APK instalado e exatamente o app-debug.apk local.
# Uso: .\scripts\install-local.ps1 [-NoLaunch]
param([switch]$NoLaunch)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = Join-Path $env:LOCALAPPDATA "Android\Sdk"
$adb = Join-Path $env:ANDROID_HOME "platform-tools\adb.exe"
$package = "com.farukzahra.muslimplus"

Push-Location $root
try {
    & (Join-Path $root "gradlew.bat") installDebug --console=plain | Out-Host
    if ($LASTEXITCODE -ne 0) { throw "installDebug falhou" }

    $apk = Join-Path $root "app\build\outputs\apk\debug\app-debug.apk"
    $expected = (Get-FileHash $apk -Algorithm SHA256).Hash

    $devices = (& $adb devices) | Select-String -Pattern "^(\S+)\s+device$" |
        ForEach-Object { $_.Matches[0].Groups[1].Value }
    if (-not $devices) { throw "Nenhum dispositivo conectado" }

    $failed = @()
    foreach ($serial in $devices) {
        $remote = ((& $adb -s $serial shell pm path $package) -join "").Trim() -replace "^package:", ""
        if (-not $remote) { $failed += "$serial (app nao instalado)"; continue }

        $actual = ((& $adb -s $serial shell "sha256sum $remote") -join "").Trim().Split(" ")[0].ToUpper()
        if (-not $actual) {
            # Fallback para emuladores sem sha256sum: puxa o APK e compara local.
            $copy = Join-Path $env:TEMP "installed-$serial.apk"
            & $adb -s $serial pull $remote $copy 2>&1 | Out-Null
            $actual = (Get-FileHash $copy -Algorithm SHA256).Hash
            Remove-Item $copy -Force
        }

        $version = ((& $adb -s $serial shell dumpsys package $package |
            Select-String -Pattern "versionName=") -join "").Trim()
        if ($actual -ne $expected) {
            $failed += "$serial (hash diferente: emulador com build antigo)"
            continue
        }

        if (-not $NoLaunch) {
            & $adb -s $serial shell am force-stop $package | Out-Null
            & $adb -s $serial shell am start -n "$package/.MainActivity" | Out-Null
            Start-Sleep -Seconds 3
            $appPid = ((& $adb -s $serial shell pidof $package) -join "").Trim()
            if (-not $appPid) { $failed += "$serial (APK novo, mas o app nao abriu)"; continue }
        }

        Write-Host "OK $serial - $version"
    }

    if ($failed.Count -gt 0) {
        $failed | ForEach-Object { Write-Host "FALHOU $_" }
        exit 1
    }
    Write-Host "Todos os dispositivos estao com o build atual."
}
finally {
    Pop-Location
}
