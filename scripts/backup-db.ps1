[CmdletBinding()]
param(
    [string]$Destination = (Join-Path (Split-Path $PSScriptRoot -Parent) 'backups'),
    [string]$ContainerName = 'hermandad-postgres',
    [ValidateRange(1, 3650)]
    [int]$KeepDays = 14
)

$ErrorActionPreference = 'Stop'

$isRunning = (& docker inspect --format '{{.State.Running}}' $ContainerName 2>$null).Trim()
if ($LASTEXITCODE -ne 0 -or $isRunning -ne 'true') {
    throw "El contenedor '$ContainerName' no está en ejecución. Inicia Docker y el servicio PostgreSQL antes de crear la copia."
}

New-Item -ItemType Directory -Force -Path $Destination | Out-Null

$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$fileName = "hermandad_$timestamp.dump"
$backupPath = Join-Path $Destination $fileName
$containerPath = "/tmp/$fileName"
$dumpCommand = 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc -f ' + $containerPath

try {
    & docker exec $ContainerName sh -c $dumpCommand
    if ($LASTEXITCODE -ne 0) {
        throw 'PostgreSQL no ha podido generar la copia.'
    }

    & docker cp "${ContainerName}:$containerPath" $backupPath
    if ($LASTEXITCODE -ne 0) {
        throw 'No se ha podido copiar la copia de seguridad fuera del contenedor.'
    }

    Get-ChildItem -LiteralPath $Destination -Filter 'hermandad_*.dump' -File |
        Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-$KeepDays) } |
        Remove-Item -Force

    Write-Host "Copia creada: $backupPath"
}
finally {
    & docker exec $ContainerName rm -f $containerPath 2>$null | Out-Null
}
