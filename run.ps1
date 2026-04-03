$ErrorActionPreference = "Stop"
$src = Get-ChildItem -Recurse -Filter *.java -Path src | ForEach-Object { $_.FullName }
$out = "target\classes"

if (-not (Test-Path $out)) { New-Item -ItemType Directory -Path $out | Out-Null }

Write-Host "Compiling..."
javac -d $out @($src)

Write-Host "Running..."
java -cp $out org.delightofcomposition.MarkovMain
