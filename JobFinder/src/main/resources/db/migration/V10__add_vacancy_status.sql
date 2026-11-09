CREATE TYPE jobfinder.vacancy_status AS ENUM ('INACTIVE', 'ACTIVE');
ALTER TABLE jobfinder.vacancies
    ADD COLUMN status jobfinder.vacancy_status NOT NULL DEFAULT 'ACTIVE';
