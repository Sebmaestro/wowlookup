param()

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendCommand = "cd /d `"$root`" && call mvnw.cmd spring-boot:run"
$frontendCommand = "cd /d `"$root\frontend`" && npm run dev"

Start-Process -FilePath "cmd.exe" -ArgumentList "/k", $backendCommand
Start-Process -FilePath "cmd.exe" -ArgumentList "/k", $frontendCommand

Write-Host "Started backend and frontend in separate windows."
Write-Host "Backend:  http://localhost:8080"
Write-Host "Frontend: http://localhost:5173"