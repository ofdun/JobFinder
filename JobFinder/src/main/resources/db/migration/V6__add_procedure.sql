CREATE OR REPLACE PROCEDURE jobfinder.accept_one_applicant(vacancy_id_ BIGINT, accepted_application_id BIGINT)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE jobfinder.applications a
    SET status = 'REJECTION'
    WHERE vacancy_id_ = vacancy_id
      AND id != accepted_application_id;

    UPDATE jobfinder.applications
    SET status = 'INVITATION'
    WHERE id = accepted_application_id;
END;
$$;


