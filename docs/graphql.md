# GraphQL API

[Схема](schema.graphqls) описывает дополнительный интерфейс JobFinder по адресу
`POST /api/v2`, альтернативный [REST API](swagger.yml) по адресам `/api/v2/...`.
Сервер загружает эту схему при запуске; CLI использует GraphQL. Оба интерфейса
используют общие сервисы, репозитории и данные. Веб-клиент продолжает работать через `/api/v1`.

GraphQL позволяет выбрать нужные поля и связанные данные за один запрос: например,
отклик, его вакансию, публичное название компании и резюме. REST остаётся доступен
для интеграций с фиксированными ресурсами. Для текущих сценариев достаточно Query
и Mutation; подписки и WebSocket не требуются.

## HTTP и ошибки

Клиент отправляет `Content-Type: application/json` и
`Accept: application/graphql-response+json`. Тело — один объект с `query`,
`operationName` и необязательным объектом `variables`; имя операции обязательно.
Массив запросов и GET в этом интерфейсе не поддерживаются. Для защищённых полей
передаётся `Authorization: Bearer <accessToken>`; токены не помещаются в URL.
Подписанный claim `token_use` разделяет access/refresh: refresh-токен не принимается
как Bearer в v2, а access-токен нельзя обменять через refreshTokens. Старые токены
без этого claim сохраняют совместимость с REST v1; для v2 потребуется новый вход.
Refresh-токены дополнительно проверяются по Redis и заменяются при обмене.

Пример тела запроса:

```json
{
  "query": "mutation DecideApplication($id: ID!, $status: ApplicationStatus!) { updateApplicationStatus(id: $id, status: $status) { id applicationStatus vacancyId resumeId } }",
  "operationName": "DecideApplication",
  "variables": { "id": "40", "status": "INVITATION" }
}
```

| Ситуация | Ответ |
| --- | --- |
| Успешное исполнение | HTTP 200, `data` с выбранными полями |
| Ошибка отдельного поля при исполнении | HTTP 200, `data` и `errors`; ошибочное поле становится `null` |
| Невалидный JSON, документ, имя операции или переменные | HTTP 400, `errors`, без `data` |
| Передан невалидный/истёкший Bearer-токен до исполнения | HTTP 401, `WWW-Authenticate: Bearer`, без исполнения |
| Неподдерживаемый метод / тип тела / Accept | HTTP 405 (`Allow: POST`) / 415 / 406 |
| Сервис недоступен до начала исполнения | HTTP 503 |

Ошибки исполнения содержат `message`, `path` и `extensions.code`:
`BAD_USER_INPUT`, `UNAUTHENTICATED`, `FORBIDDEN`, `NOT_FOUND`, `CONFLICT`,
`SERVICE_UNAVAILABLE` или `INTERNAL_SERVER_ERROR`. Для синтаксической ошибки
используется `GRAPHQL_PARSE_FAILED`, для ошибки документа/переменных —
`GRAPHQL_VALIDATION_FAILED`. Стектрейсы, пароли и детали внутренних исключений не
выдаются. Корневые поля nullable: ошибка одного поля не обнуляет независимые
результаты. Ошибка вложенного non-null поля поднимается к ближайшему nullable полю
по правилам GraphQL. Перед мутацией полностью проверяются права и входные данные.

Пример запроса публичной вакансии и чужого приватного профиля с токеном соискателя:

```graphql
query PartialAccess {
  vacancy(id: "10") { id employer { name } }
  employer(id: "2") { id email }
}
```

Ожидаемый частичный результат:

```json
{
  "data": {
    "vacancy": { "id": "10", "employer": { "name": "Example" } },
    "employer": null
  },
  "errors": [{
    "message": "Нет доступа к профилю работодателя",
    "path": ["employer"],
    "extensions": { "code": "FORBIDDEN" }
  }]
}
```

