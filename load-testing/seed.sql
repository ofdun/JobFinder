CREATE EXTENSION IF NOT EXISTS pgcrypto;
WITH credential AS MATERIALIZED (SELECT crypt('load-password', '$2a$10$abcdefghijklmnopqrstuu') AS hash)
INSERT INTO jobfinder.applicants(id, name, email, password_hash, address, phone_number, location_id)
SELECT 1000 + n, 'Load applicant ' || n, 'load-' || n || '@example.test',
       credential.hash, 'Test', '+70000000000', 1
FROM generate_series(1, 1024) AS n CROSS JOIN credential;
INSERT INTO jobfinder.resumes(id, applicant_id, category_id, description, creation_date)
SELECT 1000 + n, 1000 + n, 1, 'Java developer ' || n, TIMESTAMP '2026-01-01 00:00:00'
FROM generate_series(1, 1024) AS n;
INSERT INTO jobfinder.vacancies(id, employer_id, location_id, salary, payment_frequency,
                              work_experience, work_format, description, publication_date,
                              employment_type, address)
SELECT 1000 + n, 1, 1, 50000 + n * 100, 'MONTHLY', 'Java', 'REMOTE',
       'Java backend developer ' || n, TIMESTAMP '2026-01-01 00:00:00', 'FULL_TIME', 'Remote'
FROM generate_series(1, 1000) AS n;
SELECT setval(pg_get_serial_sequence('jobfinder.applicants', 'id'), (SELECT max(id) FROM jobfinder.applicants));
SELECT setval(pg_get_serial_sequence('jobfinder.resumes', 'id'), (SELECT max(id) FROM jobfinder.resumes));
SELECT setval(pg_get_serial_sequence('jobfinder.vacancies', 'id'), (SELECT max(id) FROM jobfinder.vacancies));
