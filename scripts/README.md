# Copias de seguridad

Las copias se generan en `backups/` y se guardan 14 días de forma predeterminada.

## Crear una copia manual

Ejecuta desde la carpeta `backend`:

```powershell
.\scripts\backup-db.ps1
```

## Programar la copia diaria

Ejecuta una vez desde tu sesión de Windows, con Docker Desktop iniciado:

```powershell
.\scripts\install-backup-task.ps1
```

Por defecto se ejecuta cada día a las 02:00. Para elegir otra hora o retención:

```powershell
.\scripts\install-backup-task.ps1 -Time '03:30' -KeepDays 30
```

## Restaurar una copia

Primero detén el backend para evitar escrituras. Después ejecuta:

```powershell
.\scripts\restore-db.ps1 -BackupFile .\backups\hermandad_YYYYMMDD_HHMMSS.dump
```

El script exige confirmar escribiendo `RESTAURAR` y no elimina el archivo de copia.
