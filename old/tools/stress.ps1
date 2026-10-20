param(
    [Parameter(Mandatory = $true)][string]$Apk,
    [string]$Pkg = "com.bitsmedia.android.muslimpro",
    [int]$Rounds = 3,
    [int]$WaitSec = 40
)

$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$activity = "$Pkg/com.bitsmedia.android.muslimpro.core.activities.SplashActivity"

& $adb uninstall $Pkg 2>$null | Out-Null
& $adb install -r $Apk 2>&1 | Select-Object -Last 1

$crashes = 0
for ($i = 1; $i -le $Rounds; $i++) {
    & $adb logcat -c
    & $adb logcat -b crash -c
    & $adb shell am start -n $activity | Out-Null
    Start-Sleep -Seconds $WaitSec
    $pid = (& $adb shell pidof $Pkg) -join ""
    $crash = & $adb logcat -d -b crash 2>&1 | Select-String "FATAL EXCEPTION"
    if ($crash) { $crashes++ }
    Write-Output ("rodada {0}: pid={1} crash={2}" -f $i, ($(if ($pid) { $pid } else { "morto" })), ($(if ($crash) { "SIM" } else { "nao" })))
    if ($crash) { $crash | Select-Object -First 1 | ForEach-Object { "   " + $_.Line.Trim() } }
}

Write-Output ("resumo: {0}/{1} rodadas com crash" -f $crashes, $Rounds)
