DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_type t JOIN pg_namespace n ON n.oid = t.typnamespace
        WHERE n.nspname = 'jobfinder' AND t.typname = 'vacancy_status'
    ) THEN
        CREATE TYPE jobfinder.vacancy_status AS ENUM ('INACTIVE', 'ACTIVE');
    END IF;
END
$$;

ALTER TABLE jobfinder.vacancies
    ADD COLUMN IF NOT EXISTS status jobfinder.vacancy_status NOT NULL DEFAULT 'ACTIVE';
