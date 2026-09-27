[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [string]$BackupFile,
    [string]$DatabaseContainerName = 'hermandad-postgres',
    [string]$BackendContainerName = 'hermandad-backend',
    [switch]$Force
)

$ErrorActionPreference = 'Stop'

$backupPath = (Resolve-Path -LiteralPath $BackupFile).Path
$databaseRunning = (& docker inspect --format '{{.State.Running}}' $DatabaseContainerName 2>$null).Trim()
if ($LASTEXITCODE -ne 0 -or $databaseRunning -ne 'true') {
    throw "El contenedor '$DatabaseContainerName' no está en ejecución."
}

$backendRunning = (& docker inspect --format '{{.State.Running}}' $BackendContainerName 2>$null).Trim()
if ($backendRunning -eq 'true') {
    throw "Detén primero el backend ('$BackendContainerName') para evitar escrituras durante la restauración."
}

if (-not $Force) {
    $answer = Read-Host 'Esta operación sustituirá los datos actuales. Escribe RESTAURAR para continuar'
    if ($answer -ne 'RESTAURAR') {
        Write-Host 'Restauración cancelada.'
        return
    }
}

$containerPath = '/tmp/hermandad_restore.dump'
$restoreCommand = 'pg_restore -U "$POSTGRES_USER" -d "$POSTGRES_DB" --clean --if-exists --no-owner ' + $containerPath

try {
    & docker cp $backupPath "${DatabaseContainerName}:$containerPath"
    if ($LASTEXITCODE -ne 0) {
        throw 'No se ha podido copiar la copia al contenedor PostgreSQL.'
    }

    & docker exec $DatabaseContainerName sh -c $restoreCommand
    if ($LASTEXITCODE -ne 0) {
        throw 'PostgreSQL no ha podido restaurar la copia.'
    }

    Write-Host 'Restauración completada. Ya puedes iniciar el backend.'
}
finally {
    & docker exec $DatabaseContainerName rm -f $containerPath 2>$null | Out-Null
}
