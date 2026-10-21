# Envia o APK debug do Muslim Plus para o celular fisico.
#
# Uso:
#   powershell -NoProfile -ExecutionPolicy Bypass -File scripts\enviar-celular.ps1
#
# Com o celular no USB (depuracao USB ligada) o script:
#   1. compila o debug;
#   2. instala por cima (mantem dados, mesma assinatura);
#   3. deixa uma copia em /sdcard/Download e em %USERPROFILE%\Downloads.

$ErrorActionPreference = "Stop"
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$adb = "$env:ANDROID_HOME\platform-tools\adb.exe"
$root = Split-Path -Parent $PSScriptRoot
$apk = Join-Path $root "app\build\outputs\apk\debug\app-debug.apk"

Set-Location $root
& .\gradlew.bat assembleDebug --no-daemon | Out-Host
if (-not (Test-Path $apk)) {
    throw "APK debug nao encontrado em $apk"
}

$devices = & $adb devices |
    Select-Object -Skip 1 |
    Where-Object { $_ -match "`tdevice$" -and $_ -notmatch "^emulator-" }

if (-not $devices) {
    Write-Host ""
    Write-Host "Nenhum celular fisico conectado." -ForegroundColor Yellow
    Write-Host "Ligue a depuracao USB e conecte o cabo, ou use:"
    Write-Host "  adb pair <ip:porta>   (Android 11+, emparelhamento sem fio)"
    Write-Host "  adb connect <ip:porta>"
    Write-Host ""
    Write-Host "O APK esta pronto em: $apk"
    exit 1
}

$serial = ($devices[0] -split "\s+")[0]
Write-Host "Instalando em $serial ..."
& $adb -s $serial install -r $apk | Out-Host
& $adb -s $serial push $apk /sdcard/Download/muslimplus-debug.apk | Out-Host
Copy-Item $apk "$env:USERPROFILE\Downloads\muslimplus-debug.apk" -Force

Write-Host ""
Write-Host "Pronto. App atualizado em $serial." -ForegroundColor Green
Write-Host "Copia em /sdcard/Download/muslimplus-debug.apk e em Downloads."
