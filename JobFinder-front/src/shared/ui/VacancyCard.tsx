import { Link } from "react-router-dom";
import { Vacancy } from "../types/vacancy";
import {
  formatDate,
  formatEmploymentType,
  formatJobFormat,
  formatSalary,
  toHumanTitle,
} from "../lib/format";
import { Icon } from "./Icon";

export function VacancyCard({ vacancy }: { vacancy: Vacancy }) {
  return (
    <article className="card job-card">
      <div className="job-card-top">
        <span className="job-mark">
          <Icon name="briefcase" size={24} />
        </span>
        <span className="eyebrow">
          {vacancy.location?.city || "Вакансия"}
          <span className="meta-dot">·</span>
          {formatDate(vacancy.publicationDate)}
        </span>
        <span className="job-arrow">
          <Icon name="arrow" />
        </span>
      </div>
      <h3>
        <Link to={`/vacancies/${vacancy.id}`}>
          {toHumanTitle(vacancy.description, "Вакансия")}
        </Link>
      </h3>
      <p className="job-salary">{formatSalary(vacancy)}</p>
      <div className="job-tags">
        <span>{formatJobFormat(vacancy.jobFormat)}</span>
        <span>{formatEmploymentType(vacancy.employmentType)}</span>
        {vacancy.status === "INACTIVE" && <span>Неактивна</span>}
      </div>
      <div className="job-card-bottom">
        <span>
          <Icon name="pin" size={15} />
          {vacancy.location?.city || "Локация не указана"}
        </span>
        <Link to={`/vacancies/${vacancy.id}`} className="text-link">
          Подробнее <Icon name="arrow" size={16} />
        </Link>
      </div>
    </article>
  );
}
