$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$routes = @("prayers", "quran", "qibla", "more", "inspiration", "duas")

foreach ($r in $routes) {
  & $adb shell am start -a android.intent.action.VIEW -d "muslimpro://$r" | Out-Null
  Start-Sleep 7
  $out = & $adb shell dumpsys activity top 2>&1
  $hits = $out | Select-String -Pattern "gms\.ads|AdView|AppLovinAdView|MAXAdView|adview"
  $top = ($out | Select-String "DecorView@.*\[" | Select-Object -Last 1).Line.Trim()
  Write-Output ("{0,-12} top={1} linhas={2} hits={3}" -f $r, ($top -replace "\s+", " "), $out.Count, ($hits | Measure-Object).Count)
  if (($hits | Measure-Object).Count -gt 0) {
    $hits | Select-Object -First 8 | ForEach-Object { "     " + $_.Line.Trim() }
  }
  & $adb exec-out screencap -p > ("C:\repo\muslim-pro-plus\evidence\deep-" + $r + ".png")
}