Ответы этого endpoint используют `Cache-Control: no-store`: один запрос может
содержать и публичные данные, и профиль, и токены. Корневые мутации исполняются
последовательно, но несколько мутаций в одном документе не образуют общую
транзакцию. Автоматически повторять создающие мутации после сетевой ошибки нельзя:
повторная регистрация/отклик конфликтует, повторное создание вакансии может создать
вторую запись. Повтор той же смены статуса не меняет результат. Выход клиента
удаляет локальную пару токенов; отдельная операция отзыва токена этим контрактом не обещана.

## Данные и права доступа

| Возможность | Поля схемы и доступ |
| --- | --- |
| Каталоги и поиск вакансий | `categories`, `skills`, `languages`, `location`, `locations`, `vacancy`, `vacancies`: публично |
| Регистрация и токены | `registerApplicant`, `registerEmployer`, `login`, `refreshTokens`: публично; учётные данные/refresh-токен проверяются |
| Профили | `applicant`, `employer`, `replaceApplicant`, `replaceEmployer`, `deleteApplicant`, `deleteEmployer`: только владелец |
| Вакансии | `createVacancy`, `replaceVacancy`, `deleteVacancy`: работодатель-владелец |
| Сохранённые версии | `vacancyDrafts`, `createVacancyDraft`, `restoreVacancyDraft`, `deleteVacancyDraft`: работодатель-владелец вакансии |
| Резюме | `resume`: владелец или работодатель; `resumes`: работодатель или соискатель только для своих резюме; создание/замена/удаление — соискатель-владелец |
| Отклики | `myApplications`: текущий соискатель; `vacancyApplications`: владелец вакансии; `application`: автор или владелец вакансии |
| Изменение отклика | `createApplication`: владелец резюме; `replaceApplication`, `updateApplicationStatus`: владелец вакансии; `deleteApplication`: автор |
| Подбор | `matchingCandidates`: владелец вакансии |

Регистрация возвращает только что созданный профиль без Bearer-токена; это явное
исключение для `registerApplicant`/`registerEmployer`, не открывающее чтение чужих профилей.
При поиске резюме соискателем фильтр applicantId принудительно берётся из токена;
если передан чужой ID, возвращается `FORBIDDEN`. Проверяется владелец каждого результата.
Это уточняет грубое требование Bearer в REST-контракте и не разрешает обойти
правила `resume(id)` через поиск.

Вложенные поля проверяют те же права независимо от пути, alias или fragment.
`Vacancy.employer` возвращает только `EmployerSummary` (id, название, описание,
сайт), а приватный `employer(id)` — отдельный защищённый профиль. Нельзя получить
email через публичную вакансию. `Application.resume`/`vacancy` и `MatchResult.resume`
повторно проверяют доступ к связанному объекту. Схема не содержит password/hash
в выходных типах; `TokenPair` доступен только в результате операций авторизации.

ID сериализуются строками, например `"9223372036854775807"`: это положительные
идентификаторы Java Long, сервер отклоняет ноль, отрицательные и выходящие за диапазон
значения. `Long` для счётчиков также передаётся строкой, но допускает ноль.
`Decimal` — неотрицательная десятичная строка без экспоненты, чтобы не терять точность
зарплаты. Date/DateTime соответствуют календарной дате и RFC 3339 с часовым поясом;
Email и URL проверяются по форматам, URL сайта допускает HTTP(S).

Пагинация: `limit` от 1 до 100 (по умолчанию 20), `offset >= 0`; у поиска локаций
максимум 50, у подбора кандидатов по умолчанию 10 и максимум 100. `PageInput: null`
равнозначен отсутствию аргумента. `page = floor(offset / limit)`, `size = limit`,
`totalPages = ceil(totalElements / limit)`; пустая коллекция — `[]`. При равенстве
основного поля сортировки используется id в том же направлении. Отклики сортируются
по applicationDate и id по убыванию. Фильтры объединяются через AND, все переданные
skillIds/languageIds должны присутствовать; отсутствующие/null фильтры и пустые
массивы не ограничивают поиск. Нижняя граница диапазона не может превышать верхнюю.

