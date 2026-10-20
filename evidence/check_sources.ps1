$ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
$urls = @(
  "https://apkcombo.app/muslim-pro/com.bitsmedia.android.muslimpro/",
  "https://apkpure.net/muslim-pro-prayer-times-quran/com.bitsmedia.android.muslimpro",
  "https://play.google.com/store/apps/details?id=com.bitsmedia.android.muslimpro&hl=en"
)
foreach ($u in $urls) {
  try {
    $r = Invoke-WebRequest -Uri $u -Headers @{ "User-Agent" = $ua; "Accept-Language" = "en-US,en;q=0.9" } -TimeoutSec 30 -ErrorAction Stop
    $m = [regex]::Matches($r.Content, '(\d+\.\d+\.\d+)') | Select-Object -First 8
    $hits = ($m | ForEach-Object { $_.Value }) -join ","
    Write-Output "OK $u status=$($r.StatusCode) len=$($r.Content.Length) vers=$hits"
  } catch {
    Write-Output "ERR $u => $($_.Exception.Message)"
  }
}
