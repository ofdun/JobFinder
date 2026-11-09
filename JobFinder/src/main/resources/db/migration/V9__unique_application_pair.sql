-- Concurrent submissions for one vacancy and resume must conflict atomically.
-- Existing duplicates are intentionally not deleted; migration fails for manual review.
ALTER TABLE jobfinder.applications
    ADD CONSTRAINT applications_vacancy_resume_unique UNIQUE (vacancy_id, resume_id);
