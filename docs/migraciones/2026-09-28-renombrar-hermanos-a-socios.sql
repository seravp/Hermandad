-- Puede ejecutarse también si se aplicó una primera versión incompleta.
-- Conserva los registros, sus números y la relación con las cuotas.

DO $$
BEGIN
    IF to_regclass('public.hermanos') IS NOT NULL
       AND to_regclass('public.socios') IS NULL THEN
        ALTER TABLE hermanos RENAME TO socios;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'socios'
          AND column_name = 'numero_hermano'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'socios'
          AND column_name = 'numero_socio'
    ) THEN
        ALTER TABLE socios RENAME COLUMN numero_hermano TO numero_socio;
    ELSIF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'socios'
          AND column_name = 'numero_hermano'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'socios'
          AND column_name = 'numero_socio'
    ) THEN
        UPDATE socios
        SET numero_socio = numero_hermano
        WHERE numero_socio IS NULL;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'cuotas'
          AND column_name = 'hermano_id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'cuotas'
          AND column_name = 'socio_id'
    ) THEN
        ALTER TABLE cuotas RENAME COLUMN hermano_id TO socio_id;
    END IF;
END $$;

-- Clasificación exclusiva del socio y tipo histórico de cada cuota.
ALTER TABLE socios ADD COLUMN IF NOT EXISTS tipo VARCHAR(20);
UPDATE socios SET tipo = 'HERMANO' WHERE tipo IS NULL;
ALTER TABLE socios ALTER COLUMN tipo SET NOT NULL;

ALTER TABLE cuotas ADD COLUMN IF NOT EXISTS tipo VARCHAR(20);
UPDATE cuotas c
SET tipo = s.tipo
FROM socios s
WHERE c.socio_id = s.id
  AND c.tipo IS NULL;
UPDATE cuotas SET tipo = 'HERMANO' WHERE tipo IS NULL;
ALTER TABLE cuotas ALTER COLUMN tipo SET NOT NULL;

-- Dos importes configurables, partiendo del antiguo importe único.
ALTER TABLE configuracion ADD COLUMN IF NOT EXISTS importe_cuota_hermano NUMERIC(19,2);
ALTER TABLE configuracion ADD COLUMN IF NOT EXISTS importe_cuota_costalero NUMERIC(19,2);
UPDATE configuracion
SET importe_cuota_hermano = importe_cuota
WHERE importe_cuota_hermano IS NULL;
UPDATE configuracion
SET importe_cuota_costalero = importe_cuota
WHERE importe_cuota_costalero IS NULL;
