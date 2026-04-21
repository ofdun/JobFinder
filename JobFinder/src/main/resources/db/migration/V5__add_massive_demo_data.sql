-- Seed data for business entities only. Dictionary tables (locations/categories/skills/languages) are not modified.

INSERT INTO jobfinder.applicants (name, address, email, password_hash, phone_number, location_id)
SELECT
  'Соискатель ' || gs,
  'ул. Тестовая, д. ' || ((gs % 250) + 1),
  'seed.applicant.' || gs || '@jobfinder.local',
  '$2a$10$0FYFBrRIdbTfE0VvKD60He6Fy/ASahx8K68nkf2TTaR8vLa28FUze',
  '+79' || lpad((900000000 + gs)::text, 9, '0'),
  loc.ids[1 + ((gs - 1) % loc.cnt)]
FROM generate_series(1, 500) AS gs
CROSS JOIN (
  SELECT array_agg(id ORDER BY id) AS ids, COUNT(*)::int AS cnt
  FROM jobfinder.locations
) AS loc
WHERE loc.cnt > 0
ON CONFLICT (email) DO NOTHING;

INSERT INTO jobfinder.employers (name, description, address, website_link, email, password_hash, location_id)
SELECT
  'Компания ' || gs,
  'Компания для массового наполнения базы #' || gs,
  'Бизнес-центр ' || ((gs % 120) + 1),
  'https://company-' || gs || '.jobfinder.local',
  'seed.employer.' || gs || '@jobfinder.local',
  '$2a$10$0FYFBrRIdbTfE0VvKD60He6Fy/ASahx8K68nkf2TTaR8vLa28FUze',
  loc.ids[1 + ((gs * 3 - 1) % loc.cnt)]
FROM generate_series(1, 140) AS gs
CROSS JOIN (
  SELECT array_agg(id ORDER BY id) AS ids, COUNT(*)::int AS cnt
  FROM jobfinder.locations
) AS loc
WHERE loc.cnt > 0
ON CONFLICT (email) DO NOTHING;

INSERT INTO jobfinder.vacancies (
  employer_id,
  location_id,
  salary,
  payment_frequency,
  work_experience,
  work_format,
  description,
  publication_date,
  employment_type,
  address
)
SELECT
  emp.ids[1 + ((gs - 1) % emp.cnt)],
  loc.ids[1 + ((gs * 5 - 1) % loc.cnt)],
  (45000 + (gs % 40) * 3500)::numeric,
  (ARRAY['MONTHLY', 'WEEKLY', 'HOURLY', 'PROJECT'])[1 + ((gs - 1) % 4)]::jobfinder.payment_frequency,
  (ARRAY['Без опыта', '1-3 года', '3-6 лет', '6+ лет'])[1 + ((gs - 1) % 4)],
  (ARRAY['REMOTE', 'OFFICE', 'HYBRID'])[1 + ((gs - 1) % 3)]::jobfinder.work_format,
  'Вакансия #' || gs || ': масштабное тестовое наполнение',
  now() - make_interval(days => (gs % 120), hours => (gs % 24)),
  (ARRAY['FULL_TIME', 'PART_TIME', 'FREELANCE'])[1 + ((gs - 1) % 3)]::jobfinder.employment_type,
  'Офис ' || ((gs % 80) + 1)
FROM generate_series(1, 1800) AS gs
CROSS JOIN (
  SELECT array_agg(id ORDER BY id) AS ids, COUNT(*)::int AS cnt
  FROM jobfinder.employers
) AS emp
CROSS JOIN (
  SELECT array_agg(id ORDER BY id) AS ids, COUNT(*)::int AS cnt
  FROM jobfinder.locations
) AS loc
WHERE emp.cnt > 0 AND loc.cnt > 0;

INSERT INTO jobfinder.resumes (applicant_id, category_id, description, creation_date)
SELECT
  app.ids[1 + ((gs - 1) % app.cnt)],
  cat.ids[1 + ((gs * 7 - 1) % cat.cnt)],
  'Резюме #' || gs || ': тестовый профиль соискателя',
  now() - make_interval(days => (gs % 365))
FROM generate_series(1, 2200) AS gs
CROSS JOIN (
  SELECT array_agg(id ORDER BY id) AS ids, COUNT(*)::int AS cnt
  FROM jobfinder.applicants
) AS app
CROSS JOIN (
  SELECT array_agg(id ORDER BY id) AS ids, COUNT(*)::int AS cnt
  FROM jobfinder.categories
) AS cat
WHERE app.cnt > 0 AND cat.cnt > 0;

INSERT INTO jobfinder.educations (
  resume_id,
  institution,
  faculty,
  department,
  graduation_year,
  degree
)
SELECT
  r.id,
  'Университет ' || ((r.id % 30) + 1),
  (ARRAY['Информатика', 'Экономика', 'Управление', 'Математика'])[1 + (r.id % 4)],
  (ARRAY['Прикладные системы', 'Аналитика', 'Менеджмент', 'Инженерия'])[1 + (r.id % 4)],
  2005 + (r.id % 20),
  (ARRAY['BACHELOR', 'MASTER', 'SPECIALIST', 'PHD'])[1 + (r.id % 4)]::jobfinder.education_degree
