CREATE TYPE jobfinder.vacancy_status AS ENUM ('INACTIVE','ACTIVE');
ALTER TABLE jobfinder.vacancies ADD COLUMN status jobfinder.vacancy_status NOT NULL DEFAULT 'ACTIVE';
CREATE TABLE jobfinder.draft_vacancies (
    id BIGSERIAL PRIMARY KEY,
    vacancy_id BIGINT NOT NULL REFERENCES jobfinder.vacancies(id) ON DELETE CASCADE,
    version_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    snapshot TEXT NOT NULL
);
ALTER TABLE jobfinder.resumes ALTER COLUMN description DROP NOT NULL;
ALTER TABLE jobfinder.resumes DROP CONSTRAINT resumes_description_not_empty;
ALTER TABLE jobfinder.experiences ALTER COLUMN end_date DROP NOT NULL;
ALTER TABLE jobfinder.applicants ALTER COLUMN phone_number DROP NOT NULL;
ALTER TABLE jobfinder.applications ADD CONSTRAINT applications_vacancy_resume_unique UNIQUE (vacancy_id,resume_id);
