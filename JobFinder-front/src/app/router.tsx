import { Navigate, Route, Routes } from 'react-router-dom';
import { RequireAuth } from './guards/RequireAuth';
import { RequireRole } from './guards/RequireRole';
import { ApplicantApplicationsPage } from '../pages/applicant/ApplicantApplicationsPage';
import { ApplicantProfilePage } from '../pages/applicant/ApplicantProfilePage';
import { ApplicantResumesPage } from '../pages/applicant/ApplicantResumesPage';
import { ApplyToVacancyPage } from '../pages/applicant/ApplyToVacancyPage';
import { CreateResumePage } from '../pages/applicant/CreateResumePage';
import { EditResumePage } from '../pages/applicant/EditResumePage';
import { CreateVacancyPage } from '../pages/employer/CreateVacancyPage';
import { EditVacancyPage } from '../pages/employer/EditVacancyPage';
import { EmployerProfilePage } from '../pages/employer/EmployerProfilePage';
import { ResumeSearchPage } from '../pages/employer/ResumeSearchPage';
import { EmployerVacanciesPage } from '../pages/employer/EmployerVacanciesPage';
import { VacancyApplicationsPage } from '../pages/employer/VacancyApplicationsPage';
import { VacancyMatchingPage } from '../pages/employer/VacancyMatchingPage';
import { LoginPage } from '../pages/public/LoginPage';
import { RegisterPage } from '../pages/public/RegisterPage';
import { VacancyDetailsPage } from '../pages/public/VacancyDetailsPage';
import { VacancySearchPage } from '../pages/public/VacancySearchPage';
import { ForbiddenPage } from '../pages/system/ForbiddenPage';
import { NotFoundPage } from '../pages/system/NotFoundPage';

export function AppRouter() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/vacancies/search" replace />} />
      <Route path="/vacancies/search" element={<VacancySearchPage />} />
      <Route path="/vacancies/:id" element={<VacancyDetailsPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/register/applicant" element={<Navigate to="/register" replace />} />
      <Route path="/register/employer" element={<Navigate to="/register" replace />} />
      <Route path="/forbidden" element={<ForbiddenPage />} />

      <Route element={<RequireAuth />}>
        <Route element={<RequireRole role="applicant" />}>
          <Route path="/me/applicant" element={<ApplicantProfilePage />} />
          <Route path="/me/profile" element={<Navigate to="/me/applicant" replace />} />
          <Route path="/me/resumes" element={<ApplicantResumesPage />} />
          <Route path="/me/resumes/new" element={<CreateResumePage />} />
          <Route path="/me/resumes/:id/edit" element={<EditResumePage />} />
          <Route path="/me/applications" element={<ApplicantApplicationsPage />} />
          <Route path="/vacancies/:id/apply" element={<ApplyToVacancyPage />} />
        </Route>
        <Route element={<RequireRole role="employer" />}>
          <Route path="/me/employer" element={<EmployerProfilePage />} />
          <Route path="/me/employer/profile" element={<Navigate to="/me/employer" replace />} />
          <Route path="/me/vacancies" element={<EmployerVacanciesPage />} />
          <Route path="/me/resumes/search" element={<ResumeSearchPage />} />
          <Route path="/me/vacancies/new" element={<CreateVacancyPage />} />
          <Route path="/me/vacancies/:id/edit" element={<EditVacancyPage />} />
          <Route path="/me/vacancies/:id/applications" element={<VacancyApplicationsPage />} />
          <Route path="/me/vacancies/:id/matching" element={<VacancyMatchingPage />} />
        </Route>
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}