FROM jobfinder.resumes AS r
WHERE r.description LIKE 'Резюме #%'
  AND NOT EXISTS (
    SELECT 1
    FROM jobfinder.educations e
    WHERE e.resume_id = r.id
      AND e.institution = 'Университет ' || ((r.id % 30) + 1)
  );

INSERT INTO jobfinder.experiences (
  resume_id,
  position,
  start_date,
  end_date,
  company_name,
  description
)
SELECT
  r.id,
  (ARRAY['Junior Developer', 'Data Analyst', 'QA Engineer', 'DevOps Engineer'])[1 + (r.id % 4)],
  (CURRENT_DATE - make_interval(years => 6, days => (r.id % 700)::int))::date,
  CASE WHEN (r.id % 5) = 0 THEN NULL ELSE (CURRENT_DATE - make_interval(days => (r.id % 200)::int))::date END,
  'Компания-работодатель ' || ((r.id % 90) + 1),
  'Опыт работы по тестовому профилю #' || r.id
FROM jobfinder.resumes AS r
WHERE r.description LIKE 'Резюме #%'
  AND NOT EXISTS (
    SELECT 1
    FROM jobfinder.experiences e
    WHERE e.resume_id = r.id
      AND e.company_name = 'Компания-работодатель ' || ((r.id % 90) + 1)
  );

INSERT INTO jobfinder.resume_skills (resume_id, skill_id)
SELECT
  r.id,
  sk.ids[1 + ((r.id + shift_idx) % sk.cnt)]
FROM jobfinder.resumes AS r
CROSS JOIN generate_series(0, 2) AS shift_idx
CROSS JOIN (
  SELECT array_agg(id ORDER BY id) AS ids, COUNT(*)::int AS cnt
  FROM jobfinder.skills
) AS sk
WHERE r.description LIKE 'Резюме #%'
  AND sk.cnt > 0
ON CONFLICT DO NOTHING;

INSERT INTO jobfinder.vacancy_skills (vacancy_id, skill_id)
SELECT
  v.id,
  sk.ids[1 + ((v.id + shift_idx) % sk.cnt)]
FROM jobfinder.vacancies AS v
CROSS JOIN generate_series(0, 2) AS shift_idx
CROSS JOIN (
  SELECT array_agg(id ORDER BY id) AS ids, COUNT(*)::int AS cnt
  FROM jobfinder.skills
) AS sk
WHERE v.description LIKE 'Вакансия #%'
  AND sk.cnt > 0
ON CONFLICT DO NOTHING;

INSERT INTO jobfinder.language_resume (language_id, resume_id)
SELECT
  lang.ids[1 + ((r.id + shift_idx) % lang.cnt)],
  r.id
FROM jobfinder.resumes AS r
CROSS JOIN generate_series(0, 1) AS shift_idx
CROSS JOIN (
  SELECT array_agg(id ORDER BY id) AS ids, COUNT(*)::int AS cnt
  FROM jobfinder.languages
) AS lang
WHERE r.description LIKE 'Резюме #%'
  AND lang.cnt > 0
ON CONFLICT DO NOTHING;

INSERT INTO jobfinder.language_vacancy (language_id, vacancy_id)
SELECT
  lang.ids[1 + ((v.id + shift_idx) % lang.cnt)],
  v.id
FROM jobfinder.vacancies AS v
CROSS JOIN generate_series(0, 1) AS shift_idx
CROSS JOIN (
  SELECT array_agg(id ORDER BY id) AS ids, COUNT(*)::int AS cnt
  FROM jobfinder.languages
) AS lang
WHERE v.description LIKE 'Вакансия #%'
  AND lang.cnt > 0
ON CONFLICT DO NOTHING;

INSERT INTO jobfinder.applications (vacancy_id, resume_id, date, status)
SELECT
  vac.ids[1 + ((gs - 1) % vac.cnt)],
  res.ids[1 + ((gs * 9 - 1) % res.cnt)],
  now() - make_interval(days => (gs % 90), hours => (gs % 24), mins => (gs % 59)),
  (ARRAY['NEW', 'INVITATION', 'REJECTION'])[1 + ((gs - 1) % 3)]::jobfinder.application_status
FROM generate_series(1, 7000) AS gs
CROSS JOIN (
  SELECT array_agg(id ORDER BY id) AS ids, COUNT(*)::int AS cnt
  FROM jobfinder.vacancies
  WHERE description LIKE 'Вакансия #%'
) AS vac
CROSS JOIN (
  SELECT array_agg(id ORDER BY id) AS ids, COUNT(*)::int AS cnt
  FROM jobfinder.resumes
  WHERE description LIKE 'Резюме #%'
) AS res
WHERE vac.cnt > 0 AND res.cnt > 0;

