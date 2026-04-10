ALTER TABLE jobfinder.employers
  ALTER COLUMN name SET NOT NULL,
  ALTER COLUMN email SET NOT NULL,
  ALTER COLUMN password_hash SET NOT NULL,
  ADD CONSTRAINT employers_email_unique UNIQUE (email),
  ADD CONSTRAINT employers_name_not_empty CHECK (btrim(name) <> ''),
  ADD CONSTRAINT employers_email_not_empty CHECK (btrim(email) <> ''),
  ADD CONSTRAINT employers_password_hash_not_empty CHECK (btrim(password_hash) <> '');

ALTER TABLE jobfinder.applicants
  ALTER COLUMN name SET NOT NULL,
  ALTER COLUMN email SET NOT NULL,
  ALTER COLUMN password_hash SET NOT NULL,
  ALTER COLUMN phone_number SET NOT NULL,
  ALTER COLUMN location_id SET NOT NULL,
  ADD CONSTRAINT applicants_email_unique UNIQUE (email),
  ADD CONSTRAINT applicants_name_not_empty CHECK (btrim(name) <> ''),
  ADD CONSTRAINT applicants_email_not_empty CHECK (btrim(email) <> ''),
  ADD CONSTRAINT applicants_password_hash_not_empty CHECK (btrim(password_hash) <> ''),
  ADD CONSTRAINT applicants_phone_number_not_empty CHECK (btrim(phone_number) <> '');

ALTER TABLE jobfinder.vacancies
  ALTER COLUMN employer_id SET NOT NULL,
  ALTER COLUMN location_id SET NOT NULL,
  ALTER COLUMN salary SET NOT NULL,
  ALTER COLUMN payment_frequency SET NOT NULL,
  ALTER COLUMN work_experience SET NOT NULL,
  ALTER COLUMN work_format SET NOT NULL,
  ALTER COLUMN description SET NOT NULL,
  ALTER COLUMN publication_date SET NOT NULL,
  ALTER COLUMN employment_type SET NOT NULL,
  ALTER COLUMN address SET NOT NULL,
  ADD CONSTRAINT vacancies_salary_non_negative CHECK (salary >= 0),
  ADD CONSTRAINT vacancies_work_experience_not_empty CHECK (btrim(work_experience) <> ''),
  ADD CONSTRAINT vacancies_description_not_empty CHECK (btrim(description) <> ''),
  ADD CONSTRAINT vacancies_address_not_empty CHECK (btrim(address) <> '');

ALTER TABLE jobfinder.applications
  ALTER COLUMN vacancy_id SET NOT NULL,
  ALTER COLUMN resume_id SET NOT NULL,
  ALTER COLUMN date SET NOT NULL,
  ALTER COLUMN status SET NOT NULL;

ALTER TABLE jobfinder.educations
  ALTER COLUMN resume_id SET NOT NULL,
  ALTER COLUMN institution SET NOT NULL,
  ALTER COLUMN faculty SET NOT NULL,
  ALTER COLUMN department SET NOT NULL,
  ALTER COLUMN graduation_year SET NOT NULL,
  ALTER COLUMN degree SET NOT NULL,
  ADD CONSTRAINT educations_institution_not_empty CHECK (btrim(institution) <> ''),
  ADD CONSTRAINT educations_faculty_not_empty CHECK (btrim(faculty) <> ''),
  ADD CONSTRAINT educations_department_not_empty CHECK (btrim(department) <> ''),
  ADD CONSTRAINT educations_graduation_year_valid CHECK (graduation_year >= 1900 AND graduation_year <= EXTRACT(YEAR FROM CURRENT_DATE) + 10);

ALTER TABLE jobfinder.languages
  ALTER COLUMN name SET NOT NULL,
  ALTER COLUMN proficiency_level SET NOT NULL,
  ADD CONSTRAINT languages_name_not_empty CHECK (btrim(name) <> '');

ALTER TABLE jobfinder.language_resume
  ALTER COLUMN language_id SET NOT NULL,
  ALTER COLUMN resume_id SET NOT NULL,
  ADD CONSTRAINT language_resume_unique UNIQUE (resume_id, language_id);

ALTER TABLE jobfinder.language_vacancy
  ALTER COLUMN language_id SET NOT NULL,
  ALTER COLUMN vacancy_id SET NOT NULL,
  ADD CONSTRAINT language_vacancy_unique UNIQUE (vacancy_id, language_id);

ALTER TABLE jobfinder.resumes
  ALTER COLUMN applicant_id SET NOT NULL,
  ALTER COLUMN category_id SET NOT NULL,
  ALTER COLUMN description SET NOT NULL,
  ALTER COLUMN creation_date SET NOT NULL,
  ADD CONSTRAINT resumes_description_not_empty CHECK (btrim(description) <> '');

ALTER TABLE jobfinder.categories
  ALTER COLUMN name SET NOT NULL,
  ADD CONSTRAINT categories_name_unique UNIQUE (name),
  ADD CONSTRAINT categories_name_not_empty CHECK (btrim(name) <> '');

