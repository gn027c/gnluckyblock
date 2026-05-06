Add-Type -AssemblyName 'System.IO.Compression.FileSystem'
$jarPath = Resolve-Path 'bootstrap\paper\build\libs\gnluckyblock.jar'
$zip = [IO.Compression.ZipFile]::OpenRead($jarPath)
$entry = $zip.Entries | Where-Object { $_.FullName -eq 'plugin.yml' }
if ($entry) {
    $stream = $entry.Open()
    $reader = New-Object IO.StreamReader($stream)
    Write-Output $reader.ReadToEnd()
    $reader.Close()
    $stream.Close()
} else {
    Write-Output "plugin.yml NOT FOUND in JAR!"
    Write-Output "All entries:"
    $zip.Entries | ForEach-Object { Write-Output $_.FullName } | Select-String 'plugin'
}
$zip.Dispose()
