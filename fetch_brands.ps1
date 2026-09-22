$brands = @('LG', 'Samsung', 'Daikin', 'Hitachi', 'Panasonic', 'Carrier', 'Voltas', 'Blue_Star', 'General_Electric')
$noop = $null
foreach ($b in $brands) {
    Write-Host "=== $b ==="
    try {
        $r = Invoke-RestMethod -Uri "https://api.github.com/repos/Lucaslhm/Flipper-IRDB/contents/ACs/$b" -ErrorAction Stop
        $r | ForEach-Object { Write-Host "$($_.name) $($_.download_url)" }
    } catch {
        Write-Host "NOT FOUND"
    }
}
