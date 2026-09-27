[CmdletBinding()]
param(
    [string]$Time = '02:00',
    [int]$KeepDays = 14
)

$ErrorActionPreference = 'Stop'
$backupScript = Join-Path $PSScriptRoot 'backup-db.ps1'
$taskName = 'Hermandad - Copia de seguridad diaria'
$arguments = "-NoProfile -ExecutionPolicy Bypass -File `"$backupScript`" -KeepDays $KeepDays"

$action = New-ScheduledTaskAction -Execute 'powershell.exe' -Argument $arguments
$trigger = New-ScheduledTaskTrigger -Daily -At $Time
$principal = New-ScheduledTaskPrincipal -UserId ([System.Security.Principal.WindowsIdentity]::GetCurrent().Name) -LogonType Interactive -RunLevel Limited

Register-ScheduledTask -TaskName $taskName -Action $action -Trigger $trigger -Principal $principal -Description 'Copia diaria de PostgreSQL de Gestión de Hermandad.' -Force | Out-Null
Write-Host "Tarea diaria creada: $taskName a las $Time"
