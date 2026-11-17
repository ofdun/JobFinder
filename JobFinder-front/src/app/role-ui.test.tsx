import { Route, Routes } from "react-router-dom";
import { VacancyDetailsPage } from "../pages/public/VacancyDetailsPage";
import { renderToStaticMarkup } from "react-dom/server";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { StaticRouter } from "react-router-dom/server";
import { describe, expect, it, vi } from "vitest";
import { AppShell } from "./AppShell";
import { HomePage } from "../pages/public/HomePage";

const state = vi.hoisted(() => ({
  role: null as "applicant" | "employer" | null,
}));
vi.mock("../shared/session/sessionStore", () => ({
  useSession: () => (state.role ? { role: state.role, userId: 1 } : null),
  clearSession: vi.fn(),
}));

function render(role: typeof state.role) {
  state.role = role;
  return renderToStaticMarkup(
    <QueryClientProvider
      client={
        new QueryClient({ defaultOptions: { queries: { retry: false } } })
      }
    >
      <StaticRouter location="/">
        <AppShell />
        <HomePage />
      </StaticRouter>
    </QueryClientProvider>,
  );
}

describe("role-specific user interface", () => {
  it("only shows résumé and application actions to an applicant", () => {
    const html = render("applicant");
    expect(html).toContain('href="/me/resumes/new"');
    expect(html).toContain('href="/me/applications"');
    expect(html).not.toContain('href="/me/vacancies');
    expect(html).not.toContain('href="/me/resumes/search"');
    expect(html).not.toContain("role=employer");
    expect(html).not.toContain("Ищу сотрудников");
    expect(html).not.toContain("Создать вакансию");
  });

  it("only shows recruiting actions to an employer", () => {
    const html = render("employer");
    expect(html).toContain('href="/me/vacancies/new"');
    expect(html).toContain('href="/me/resumes/search"');
    expect(html).not.toContain('href="/me/resumes/new"');
    expect(html).not.toContain('href="/me/applications"');
    expect(html).not.toContain("role=applicant");
    expect(html).not.toContain("Создать резюме");
  });

  it("offers both account types only on the public landing page", () => {
    const html = render(null);
    expect(html).toContain('href="/register?role=applicant"');
    expect(html).toContain('href="/register?role=employer"');
    expect(html).not.toContain('href="/me/');
    expect(html).not.toContain('href="/management"');
    expect(html).not.toContain('href="/admin');
  });
});

function renderDetails(role: typeof state.role) {
  state.role = role;
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false, staleTime: Infinity } },
  });
  client.setQueryData(["vacancy", 41], {
    id: 41,
    description: "Java engineer",
    jobFormat: "REMOTE",
    employmentType: "FULL_TIME",
    status: "ACTIVE",
    experience: "2 years",
    address: "Moscow",
    salary: 150000,
    paymentFrequency: "MONTHLY",
    publicationDate: "2026-09-20T10:00:00Z",
  });
  return renderToStaticMarkup(
    <QueryClientProvider client={client}>
      <StaticRouter location="/vacancies/41">
        <Routes>
          <Route path="/vacancies/:id" element={<VacancyDetailsPage />} />
        </Routes>
      </StaticRouter>
    </QueryClientProvider>,
  );
}

describe("vacancy application permissions in the UI", () => {
  it("never invites an employer to apply or log in again", () => {
    const html = renderDetails("employer");
    expect(html).not.toContain('href="/vacancies/41/apply"');
    expect(html).not.toContain("Войти, чтобы откликнуться");
  });
  it("allows an applicant to open the application form", () => {
    expect(renderDetails("applicant")).toContain('href="/vacancies/41/apply"');
  });
  it("asks a guest to log in before applying", () => {
    expect(renderDetails(null)).toContain("Войти, чтобы откликнуться");
  });
});
