CREATE TABLE jobfinder.draft_vacancies (
    id BIGSERIAL PRIMARY KEY,
    vacancy_id BIGINT NOT NULL REFERENCES jobfinder.vacancies(id) ON DELETE CASCADE,
    version_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    snapshot TEXT NOT NULL
);
CREATE INDEX idx_draft_vacancies_vacancy ON jobfinder.draft_vacancies(vacancy_id);
GRANT SELECT, INSERT, UPDATE, DELETE ON jobfinder.draft_vacancies TO db_admin, employer_role;
GRANT USAGE, SELECT ON SEQUENCE jobfinder.draft_vacancies_id_seq TO db_admin, employer_role;

-- Optional resume text may be cleared by full replacement; an ongoing job has no end date.
ALTER TABLE jobfinder.resumes ALTER COLUMN description DROP NOT NULL;
ALTER TABLE jobfinder.resumes DROP CONSTRAINT resumes_description_not_empty;
ALTER TABLE jobfinder.experiences ALTER COLUMN end_date DROP NOT NULL;
ALTER TABLE jobfinder.applicants ALTER COLUMN phone_number DROP NOT NULL;
