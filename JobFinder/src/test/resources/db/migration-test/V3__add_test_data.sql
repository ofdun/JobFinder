INSERT INTO jobfinder.languages (name, proficiency_level) VALUES
('English', 'A1'),
('English', 'A2'),
('English', 'B1'),
('English', 'B2'),
('English', 'C1'),
('English', 'C2'),
('Russian', 'A1'),
('Russian', 'A2'),
('Russian', 'B1'),
('Russian', 'B2'),
('Russian', 'C1'),
('Russian', 'C2');

INSERT INTO jobfinder.locations (id, city, country)
SELECT 1, 'Vila', 'Andorra'
WHERE NOT EXISTS (SELECT 1 FROM jobfinder.locations WHERE city = 'Vila' AND country = 'Andorra');

INSERT INTO jobfinder.locations (id, city, country)
SELECT 2, 'Moscow', 'Russia'
WHERE NOT EXISTS (SELECT 1 FROM jobfinder.locations WHERE city = 'Moscow' AND country = 'Russia');

INSERT INTO jobfinder.skills (name) VALUES
('Python'),
('Java'),
('C++');
INSERT INTO jobfinder.categories (name) VALUES
('Software Development'),
('Data Science');

INSERT INTO jobfinder.applicants (id, name, email, password_hash, address, phone_number, location_id)
SELECT 1, 'Alice', 'alice@example.com', 'passHash', 'Address 1', '+70000000000', 1
WHERE NOT EXISTS (SELECT 1 FROM jobfinder.applicants WHERE email = 'alice@example.com');

INSERT INTO jobfinder.applicants (id, name, email, password_hash, address, phone_number, location_id)
SELECT 2, 'Bob', 'bob@example.com', 'passHash', 'Address 2', '+70000000001', 1
WHERE NOT EXISTS (SELECT 1 FROM jobfinder.applicants WHERE email = 'bob@example.com');

INSERT INTO jobfinder.applicants (id, name, email, password_hash, address, phone_number, location_id)
SELECT 3, 'Carol', 'carol@example.com', 'passHash', 'Address 3', '+70000000002', 1
WHERE NOT EXISTS (SELECT 1 FROM jobfinder.applicants WHERE email = 'carol@example.com');

INSERT INTO jobfinder.applicants (id, name, email, password_hash, address, phone_number, location_id)
SELECT 4, 'Dave', 'dave@example.com', 'passHash', 'Address 4', '+70000000003', 1
WHERE NOT EXISTS (SELECT 1 FROM jobfinder.applicants WHERE email = 'dave@example.com');

INSERT INTO jobfinder.employers (id, name, email, password_hash, address, location_id)
SELECT 1, 'Test Employer', 'employer@example.com', 'passHash', 'Employer Address', 2
WHERE NOT EXISTS (SELECT 1 FROM jobfinder.employers WHERE email = 'employer@example.com');

INSERT INTO jobfinder.vacancies (id, employer_id, location_id, salary, payment_frequency, work_experience, work_format, description, publication_date, employment_type, address)
SELECT 1, 1, 2, 1000, 'MONTHLY', '3 years', 'OFFICE', 'Test vacancy', now(), 'FULL_TIME', 'Vacancy address'
WHERE NOT EXISTS (SELECT 1 FROM jobfinder.vacancies WHERE id = 1);

INSERT INTO jobfinder.resumes (id, applicant_id, category_id, description, creation_date)
SELECT 1, 1, 1, 'Test resume', now()
WHERE NOT EXISTS (SELECT 1 FROM jobfinder.resumes WHERE id = 1);

INSERT INTO jobfinder.resumes (id, applicant_id, category_id, description, creation_date)
SELECT 2, 2, 1, 'Bob resume', now()
WHERE NOT EXISTS (SELECT 1 FROM jobfinder.resumes WHERE id = 2);

INSERT INTO jobfinder.applications (id, vacancy_id, resume_id, date, status)
SELECT 1, 1, 1, now(), 'NEW'
WHERE NOT EXISTS (SELECT 1 FROM jobfinder.applications WHERE id = 1);

INSERT INTO jobfinder.applications (id, vacancy_id, resume_id, date, status)
SELECT 2, 1, 2, now(), 'INVITATION'
WHERE NOT EXISTS (SELECT 1 FROM jobfinder.applications WHERE id = 2);

SELECT setval(
  pg_get_serial_sequence('jobfinder.applicants','id'),
  (SELECT COALESCE(MAX(id), 1) FROM jobfinder.applicants),
  true
);

SELECT setval(
  pg_get_serial_sequence('jobfinder.locations','id'),
  (SELECT COALESCE(MAX(id), 1) FROM jobfinder.locations),
  true
);

SELECT setval(
  pg_get_serial_sequence('jobfinder.employers','id'),
  (SELECT COALESCE(MAX(id), 1) FROM jobfinder.employers),
  true
);

SELECT setval(
  pg_get_serial_sequence('jobfinder.vacancies','id'),
  (SELECT COALESCE(MAX(id), 1) FROM jobfinder.vacancies),
  true
);

SELECT setval(
  pg_get_serial_sequence('jobfinder.resumes','id'),
  (SELECT COALESCE(MAX(id), 1) FROM jobfinder.resumes),
  true
);

SELECT setval(
  pg_get_serial_sequence('jobfinder.applications','id'),
  (SELECT COALESCE(MAX(id), 1) FROM jobfinder.applications),
  true
);
