$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$tabs = @(
  @{ name = "timeline"; x = 108; y = 2250 },
  @{ name = "prayers";  x = 324; y = 2250 },
  @{ name = "quran";    x = 540; y = 2250 },
  @{ name = "qibla";    x = 756; y = 2250 },
  @{ name = "more";     x = 972; y = 2250 }
)

foreach ($t in $tabs) {
  & $adb shell input tap $t.x $t.y | Out-Null
  Start-Sleep 6
  $out = & $adb shell dumpsys activity top 2>&1
  $adHits = $out | Select-String -Pattern "gms\.ads|AdView|AppLovinAdView|adview|MAXAd" 
  $count = ($adHits | Measure-Object).Count
  Write-Output ("{0}: linhas={1} hits_anuncio={2}" -f $t.name, $out.Count, $count)
  if ($count -gt 0) { $adHits | Select-Object -First 6 | ForEach-Object { "    " + $_.Line.Trim() } }
  & $adb exec-out screencap -p > ("C:\repo\muslim-pro-plus\evidence\tab-" + $t.name + ".png")
}

Write-Output "--- logcat applovin/ads ---"
& $adb logcat -d 2>&1 | Select-String -Pattern "AppLovin|MAX Ad|AdRequest|onAdLoaded|ads:" | Select-Object -First 15
