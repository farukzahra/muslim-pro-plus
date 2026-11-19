# Sobe um emulador de teste sempre sem som.
#
# Regra do projeto: o emulador nunca sobe com áudio, porque o adhan do app
# toca no alto-falante da máquina durante os testes e nas capturas da loja.
#
# Uso:
#   .\scripts\start-emulator.ps1             # AVD Pixel6
#   .\scripts\start-emulator.ps1 Pixel6Test  # outro AVD
#
# Saída esperada:
#   waiting for device
#   boot completed: 1

param(
    [string]$Avd = "Pixel6"
)

$ErrorActionPreference = "Stop"
$emulator = "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe"
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"

if (-not (Test-Path $emulator)) { throw "emulador ausente: $emulator" }

$running = & $adb devices | Select-String "emulator-\d+\s+device"
if ($running) {
    Write-Output "emulador já conectado: $($running -join ', ')"
    exit 0
}

Start-Process -FilePath $emulator `
    -ArgumentList "-avd", $Avd, "-no-audio", "-no-boot-anim" `
    -WindowStyle Minimized

Write-Output "waiting for device"
& $adb wait-for-device | Out-Null

for ($i = 0; $i -lt 90; $i++) {
    $boot = (& $adb shell getprop sys.boot_completed 2>$null).Trim()
    if ($boot -eq "1") { break }
    Start-Sleep -Seconds 2
}

$boot = (& $adb shell getprop sys.boot_completed 2>$null).Trim()
Write-Output "boot completed: $boot"
if ($boot -ne "1") { throw "emulador não concluiu o boot" }
