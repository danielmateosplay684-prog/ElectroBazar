$files = Get-ChildItem -Path "src\main\resources\templates\admin" -Recurse -Filter *.html
$regex = "[^\x00-\x7FáéíóúÁÉÍÓÚñÑüÜ€—\n\r\t]"
foreach ($f in $files) {
    if ($f.Name -match ".bak") { continue }
    Write-Host "Cleaning $($f.FullName)"
    $c = Get-Content -Path $f.FullName -Raw -Encoding UTF8
    $c = $c -replace 'á', 'á'
    $c = $c -replace 'é', 'é'
    $c = $c -replace 'í', 'í'
    $c = $c -replace 'ó', 'ó'
    $c = $c -replace 'ú', 'ú'
    $c = $c -replace 'ñ', 'ñ'
    $c = [Regex]::Replace($c, $regex, "")
    $c | Set-Content -Path $f.FullName -Encoding UTF8
}
