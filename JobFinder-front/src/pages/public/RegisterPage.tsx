import { Navigate, Link, useSearchParams } from "react-router-dom";
import { useSession } from "../../shared/session/sessionStore";
import { Icon } from "../../shared/ui/Icon";
import { RegisterApplicantForm } from "../../features/auth/RegisterApplicantForm";
import { RegisterEmployerForm } from "../../features/auth/RegisterEmployerForm";

export function RegisterPage() {
  const session = useSession();
  const [params, setParams] = useSearchParams();
  const role = params.get("role") === "employer" ? "employer" : "applicant";
  if (session) return <Navigate to="/" replace />;
  function chooseRole(value: string) {
    const next = new URLSearchParams(params);
    next.set("role", value);
    setParams(next, { replace: true });
  }
  return (
    <main className="page auth-page">
      <aside className="auth-story">
        <span className="eyebrow">ВАШ СЛЕДУЮЩИЙ ШАГ</span>
        <h1>
          Новая глава
          <br />
          начинается здесь.
        </h1>
        <p>
          {role === "applicant"
            ? "Найдите работу, которая подходит вашему опыту и вашему ритму."
            : "Соберите команду, с которой захочется двигаться вперёд."}
        </p>
        <span className="auth-flower" aria-hidden="true">
          ✳
        </span>
        <span className="auth-story-bottom">
          <Icon name="spark" /> Место для больших планов.
        </span>
      </aside>
      <section className="auth-panel">
        <span className="eyebrow">JOBFINDER</span>
        <h2>Создать аккаунт</h2>
        <p className="auth-intro">
          {role === "applicant"
            ? "Аккаунт соискателя — для резюме и откликов."
            : "Аккаунт работодателя — для вакансий и подбора."}
        </p>
        <div className="role-switch" role="group" aria-label="Тип аккаунта">
          <button
            type="button"
            aria-pressed={role === "applicant"}
            onClick={() => chooseRole("applicant")}
          >
            Ищу работу
          </button>
          <button
            type="button"
            aria-pressed={role === "employer"}
            onClick={() => chooseRole("employer")}
          >
            Ищу сотрудников
          </button>
        </div>
        {role === "applicant" ? (
          <RegisterApplicantForm key={role} />
        ) : (
          <RegisterEmployerForm key={role} />
        )}
        <p className="auth-footer">
          Уже с нами?{" "}
          <Link to={`/login?role=${role}`}>
            Войти <span aria-hidden="true">↗</span>
          </Link>
        </p>
      </section>
    </main>
  );
}