`replace*` заменяет редактируемые данные; пропущенные nullable поля очищаются,
кроме пароля профиля: пропуск или null сохраняет старый пароль. Переданные списки
заменяются целиком, `[]` очищает связи. При создании/замене вакансии пропущенные списки
становятся `[]`, status — `ACTIVE`. При полной замене резюме списки обязательны.
Nullable `description` при замене резюме — явное отличие входного представления
GraphQL: null/пропуск очищает значение, тогда как текущая схема REST PUT требует строку.
Владельца вакансии/резюме нельзя изменить. `updateApplicationStatus` меняет только
статус и сохраняет идентификаторы связей и дату. Новый отклик всегда `NEW`;
в `replaceApplication` vacancyId/resumeId обязаны совпадать с текущими.
Сохранённая версия принадлежит одной вакансии; восстановление делает её `ACTIVE`.

Проверки длины имени (2–100), пароля (не менее 8 при регистрации/смене), непустых
обязательных строк, границ пагинации/ID и владения ресурсами выполняются бизнес-слоем
и resolvers. Одна лишь валидация GraphQL-типов эти правила не обеспечивает.
В записи об образовании обязательны степень, учебное заведение, факультет, кафедра
и год выпуска; в опыте работы — должность, компания и дата начала. Год выпуска
допускается от 1900 до текущего года + 10, дата окончания не может предшествовать началу.
Глубина запроса ограничивается 8 уровнями, суммарное количество выбранных полей
с учётом раскрытия fragments — 200; превышение отклоняется до исполнения с HTTP 400.
Ограничения проверяются сервером до вызова resolvers.

## Примеры операций

Каждая операция именованная; из документа выполняется только выбранная
`operationName`. Примеры независимы: создание и удаление не исполняются одним вызовом.
Для пользовательских и работодательских примеров нужны соответствующие токены.
В JSON ниже приведены `variables` по имени операции, остальные операции обходятся
без переменных. Значения и пароли фиктивные.

```graphql
query Catalogue($filter: VacancyFilter, $page: PageInput) {
  vacancies(filter: $filter, page: $page) {
    items { id description salary employer { id name websiteLink } location { city } skills { name } }
    page size totalElements totalPages
  }
  vacancy(id: "10") { id status languages { name proficiencyLevel } }
  categories { id name }
  skills { id name }
  languages { id name proficiencyLevel }
  location(id: "1") { id city country }
  locations(q: "Москва", limit: 10) { id city }
}

query ApplicantWorkspace {
  applicant(id: "1") { id name email address location { city } }
  resumes(filter: {applicantId: "1"}) { items { id description } totalElements }
  resume(id: "20") { id description educations { institutionName } jobExperiences { startDate endDate } }
  myApplications(page: {limit: 20, offset: 0}) { id applicationStatus vacancy { description employer { name } } }
  application(id: "40") { id applicationStatus resume { id category { name } } }
}

query EmployerWorkspace($filter: ResumeFilter) {
  employer(id: "2") { id name email description websiteLink }
  resumes(filter: $filter) { items { id description skills { name } } totalElements }
  vacancyApplications(vacancyId: "10") { id applicationStatus resume { description } }
  vacancyDrafts(vacancyId: "10") { id versionTimestamp }
  matchingCandidates(vacancyId: "10", limit: 10) { id score resume { id description skills { name } } }
}

mutation RegisterApplicant {
  registerApplicant(input: {name: "Анна Иванова", email: "anna@example.com", password: "ExamplePass123!", locationId: "1"}) { id name }
}
mutation RegisterEmployer {
  registerEmployer(input: {name: "Example", email: "hr@example.com", password: "ExamplePass123!", locationId: "1", websiteLink: "https://example.com", description: null}) { id name }
}
mutation Login($input: LoginInput!) {
  login(input: $input) { accessToken refreshToken }
}
mutation RefreshTokens {
  refreshTokens(input: {accountType: APPLICANT, refreshToken: "example-refresh-token"}) { accessToken refreshToken }
}
mutation ReplaceApplicant {
  replaceApplicant(id: "1", input: {name: "Анна Иванова", email: "anna@example.com", locationId: "1", address: null}) { id name address }
}
mutation DeleteApplicant { deleteApplicant(id: "1") { id } }
mutation ReplaceEmployer {
  replaceEmployer(id: "2", input: {name: "Example", email: "hr@example.com", locationId: "1", websiteLink: null}) { id name websiteLink }
}
mutation DeleteEmployer { deleteEmployer(id: "2") { id } }

mutation CreateVacancy($input: VacancyInput!) {
  createVacancy(input: $input) { id description status employer { name } }
}
mutation ReplaceVacancy($input: VacancyInput!) {
  replaceVacancy(id: "10", input: $input) { id description status }
}
mutation DeleteVacancy { deleteVacancy(id: "10") { id } }
mutation SaveVacancyDraft { createVacancyDraft(vacancyId: "10") { id vacancyId versionTimestamp } }
mutation RestoreVacancyDraft { restoreVacancyDraft(vacancyId: "10", draftId: "30") { id status description } }
mutation DeleteVacancyDraft { deleteVacancyDraft(vacancyId: "10", draftId: "30") { id } }

mutation CreateResume($input: ResumeCreateInput!) {
  createResume(input: $input) { id date jobExperiences { startDate endDate } }
}
mutation ReplaceResume {
  replaceResume(id: "20", input: {applicantId: "1", categoryId: "1", description: "Java разработчик", skillIds: ["1"], languageIds: [], educations: [], jobExperiences: []}) { id description }
}
mutation DeleteResume { deleteResume(id: "20") { id } }

mutation CreateApplication {
  createApplication(input: {vacancyId: "10", resumeId: "20"}) { id applicationStatus applicationDate }
}
mutation ReplaceApplication {
  replaceApplication(id: "40", input: {vacancyId: "10", resumeId: "20", applicationStatus: INVITATION}) { id applicationStatus }
}
mutation DecideApplication($id: ID!, $status: ApplicationStatus!) {
  updateApplicationStatus(id: $id, status: $status) { id applicationStatus vacancyId resumeId }
}
mutation WithdrawApplication { deleteApplication(id: "40") { id } }
```