ALTER TABLE jobfinder.locations
  ALTER COLUMN city SET NOT NULL,
  ALTER COLUMN country SET NOT NULL,
  ADD CONSTRAINT locations_city_country_unique UNIQUE (city, country),
  ADD CONSTRAINT locations_city_not_empty CHECK (btrim(city) <> ''),
  ADD CONSTRAINT locations_country_not_empty CHECK (btrim(country) <> '');

ALTER TABLE jobfinder.experiences
  ALTER COLUMN resume_id SET NOT NULL,
  ALTER COLUMN position SET NOT NULL,
  ALTER COLUMN start_date SET NOT NULL,
  ALTER COLUMN company_name SET NOT NULL,
  ADD CONSTRAINT experiences_position_not_empty CHECK (btrim(position) <> ''),
  ADD CONSTRAINT experiences_company_name_not_empty CHECK (btrim(company_name) <> ''),
  ADD CONSTRAINT experiences_dates_valid CHECK (end_date IS NULL OR start_date <= end_date);

ALTER TABLE jobfinder.skills
  ALTER COLUMN name SET NOT NULL,
  ADD CONSTRAINT skills_name_unique UNIQUE (name),
  ADD CONSTRAINT skills_name_not_empty CHECK (btrim(name) <> '');

ALTER TABLE jobfinder.vacancy_skills
  ALTER COLUMN vacancy_id SET NOT NULL,
  ALTER COLUMN skill_id SET NOT NULL,
  ADD CONSTRAINT vacancy_skills_unique UNIQUE (vacancy_id, skill_id);

ALTER TABLE jobfinder.resume_skills
  ALTER COLUMN resume_id SET NOT NULL,
  ALTER COLUMN skill_id SET NOT NULL,
  ADD CONSTRAINT resume_skills_unique UNIQUE (resume_id, skill_id);

ALTER TABLE jobfinder.vacancies ADD CONSTRAINT fk_vacancies_employer FOREIGN KEY (employer_id) REFERENCES jobfinder.employers (id) DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE jobfinder.applications ADD CONSTRAINT fk_applications_vacancy FOREIGN KEY (vacancy_id) REFERENCES jobfinder.vacancies (id) DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE jobfinder.vacancies ADD CONSTRAINT fk_vacancies_location FOREIGN KEY (location_id) REFERENCES jobfinder.locations(id) DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE jobfinder.vacancy_skills ADD CONSTRAINT fk_vacancy_skills_vacancy FOREIGN KEY (vacancy_id) REFERENCES jobfinder.vacancies (id) DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE jobfinder.vacancy_skills ADD CONSTRAINT fk_vacancy_skills_skill FOREIGN KEY (skill_id) REFERENCES jobfinder.skills (id) DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE jobfinder.resumes ADD CONSTRAINT fk_resumes_applicant FOREIGN KEY (applicant_id) REFERENCES jobfinder.applicants (id) DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE jobfinder.resumes ADD CONSTRAINT fk_resumes_category FOREIGN KEY (category_id) REFERENCES jobfinder.categories (id) DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE jobfinder.applications ADD CONSTRAINT fk_applications_resume FOREIGN KEY (resume_id) REFERENCES jobfinder.resumes (id) DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE jobfinder.experiences ADD CONSTRAINT fk_experiences_resume FOREIGN KEY (resume_id) REFERENCES jobfinder.resumes (id) DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE jobfinder.educations ADD CONSTRAINT fk_educations_resume FOREIGN KEY (resume_id) REFERENCES jobfinder.resumes (id) DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE jobfinder.resume_skills ADD CONSTRAINT fk_resume_skills_resume FOREIGN KEY (resume_id) REFERENCES jobfinder.resumes (id) DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE jobfinder.resume_skills ADD CONSTRAINT fk_resume_skills_skill FOREIGN KEY (skill_id) REFERENCES jobfinder.skills (id) DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE jobfinder.language_resume ADD CONSTRAINT fk_language_resume_language FOREIGN KEY (language_id) REFERENCES jobfinder.languages (id) DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE jobfinder.language_resume ADD CONSTRAINT fk_language_resume_resume FOREIGN KEY (resume_id) REFERENCES jobfinder.resumes (id) DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE jobfinder.language_vacancy ADD CONSTRAINT fk_language_vacancy_language FOREIGN KEY (language_id) REFERENCES jobfinder.languages (id) DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE jobfinder.language_vacancy ADD CONSTRAINT fk_language_vacancy_vacancy FOREIGN KEY (vacancy_id) REFERENCES jobfinder.vacancies (id) DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE jobfinder.applicants ADD CONSTRAINT fk_applicants_location FOREIGN KEY (location_id) REFERENCES jobfinder.locations (id) DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE jobfinder.employers ADD CONSTRAINT fk_applicants_location FOREIGN KEY (location_id) REFERENCES jobfinder.locations (id) DEFERRABLE INITIALLY IMMEDIATE;