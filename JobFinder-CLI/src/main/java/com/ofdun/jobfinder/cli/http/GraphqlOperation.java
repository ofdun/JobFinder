package com.ofdun.jobfinder.cli.http;

/** Named operations against the shared GraphQL schema; each selects one result. */
public enum GraphqlOperation {
    LOGIN(
            false,
            "mutation LOGIN($input: LoginInput!) { result: login(input: $input) { accessToken"
                    + " refreshToken } }"),
    REFRESH(
            false,
            "mutation REFRESH($input: RefreshInput!) { result: refreshTokens(input: $input) {"
                    + " accessToken refreshToken } }"),
    CREATE_APPLICANT(
            false,
            "mutation CREATE_APPLICANT($input: ApplicantCreateInput!) { result:"
                    + " registerApplicant(input: $input) { id } }"),
    GET_APPLICANT(
            true,
            "query GET_APPLICANT($id: ID!) { result: applicant(id: $id) { id name email address"
                    + " phoneNumber location { id city country } } }"),
    REPLACE_APPLICANT(
            true,
            "mutation REPLACE_APPLICANT($id: ID!, $input: ApplicantReplaceInput!) { result:"
                + " replaceApplicant(id: $id, input: $input) { id name email address phoneNumber"
                + " location { id city country } } }"),
    DELETE_APPLICANT(
            true,
            "mutation DELETE_APPLICANT($id: ID!) { result: deleteApplicant(id: $id) { id } }"),
    CREATE_EMPLOYER(
            false,
            "mutation CREATE_EMPLOYER($input: EmployerCreateInput!) { result:"
                    + " registerEmployer(input: $input) { id } }"),
    GET_EMPLOYER(
            true,
            "query GET_EMPLOYER($id: ID!) { result: employer(id: $id) { id name email description"
                    + " address websiteLink location { id city country } } }"),
    REPLACE_EMPLOYER(
            true,
            "mutation REPLACE_EMPLOYER($id: ID!, $input: EmployerReplaceInput!) { result:"
                    + " replaceEmployer(id: $id, input: $input) { id name email description address"
                    + " websiteLink location { id city country } } }"),
    DELETE_EMPLOYER(
            true, "mutation DELETE_EMPLOYER($id: ID!) { result: deleteEmployer(id: $id) { id } }"),
    CREATE_VACANCY(
            true,
            "mutation CREATE_VACANCY($input: VacancyInput!) { result: createVacancy(input: $input)"
                    + " { id } }"),
    GET_VACANCY(
            false,
            "query GET_VACANCY($id: ID!) { result: vacancy(id: $id) { id employerId location { id"
                + " city country } salary skills { id name } languages { id name proficiencyLevel }"
                + " paymentFrequency experience jobFormat employmentType description"
                + " publicationDate address status } }"),
    REPLACE_VACANCY(
            true,
            "mutation REPLACE_VACANCY($id: ID!, $input: VacancyInput!) { result: replaceVacancy(id:"
                + " $id, input: $input) { id employerId location { id city country } salary skills"
                + " { id name } languages { id name proficiencyLevel } paymentFrequency experience"
                + " jobFormat employmentType description publicationDate address status } }"),
    DELETE_VACANCY(
            true, "mutation DELETE_VACANCY($id: ID!) { result: deleteVacancy(id: $id) { id } }"),
    CREATE_RESUME(
            true,
            "mutation CREATE_RESUME($input: ResumeCreateInput!) { result: createResume(input:"
                    + " $input) { id } }"),
    GET_RESUME(
            true,
            "query GET_RESUME($id: ID!) { result: resume(id: $id) { id applicantId category { id"
                + " name } description skills { id name } educations { id resumeId educationDegree"
                + " institutionName faculty department yearOfGraduation } jobExperiences { id"
                + " resumeId position companyName description startDate endDate } languages { id"
                + " name proficiencyLevel } date } }"),
    REPLACE_RESUME(
            true,
            "mutation REPLACE_RESUME($id: ID!, $input: ResumeReplaceInput!) { result:"
                + " replaceResume(id: $id, input: $input) { id applicantId category { id name }"
                + " description skills { id name } educations { id resumeId educationDegree"
                + " institutionName faculty department yearOfGraduation } jobExperiences { id"
                + " resumeId position companyName description startDate endDate } languages { id"
                + " name proficiencyLevel } date } }"),
    DELETE_RESUME(
            true, "mutation DELETE_RESUME($id: ID!) { result: deleteResume(id: $id) { id } }"),
    SEARCH_VACANCIES(
            false,
            "query SEARCH_VACANCIES($filter: VacancyFilter, $page: PageInput, $sort: VacancySort,"
                + " $direction: SortDirection) { result: vacancies(filter: $filter, page: $page,"
                + " sort: $sort, direction: $direction) { items { id employerId location { id city"
                + " country } salary skills { id name } languages { id name proficiencyLevel }"
                + " paymentFrequency experience jobFormat employmentType description"
                + " publicationDate address status } page size totalElements totalPages } }"),
    SEARCH_RESUMES(
            true,
            "query SEARCH_RESUMES($filter: ResumeFilter, $page: PageInput) { result:"
                + " resumes(filter: $filter, page: $page) { items { id applicantId category { id"
                + " name } description skills { id name } educations { id resumeId educationDegree"
                + " institutionName faculty department yearOfGraduation } jobExperiences { id"
                + " resumeId position companyName description startDate endDate } languages { id"
                + " name proficiencyLevel } date } page size totalElements totalPages } }"),
    CREATE_APPLICATION(
            true,
            "mutation CREATE_APPLICATION($input: ApplicationCreateInput!) { result:"
                    + " createApplication(input: $input) { id } }"),
    GET_APPLICATION(
            true,
            "query GET_APPLICATION($id: ID!) { result: application(id: $id) { id vacancyId resumeId"
                    + " applicationDate applicationStatus } }"),
    UPDATE_APPLICATION_STATUS(
            true,
            "mutation UPDATE_APPLICATION_STATUS($id: ID!, $status: ApplicationStatus!) { result:"
                    + " updateApplicationStatus(id: $id, status: $status) { id vacancyId resumeId"
                    + " applicationDate applicationStatus } }"),
    DELETE_APPLICATION(
            true,
            "mutation DELETE_APPLICATION($id: ID!) { result: deleteApplication(id: $id) { id } }"),
    MY_APPLICATIONS(
            true,
            "query MY_APPLICATIONS($page: PageInput) { result: myApplications(page: $page) { id"
                    + " vacancyId resumeId applicationDate applicationStatus } }"),
    VACANCY_APPLICATIONS(
            true,
            "query VACANCY_APPLICATIONS($vacancyId: ID!, $page: PageInput) { result:"
                + " vacancyApplications(vacancyId: $vacancyId, page: $page) { id vacancyId resumeId"
                + " applicationDate applicationStatus } }"),
    MATCHING(
            true,
            "query MATCHING($vacancyId: ID!, $limit: Int) { result: matchingCandidates(vacancyId:"
                    + " $vacancyId, limit: $limit) { id score } }");

    private final boolean authenticationRequired;
    private final String document;

    GraphqlOperation(boolean authenticationRequired, String document) {
        this.authenticationRequired = authenticationRequired;
        this.document = document;
    }

    public boolean authenticationRequired() {
        return authenticationRequired;
    }

    public String document() {
        return document;
    }
}
