# Check Java installations
Write-Host "Checking for Java installations..." -ForegroundColor Green

Write-Host "`nChecking JAVA_HOME:" -ForegroundColor Yellow
$env:JAVA_HOME

Write-Host "`nChecking for Java in PATH:" -ForegroundColor Yellow
try {
    $javaPath = Get-Command java -ErrorAction SilentlyContinue
    if ($javaPath) {
        Write-Host "Java found in PATH: $($javaPath.Source)"
        java -version 2>&1
    } else {
        Write-Host "Java not found in PATH"
    }
} catch {
    Write-Host "Error checking Java in PATH: $_"
}

Write-Host "`nChecking common Java installation directories:" -ForegroundColor Yellow

$javaPaths = @(
    "C:\Program Files\Java\jdk-24",
    "C:\Program Files\Java\jdk-17",
    "C:\Program Files\Java\jdk-11",
    "C:\Program Files\Java\jdk-8"
)

foreach ($path in $javaPaths) {
    if (Test-Path $path) {
        Write-Host "Found: $path" -ForegroundColor Green
        $javaExe = Join-Path $path "bin\java.exe"
        if (Test-Path $javaExe) {
            try {
                $versionInfo = & $javaExe -version 2>&1
                Write-Host "Version: $versionInfo" -ForegroundColor Cyan
            } catch {
                Write-Host "Error getting version for $path" -ForegroundColor Red
            }
        }
    } else {
        Write-Host "Not found: $path" -ForegroundColor Gray
    }
}

Write-Host "`nDone." -ForegroundColor Green