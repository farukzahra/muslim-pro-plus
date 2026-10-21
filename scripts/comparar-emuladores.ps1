# Comparacao lado a lado: Muslim Pro (Pixel6) x Muslim Plus (Pixel6Plus).
#
# Uso:
#   powershell -NoProfile -ExecutionPolicy Bypass -File scripts\comparar-emuladores.ps1
#
# O script:
#   1. cria o AVD Pixel6Plus (API 34) se ainda nao existir;
#   2. sobe os dois emuladores (5554 = Pro, 5556 = Plus);
#   3. instala o APK debug atual no 5556 (por cima, mantendo dados);
#   4. abre os dois apps para comparar.

$ErrorActionPreference = "Stop"
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$adb = "$env:ANDROID_HOME\platform-tools\adb.exe"
$emulator = "$env:ANDROID_HOME\emulator\emulator.exe"
$avdmanager = "$env:ANDROID_HOME\cmdline-tools\latest\bin\avdmanager.bat"
$root = Split-Path -Parent $PSScriptRoot
$apk = Join-Path $root "app\build\outputs\apk\debug\app-debug.apk"
$image = "system-images;android-34;google_apis_playstore;x86_64"

if (-not (Test-Path "$env:USERPROFILE\.android\avd\Pixel6Plus.ini")) {
    "no" | & $avdmanager create avd -n Pixel6Plus -k $image -d pixel_6 --force | Out-Host
}

function Start-Avd($name, $port) {
    $serial = "emulator-$port"
    $state = (& $adb -s $serial get-state 2>$null)
    if ($state -ne "device") {
        Start-Process -FilePath $emulator -ArgumentList "-avd", $name, "-port", "$port", "-no-snapshot", "-no-boot-anim", "-no-audio" -WindowStyle Minimized
        for ($i = 0; $i -lt 70; $i++) {
            Start-Sleep 3
            if ((& $adb -s $serial shell getprop sys.boot_completed 2>$null) -match "1") { break }
        }
    }
    return $serial
}

$pro = Start-Avd "Pixel6" 5554
$plus = Start-Avd "Pixel6Plus" 5556

Set-Location $root
& .\gradlew.bat assembleDebug --no-daemon | Out-Host
& $adb -s $plus install -r $apk | Out-Host

& $adb -s $pro shell am start -n com.bitsmedia.android.muslimpro/.core.screens.main.MainActivity | Out-Null
& $adb -s $plus shell am start -n com.farukzahra.muslimplus/.MainActivity | Out-Null

Write-Host ""
Write-Host "Pronto:" -ForegroundColor Green
Write-Host "  $pro  Muslim Pro"
Write-Host "  $plus Muslim Plus"
