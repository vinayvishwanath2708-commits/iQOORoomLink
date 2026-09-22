$urls = @{
    'LG' = 'https://raw.githubusercontent.com/Lucaslhm/Flipper-IRDB/main/ACs/LG/LG_AC.ir'
    'Samsung' = 'https://raw.githubusercontent.com/Lucaslhm/Flipper-IRDB/main/ACs/Samsung/Samsung_AC_AR12K.ir'
    'Daikin' = 'https://raw.githubusercontent.com/Lucaslhm/Flipper-IRDB/main/ACs/Daikin/Daikin_AC.ir'
    'Hitachi' = 'https://raw.githubusercontent.com/Lucaslhm/Flipper-IRDB/main/ACs/Hitachi/Hitachi_RAK35.ir'
    'Panasonic' = 'https://raw.githubusercontent.com/Lucaslhm/Flipper-IRDB/main/ACs/Panasonic/Panasonic_A75C4187.ir'
    'Carrier' = 'https://raw.githubusercontent.com/Lucaslhm/Flipper-IRDB/main/ACs/Carrier/Carrier_orlen_ac.ir'
    'GE' = 'https://raw.githubusercontent.com/Lucaslhm/Flipper-IRDB/main/ACs/General_Electric/GE_AC.ir'
}

$outDir = "ir_raw"
if (!(Test-Path $outDir)) { New-Item -ItemType Directory -Path $outDir | Out-Null }

foreach ($kv in $urls.GetEnumerator()) {
    $outFile = "$outDir/$($kv.Key).ir"
    Write-Host "Downloading $($kv.Key)..."
    Invoke-WebRequest -Uri $kv.Value -OutFile $outFile -ErrorAction Stop
    Write-Host "  Saved to $outFile ($(Get-Item $outFile | Select -Expand Length) bytes)"
}
Write-Host "Done!"