<!-- graphql-variables -->
```json
{
  "Catalogue": {
    "filter": { "q": "Java", "salaryMin": "100000.00", "status": "ACTIVE", "publicationDateFrom": "2026-09-01T00:00:00Z" },
    "page": { "limit": 20, "offset": 0 }
  },
  "EmployerWorkspace": { "filter": { "skillIds": ["1"], "creationDateFrom": "2026-01-01T00:00:00Z" } },
  "Login": { "input": { "accountType": "APPLICANT", "email": "anna@example.com", "password": "ExamplePass123!" } },
  "CreateVacancy": {
    "input": { "employerId": "2", "locationId": "1", "salary": "150000.00", "paymentFrequency": "MONTHLY", "experience": "От одного года", "jobFormat": "REMOTE", "employmentType": "FULL_TIME", "description": "Java разработчик", "address": "Москва", "skillIds": ["1"] }
  },
  "ReplaceVacancy": {
    "input": { "employerId": "2", "locationId": "1", "salary": "160000.00", "paymentFrequency": "MONTHLY", "experience": "От одного года", "jobFormat": "REMOTE", "employmentType": "FULL_TIME", "description": "Java разработчик", "address": "Москва", "skillIds": ["1"], "status": "ACTIVE" }
  },
  "CreateResume": {
    "input": { "applicantId": "1", "categoryId": "1", "description": "Java разработчик", "skillIds": ["1"], "jobExperiences": [{ "position": "Разработчик", "companyName": "Example", "startDate": "2025-06-01", "endDate": null }] }
  },
  "DecideApplication": { "id": "40", "status": "INVITATION" }
}
```

Пример успешного ответа `DecideApplication`:

```json
{
  "data": {
    "updateApplicationStatus": {
      "id": "40", "applicationStatus": "INVITATION", "vacancyId": "10", "resumeId": "20"
    }
  }
}
```

Проверка схемы, операций и переменных: [testing.md](testing.md#проверка-контракта-graphql).
Основы формата: [GraphQL Specification](https://spec.graphql.org/September2025/)
и [GraphQL over HTTP](https://http-spec.graphql.org/draft/); правила транспорта выше
фиксируют выбранное поведение JobFinder.
