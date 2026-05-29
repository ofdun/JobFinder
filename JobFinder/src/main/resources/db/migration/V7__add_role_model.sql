DO $$
BEGIN
	IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'db_admin') THEN
		CREATE ROLE db_admin;
	END IF;
	IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'employer_role') THEN
		CREATE ROLE employer_role;
	END IF;
	IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'applicant_role') THEN
		CREATE ROLE applicant_role;
	END IF;
	IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'guest_role') THEN
		CREATE ROLE guest_role;
	END IF;
END $$;

GRANT USAGE ON SCHEMA jobfinder TO db_admin, employer_role, applicant_role, guest_role;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA jobfinder TO db_admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA jobfinder TO db_admin;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA jobfinder TO db_admin;
GRANT ALL PRIVILEGES ON ALL PROCEDURES IN SCHEMA jobfinder TO db_admin;

GRANT SELECT, UPDATE ON jobfinder.employers TO employer_role;
GRANT SELECT, UPDATE ON jobfinder.applications TO employer_role;

GRANT SELECT, INSERT, UPDATE, DELETE ON jobfinder.vacancies TO employer_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON jobfinder.language_vacancy TO employer_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON jobfinder.vacancy_skills TO employer_role;

GRANT SELECT ON jobfinder.applicants TO employer_role;
GRANT SELECT ON jobfinder.resumes TO employer_role;
GRANT SELECT ON jobfinder.educations TO employer_role;
GRANT SELECT ON jobfinder.experiences TO employer_role;
GRANT SELECT ON jobfinder.resume_skills TO employer_role;
GRANT SELECT ON jobfinder.language_resume TO employer_role;
GRANT SELECT ON jobfinder.languages TO employer_role;
GRANT SELECT ON jobfinder.skills TO employer_role;
GRANT SELECT ON jobfinder.locations TO employer_role;
GRANT SELECT ON jobfinder.categories TO employer_role;

GRANT EXECUTE ON PROCEDURE jobfinder.accept_one_applicant TO employer_role;

GRANT SELECT, UPDATE ON jobfinder.applicants TO applicant_role;

GRANT SELECT, INSERT ON jobfinder.applications TO applicant_role;

GRANT SELECT, INSERT, UPDATE, DELETE ON jobfinder.resumes TO applicant_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON jobfinder.educations TO applicant_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON jobfinder.experiences TO applicant_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON jobfinder.language_resume TO applicant_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON jobfinder.resume_skills TO applicant_role;

GRANT SELECT ON jobfinder.employers TO applicant_role;
GRANT SELECT ON jobfinder.vacancies TO applicant_role;
GRANT SELECT ON jobfinder.language_vacancy TO applicant_role;
GRANT SELECT ON jobfinder.vacancy_skills TO applicant_role;
GRANT SELECT ON jobfinder.languages TO applicant_role;
GRANT SELECT ON jobfinder.skills TO applicant_role;
GRANT SELECT ON jobfinder.locations TO applicant_role;
GRANT SELECT ON jobfinder.categories TO applicant_role;

GRANT INSERT ON jobfinder.employers TO guest_role;
GRANT INSERT ON jobfinder.applicants TO guest_role;

GRANT SELECT ON jobfinder.vacancies TO guest_role;
GRANT SELECT ON jobfinder.language_vacancy TO guest_role;
GRANT SELECT ON jobfinder.vacancy_skills TO guest_role;
GRANT SELECT ON jobfinder.languages TO guest_role;
GRANT SELECT ON jobfinder.skills TO guest_role;
GRANT SELECT ON jobfinder.locations TO guest_role;
GRANT SELECT ON jobfinder.categories TO guest_role;

GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA jobfinder TO guest_role;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA jobfinder TO applicant_role;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA jobfinder TO employer_role;
