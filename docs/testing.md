# Тестирование JobFinder

## Микробенчмарки

Из корня репозитория: `python3 benchmarks/run.py`. [Отдельный набор](../benchmarks/README.md) сравнивает циклы и Java Streams на трёх операциях с вакансиями в памяти. Результат содержит сырые времена каждой операции, warmup/teardown counters, overhead, p50/p75/p90/p95/p99 и графики. Бенчмарки запускаются в собственных Docker-контейнерах с одним CPU, не обращаются к приложению/БД и не входят в `test`, `testAll` или этапы CI проверки корректности.

## Запуск

Требуется JDK 25. Команды выполняются из каталога `JobFinder`. Версия Gradle закреплена в [Gradle Wrapper](../JobFinder/gradle/wrapper/gradle-wrapper.properties); файлы `gradlew`, `gradlew.bat`, JAR и properties wrapper больше не исключаются из Git.

```sh
cd JobFinder
./gradlew test coverageSummary
./gradlew check
```

`test` запускает только `*Test`: без запуска приложения, HTTP-сервера, Docker и работающих PostgreSQL, MongoDB, Redis, Qdrant или Ollama. Основные проверки являются unit-тестами; `SecurityConfigurationTest` дополнительно создаёт изолированный Spring-контекст только для конфигурации безопасности и выполняет реальную цепочку фильтров на mock servlet-запросах. `check` также выполняет проверку форматирования Spotless. Для Windows используется `gradlew.bat`.

Для явного имени доступен `./gradlew unitTest`: это alias для `test`, с теми же отчётами, покрытием и параметрами `-P`. Стандартный `test` сохранён для IDE и `check`. Фильтрация отдельного теста выполняется через `./gradlew test --tests '*BasicApplicantServiceTest'`, а не через alias `unitTest`.

После запуска доступны:

| Результат | Файл |
| --- | --- |
| HTML-отчёт о тестах | [build/reports/tests/test/index.html](../JobFinder/build/reports/tests/test/index.html) |
| JUnit XML по каждому классу | `JobFinder/build/test-results/test/TEST-*.xml` |
| HTML-покрытие с навигацией до исходных строк | [build/reports/jacoco/test/html/index.html](../JobFinder/build/reports/jacoco/test/html/index.html) |
| JaCoCo XML | [build/reports/jacoco/test/jacocoTestReport.xml](../JobFinder/build/reports/jacoco/test/jacocoTestReport.xml) |
| JaCoCo CSV | [build/reports/jacoco/test/jacocoTestReport.csv](../JobFinder/build/reports/jacoco/test/jacocoTestReport.csv) |

Отчёты создаются автоматически. `test` подключён к `jacocoTestReport`; `coverageSummary` выводит в терминал суммарные счётчики LINE и BRANCH из CSV JaCoCo. Расчёт покрытия выполняет JaCoCo 0.8.14, собственный инструмент измерения покрытия не используется. HTML-отчёт Gradle выбран как стандартный отчёт о выполнении тестов; Allure не требуется для его просмотра. Каталог `build` исключён из Git.

### Теги в HTML-отчёте

Открой HTML-отчёт → класс → конкретный тест → вкладку `data` → поле `tags`. Для параметризованного метода сначала выбери отдельный запуск. Например, `BasicVacancyServiceTest.searchVacancies_clampsPagination` показывает `boundary, equivalence`.

[TestTagsExtension](../JobFinder/src/test/java/com/ofdun/jobfinder/support/TestTagsExtension.java) перед каждым запускаемым тестом публикует `ExtensionContext.getTags()` через стандартный JUnit `publishReportEntry`. Gradle 9.4.0 сохраняет эти данные в HTML и в `<testcase><properties><property name="tags" .../></properties></testcase>` XML. Теги класса и метода объединяются, сортируются и не меняют имена тестов или фильтрацию `--tests`. Без тегов отображается `untagged`; у отключённых тестов или сценариев, не дошедших до `beforeEach`, запись не создаётся.

Расширение включено для всех Gradle test-задач, в том числе интеграционных. Уже созданные отчёты нужно обновить запуском соответствующего набора. Для unit-отчёта: `./gradlew test --rerun-tasks`. Проверено 15.09.2026 командой `./gradlew --offline check coverageSummary --rerun-tasks`: 621/621 passed, у всех 621 cases есть `tags`, Spotless пройден. Интеграционные тесты при этой проверке не запускались.

## Случайный порядок и работа без интернета

```sh
./gradlew test coverageSummary --rerun-tasks -PrandomOrder=true -PtestSeed=20260913
./gradlew test coverageSummary --offline --rerun-tasks -PrandomOrder=true -PtestSeed=42
```

Случайный порядок включается для классов и методов через `ClassOrderer.Random` и `MethodOrderer.Random`. Seed печатается перед запуском. Если `testSeed` не задан, используется текущее время; напечатанное значение позволяет повторить порядок. `--rerun-tasks` гарантирует новый прогон, даже если Gradle считает результаты актуальными.

Первый запуск требует доступа к репозиториям зависимостей для загрузки Gradle, библиотек и JaCoCo. После одного успешного запуска `test coverageSummary` все зависимости отчётов доступны в локальном Gradle cache. `--offline` запрещает Gradle загружать зависимости, но сам по себе не является сетевым firewall. Тесты не обращаются к внешним сервисам: зависимости репозиториев и клиентов заменены Mockito, а классические тесты выполняют локальные преобразования и криптографические операции.

Для дополнительной проверки на macOS можно запретить внешние соединения всему новому дереву процессов, сохранив loopback для связи Gradle с worker JVM:

```sh
JAVA_TOOL_OPTIONS=-Djava.net.preferIPv4Stack=true sandbox-exec -p '(version 1)(allow default)(deny network-outbound)(allow network-outbound (remote ip "localhost:*"))' ./gradlew --no-daemon --offline check coverageSummary --rerun-tasks
```

Параметр `preferIPv4Stack` обеспечивает работу локального соединения JVM с loopback-исключением sandbox. `--no-daemon` здесь исключает использование ранее запущенного daemon вне сетевого ограничения. Gradle может создать новый одноразовый daemon внутри ограничения. Это отдельная проверка окружения; обычный переносимый запуск остаётся `./gradlew test --offline`.

## Процессы и изоляция

По умолчанию `maxParallelForks = 1`, `forkEvery = 0`; все unit-тесты одного запуска выполняются в одной worker JVM. Параллельное выполнение JUnit отключено. Процессы Gradle launcher и daemon не являются дополнительными процессами выполнения тестов.

[TestProcessExtension](../JobFinder/src/test/java/com/ofdun/jobfinder/support/TestProcessExtension.java) записывает для каждого класса строку `TEST_PROCESS pid=... worker=... suite=...`. Расширение автоматически подключается через [ServiceLoader](../JobFinder/src/test/resources/META-INF/services/org.junit.jupiter.api.extension.Extension) и параметр JUnit в [build.gradle.kts](../JobFinder/build.gradle.kts). Эти строки сохраняются в секции `system-out` XML-отчёта.

```sh
./gradlew test --rerun-tasks -PshowTestProcesses=true
./gradlew test --rerun-tasks -PtestForks=2 -PshowTestProcesses=true
./gradlew test --rerun-tasks -PtestForkEvery=1 -PshowTestProcesses=true
```

`testForks` ограничивает количество одновременно работающих JVM; `testForkEvery=1` перезапускает JVM после каждого класса. Это не отдельный процесс на каждый тестовый метод. Значения доступны без изменения build-файла. JUnit создаёт экземпляр тестового класса для каждого метода, Mockito создаёт новые зависимости, а фабрики возвращают новые объекты. Изменяемые модели и даты между тестами не разделяются.

## Подготовка данных и структура теста

[ApplicantBuilder](../JobFinder/src/test/java/com/ofdun/jobfinder/support/ApplicantBuilder.java) реализует Data Builder: валидные значения по умолчанию и явные изменения через `withId`, `withName`, `withEmail`, `withLocationId`. Пример использования с недопустимыми идентификаторами: [BasicApplicantValidatorTest](../JobFinder/src/test/java/com/ofdun/jobfinder/features/applicant/domain/validator/BasicApplicantValidatorTest.java), методы `validateApplicantForUpdate_invalidIdSkipsStorage` и `validateApplicantForDelete_invalidIdSkipsStorage`.

[TestDataMother](../JobFinder/src/test/java/com/ofdun/jobfinder/support/TestDataMother.java) реализует Object Mother: `applicant()`, `employer()`, `application()`, `vacancy()`, `draft()`, `resume()`, `education()`, `experience()` и справочники. Каждый вызов создаёт свежую модель. Даты предметных объектов фиксированы; тесты JWT задают срок жизни с большим запасом или явно истёкшую дату, без ожидания таймера.

Тесты организованы как Arrange–Act–Assert с разделением блоков пустыми строками. Комментарии для маркировки блоков не используются. Например, в [PostgreSQLApplicantRepositoryTest](../JobFinder/src/test/java/com/ofdun/jobfinder/features/applicant/data/postgres/repository/PostgreSQLApplicantRepositoryTest.java), `createApplicant_whenValid_savesFields`:

1. Arrange: фабрика создаёт модель, настраивается ответ mock-хранилища.
2. Act: один вызов `repository.createApplicant(model)`.
3. Assert: проверяются возвращённый ID и поля переданного в хранилище объекта через `ArgumentCaptor`.

Для исключений единственный вызов проверяемой операции находится внутри `assertThrows`. Для JPA Specifications создание спецификации относится к Arrange, а её вычисление через `toPredicate` является Act. Защищённые `execute` обработчиков проверяются через публичный `handle`. Приватные методы напрямую не вызываются. `ReflectionTestUtils` в двух тестах PostgreSQL используется только для установки зависимости `EntityManager`, которую в приложении внедряет Spring.

## Техники выбора тестовых данных

Каждый тест определяется тегом метода; для ранее существовавших тестов задан тег класса `equivalence`. Теги класса и метода складываются, а не заменяют друг друга. Параметризованные проверки, объединяющие числовую границу и отдельный класс отсутствующих/некорректных значений, имеют оба тега `boundary` и `equivalence`.

| Тег | Техника и конкретные примеры |
| --- | --- |
| `equivalence` | Классы эквивалентности: запись существует / отсутствует, корректный / неверный пароль, успешное хранилище / ошибка. Примеры: [PostgreSQLApplicantRepositoryTest](../JobFinder/src/test/java/com/ofdun/jobfinder/features/applicant/data/postgres/repository/PostgreSQLApplicantRepositoryTest.java), `getApplicantById_whenPresent_mapsFields` / `getApplicantById_whenMissing_returnsEmpty`; [ApplicantAuthServiceTest](../JobFinder/src/test/java/com/ofdun/jobfinder/features/auth/domain/service/ApplicantAuthServiceTest.java), `login_whenValidCredentials_thenReturnTokenPair` / `login_whenPasswordInvalid_thenThrowsException`. |
| `boundary` | Числовые значения на границе допустимого диапазона и рядом с ней: ID `0` перед минимальным допустимым `1`, лимиты `0, 1, 100, 101`, смещение `-1, 0`, число `9223372036854775808` сразу за `Long.MAX_VALUE`. Пример: [BasicVacancyServiceTest](../JobFinder/src/test/java/com/ofdun/jobfinder/features/vacancy/domain/service/BasicVacancyServiceTest.java), `searchVacancies_clampsPagination`. В смешанном наборе [RedisTokenRepositoryTest](../JobFinder/src/test/java/com/ofdun/jobfinder/features/auth/data/jwt/repository/RedisTokenRepositoryTest.java), `getUserIdByToken_whenMissingOrMalformed_returnsNull`, переполнение относится к `boundary`, а `null`, пустая и нечисловая строки — к `equivalence`. |
| `combinatorial` | Полный декартов набор 4 × 4 для `resumeId` и `vacancyId`: значения `1, 0, null, -1`. Все 16 сочетаний представлены в [BasicApplicationValidatorTest](../JobFinder/src/test/java/com/ofdun/jobfinder/features/application/domain/validator/BasicApplicationValidatorTest.java), `validateApplicationForCreate_checksBothForeignKeys`. Это полное комбинаторное покрытие выбранных значений, а не случайная выборка. |

`state-transition` в текущем наборе не используется: ни один из ранее отмеченных сценариев не демонстрирует явно заданный переход между состояниями. Сам факт вызова create/delete, переназначение ID или проверка конечного ACTIVE не делает тест проверкой переходов состояний. Эти случаи отнесены к `equivalence`: объект существует/отсутствует, дочерние записи присутствуют, черновик принадлежит/не принадлежит вакансии, зависимость возвращает результат/ошибку. Например, [MongoResumeRepositoryTest](../JobFinder/src/test/java/com/ofdun/jobfinder/features/resume/data/mongo/repository/MongoResumeRepositoryTest.java), `createResume_whenChildrenPresent_reassignsParentIds`, проверяет категорию «дочерние записи присутствуют». Тест перехода должен явно задавать исходное состояние, событие и ожидаемое следующее состояние; такой сценарий здесь не заявляется.

Нулевая настройка TTL в getter, давно истёкший JWT, `setNext(null)`, обычная пагинация и некорректное соотношение зарплат классифицированы как `equivalence`: их текущие данные не демонстрируют проверку числового порога. Название метода с `Boundary` само по себе не определяет технику.

В классическом стиле используются настоящие объекты без mock/stub: [ApplicantPostgreSQLMapperTest](../JobFinder/src/test/java/com/ofdun/jobfinder/features/applicant/data/postgres/mapper/ApplicantPostgreSQLMapperTest.java), остальные тесты мапперов, [BCryptEncryptionServiceTest](../JobFinder/src/test/java/com/ofdun/jobfinder/features/encrypt/BCryptEncryptionServiceTest.java) и [BasicJwtProviderTest](../JobFinder/src/test/java/com/ofdun/jobfinder/features/auth/data/jwt/impl/BasicJwtProviderTest.java). Ожидаемые поля сравниваются явно, а не только посредством обратного вызова того же маппера.

Лондонский стиль представлен тестами сервисов, валидаторов и репозиториев: зависимости Mockito, контролируемые ошибки, проверки отсутствия записи после ошибки валидации. Модели данных в новых тестах создаются фабриками. В [BasicResumeServiceTest](../JobFinder/src/test/java/com/ofdun/jobfinder/features/resume/domain/service/BasicResumeServiceTest.java), `createResume_withRealHandlers_usesVectorCreateAfterEmbedding`, используются реальные обработчики и mock только на границах хранилищ и AI-клиента.

## Проверяемые требования

| № | Требование | Реализация и пример |
| --- | --- | --- |
| 1 | Отдельный test class, позитивный и негативный сценарии для публичных операций основных компонентов | Полная карта классов приведена ниже. Например, [PostgreSQLApplicantRepositoryTest](../JobFinder/src/test/java/com/ofdun/jobfinder/features/applicant/data/postgres/repository/PostgreSQLApplicantRepositoryTest.java): пары для create, update, getById, getByEmail, delete. Для методов без ошибочного результата, таких как getter настройки TTL или null-safe mapper, второй случай проверяет граничное/отсутствующее значение, а не выдуманное исключение. |
| 2 | Ожидаемые исключения | [BasicApplicationValidatorTest](../JobFinder/src/test/java/com/ofdun/jobfinder/features/application/domain/validator/BasicApplicationValidatorTest.java): `validateApplicationForDelete_whenMissing_throwsNotFound`; [BasicDraftVacancyServiceTest](../JobFinder/src/test/java/com/ofdun/jobfinder/features/vacancy/domain/service/BasicDraftVacancyServiceTest.java): `applyDraft_whenMalformedSnapshot_preservesStorage`; Qdrant-тесты сохраняют цепочку причин ошибки. |
| 3 | Классический и лондонский стили | Реальные мапперы/BCrypt/JWT и Mockito-тесты сервисов/репозиториев, описанные выше. |
| 4 | AAA, fixtures и helpers | Пустые строки разделяют блоки; `@BeforeEach`, `@Mock`, `@InjectMocks`, [ApplicantBuilder](../JobFinder/src/test/java/com/ofdun/jobfinder/support/ApplicantBuilder.java) и [TestDataMother](../JobFinder/src/test/java/com/ofdun/jobfinder/support/TestDataMother.java). |
| 5 | Одна проверяемая операция в Act | Вызов сервиса или репозитория один; для исключений находится внутри `assertThrows`. Составной сценарий resume проверяется одним вызовом `createResume`. |
| 6 | Без прямого тестирования private/protected методов | Обработчики проверяются через `handle`, фильтрация MongoDB через `searchResumes/searchVacancies`. |
| 7 | Data Builder и Object Mother | [ApplicantBuilder](../JobFinder/src/test/java/com/ofdun/jobfinder/support/ApplicantBuilder.java) и [TestDataMother](../JobFinder/src/test/java/com/ofdun/jobfinder/support/TestDataMother.java), с конкретными примерами выше. |
| 8 | Запуск из локальной копии репозитория | Gradle Wrapper и команды `./gradlew test coverageSummary`, `./gradlew check`. |
| 9 | Автоматический отчёт о выполнении | Стандартные HTML/JUnit XML отчёты Gradle; создаются той же командой. |
| 10 | Случайный порядок | `-PrandomOrder=true -PtestSeed=20260913`, порядок классов и методов. |
| 11 | Работа без интернета | `--offline` после загрузки зависимостей; также проверка в отдельном процессе с запретом внешних соединений. |
| 12 | Проверка и настройка процессов | PID в XML/консоли; `testForks`, `testForkEvery`; результаты измерения ниже. |
| 13 | Успешное выполнение | Результаты проверенных запусков ниже; ошибка теста завершает Gradle ненулевым кодом. |
| 14 | Line и branch coverage из CLI | `./gradlew coverageSummary`, HTML/XML/CSV JaCoCo. В отчёте учитывается весь backend, а не только протестированные классы. |
| 15 | Защита от регрессии, устойчивость к рефакторингу, поддерживаемость | Проверка возвращаемых данных и значимых взаимодействий, свежие fixtures, один сценарий на тест, отсутствие внешней инфраструктуры; регрессии удаления отклика и цепочки сохранения резюме. |

## Область тестирования

Основные компоненты здесь: прикладные сервисы, бизнес-валидаторы, обработчики резюме, написанные вручную репозитории PostgreSQL/MongoDB/Redis/Qdrant, Mongo sequence service, JWT/шифрование, весь пакет `auth.security`, мапперы хранения и спецификации запросов. Для каждого класса из этой области есть отдельный test class.

Интерфейсы Spring Data не содержат собственной реализации и заменяются mock. DTO, сущности с Lombok-accessors, конфигурация Spring за исключением `SecurityConfiguration`, MVC-контроллеры, Mongo lifecycle listeners и транспортные реализации внешних клиентов не входят в эту unit-область. Их код не скрывается из общего JaCoCo-отчёта, поэтому общее покрытие ниже покрытия перечисленных компонентов. Unit-тесты адаптеров не доказывают корректность реального SQL, транзакций, Mongo query execution или сетевого протокола.

## Интеграционные тесты с Docker

`test`, `unitTest`, `check` и `coverageSummary` не запускают `*IT`. Интеграционные тесты запускаются отдельно из `JobFinder`:

| Команда | Что запускается |
| --- | --- |
| `./gradlew integrationTest` | Все `*IT`: PostgreSQL, MongoDB, Redis, Qdrant, Ollama |
| `./gradlew testPostgres` | Только `*IT` с тегом `postgres`, профиль `test-postgres` |
| `./gradlew testMongo` | Только `*IT` с тегом `mongo`, профиль `test-mongo` |
| `./gradlew databaseIntegrationTest` | PostgreSQL и MongoDB, без Redis/Qdrant/Ollama |
| `./gradlew unitTest integrationTest` | Автономные тесты, затем все интеграционные |
| `./gradlew testAll` | То же самое: все `*Test` и все `*IT` |

`testAll` теперь действительно запускает все тесты backend. Прежнее поведение (только PostgreSQL/MongoDB) доступно через `databaseIntegrationTest`. Имена и описания доступны через `./gradlew tasks --group verification`. Для нового полного прогона с продолжением другой test-задачи после ошибки используй `./gradlew testAll --rerun-tasks --continue`.

Сначала запусти Docker Desktop и проверь `docker info`. Testcontainers создаёт контейнеры для тестов самостоятельно; вручную поднимать базы через Compose не требуется. Первое получение образов требует сети. MongoDB используется через [MongoRepositoryITBase](../JobFinder/src/test/java/com/ofdun/jobfinder/common/test/MongoRepositoryITBase.java); тестовая БД очищается между сценариями.

Для полного `integrationTest` заранее собери локальный образ, который ожидает [OllamaAiClientIT](../JobFinder/src/test/java/com/ofdun/jobfinder/features/clients/ai/OllamaAiClientIT.java). Команда загружает Ollama и модель и может выполняться долго:

```sh
docker build -t ollama-mxbai-embed-large:latest -f src/test/resources/docker/ollama.Dockerfile .
./gradlew integrationTest
```

Для одного класса или метода используй фильтр настоящей test-задачи:

```sh
./gradlew integrationTest --tests '*RedisTokenRepositoryIT'
./gradlew integrationTest --tests '*OllamaAiClientIT.getEmbedding_ReturnsVector_WhenModelAvailable'
./gradlew testPostgres --tests '*PostgreSQLApplicantRepositoryIT'
```

Выбранному Redis/PostgreSQL/MongoDB/Qdrant-тесту образ Ollama не нужен. Чтобы гарантировать новый запуск при актуальных результатах Gradle, добавь `--rerun-tasks`.

HTML-отчёты находятся в `build/reports/tests/integrationTest/index.html`, `build/reports/tests/testPostgres/index.html` и `build/reports/tests/testMongo/index.html`; XML — в соответствующих каталогах `build/test-results/<имя задачи>/`. У агрегатов `unitTest`, `databaseIntegrationTest` и `testAll` собственного отчёта нет. `coverageSummary` по-прежнему считает покрытие только прогона `test`, без объединения с интеграционными тестами.

Проверка 13.09.2026: `./gradlew --offline testAll coverageSummary --rerun-tasks --continue --console=plain` действительно выполнила оба набора. Unit: 621 cases / 89 классов, 0 failures/errors/skipped. Integration: 83 cases / 27 классов, 79 passed, 4 failures, 0 errors/skipped. Общий результат — `BUILD FAILED`, 6 мин 20 с. `--offline` здесь относится только к зависимостям Gradle; Docker загружал недостающие образы.

Все четыре ошибки находятся в [PostgreSQLVacancyRepositoryIT](../JobFinder/src/test/java/com/ofdun/jobfinder/features/vacancy/data/repository/PostgreSQLVacancyRepositoryIT.java): create/get/update/delete. SQL-лог указывает `column status does not exist`: [тестовая схема](../JobFinder/src/test/resources/db/migration-test/V1__init_schema.sql) не содержит `vacancies.status`, который ожидает [VacancyEntity](../JobFinder/src/main/java/com/ofdun/jobfinder/features/vacancy/data/postgres/entity/VacancyEntity.java). Схема и assertions в ходе настройки задач не изменялись. Остальные 26 integration-классов прошли, включая Redis, Qdrant и Ollama. [HTML-отчёт интеграционного прогона](../JobFinder/build/reports/tests/integrationTest/index.html).

Справка: [фильтрация тестов Gradle](https://docs.gradle.org/current/userguide/java_testing.html#test_filtering), [требования Testcontainers к Docker](https://java.testcontainers.org/supported_docker_environment/).

## Проверки безопасности

| Класс и тесты | Проверяемое поведение |
| --- | --- |
| [SecurityExpressionsTest](../JobFinder/src/test/java/com/ofdun/jobfinder/features/auth/security/SecurityExpressionsTest.java) | Собственный/чужой ID, обе роли, отсутствие аутентификации, неизвестный principal, принадлежность вакансии/резюме/отклика, отсутствующие записи и ошибки репозиториев |
| [JwtAuthenticationFilterTest](../JobFinder/src/test/java/com/ofdun/jobfinder/features/auth/security/JwtAuthenticationFilterTest.java) | Отсутствующий/пустой/неверный Bearer header, валидные токены обеих ролей, отклонённые токены, ошибки разбора и проверки, очистка контекста/атрибутов, вызов следующего фильтра и передача исключений |
| [SecurityConfigurationTest](../JobFinder/src/test/java/com/ofdun/jobfinder/features/auth/security/SecurityConfigurationTest.java) | Разрешённые публичные маршруты, отказ для защищённых маршрутов без токена, разрешение с токеном, POST без CSRF в stateless-режиме, порядок JWT/logging фильтров, разрешённый и запрещённый CORS |

Все новые тесты отмечены `equivalence`: данные выбираются по категориям «разрешено/запрещено», «свой/чужой», «валидно/невалидно», «существует/отсутствует». В параметризованных сценариях каждая строка является отдельным запуском. Фильтр тестируется через публичный `doFilter`, а не прямым вызовом защищённого `doFilterInternal`.

`SecurityConfigurationTest` — компонентная проверка конфигурации в памяти, а не запуск полного Spring Boot приложения. Каждый сценарий получает новый контекст; после теста он закрывается и очищается `SecurityContextHolder`. Остальные security-тесты работают без Spring-контекста. Криптографическая проверка JWT отдельно покрыта `BasicJwtProviderTest`; в тестах фильтра ответы `JwtProvider` задаются Mockito.

```sh
./gradlew test --tests 'com.ofdun.jobfinder.features.auth.security.*Test' --rerun-tasks
./gradlew test coverageSummary --rerun-tasks
```

Первая команда перезаписывает отчёт выбранным пакетом; вторая восстанавливает полный отчёт. Конкретные примеры для ручной проверки: `employerOwnsVacancy_checksOwner`, `canAccessApplication_checksOwnershipForEachRole`, `doFilter_whenProviderThrows_clearsIdentityAndContinues`, `securityFilterChain_whenProtectedRoute_rejectsAnonymousRequest`.

Исторический замер до расширения API и удаления тестов сгенерированного record: пакет `com.ofdun.jobfinder.features.auth.security` содержал 98 test cases в четырёх test classes. Покрытие строк каждого production-класса — 100%: `SecurityExpressions` 59/59, `JwtAuthenticationFilter` 30/30, `SecurityConfiguration` 40/40, `JobFinderPrincipal` 1/1. Итого LINE 130/130, BRANCH 61/66 = 92.42%.

Оставшиеся пять ветвей не скрыты: две относятся к повторным проверкам `p != null` в `SecurityExpressions` после проверки типа principal; три — к сгенерированным Lombok проверкам `@NonNull` параметров защищённого `doFilterInternal`. Пользовательские ветви фильтра для заголовка и проверки токена выполнены. 100% строк не означает исчерпывающий аудит безопасности или проверку всех последовательностей ошибок.

## Карта компонентов

Ссылки ведут на test class. Пары сценариев находятся рядом и названы по проверяемой операции. У наследников `ResumeHandler` проверяется публичный `handle`; общая логика `setNext` проверяется в `ResumeHandlerTest`.

| Компонент | Проверяемые публичные операции |
| --- | --- |
| [SecurityExpressions](../JobFinder/src/test/java/com/ofdun/jobfinder/features/auth/security/SecurityExpressionsTest.java) | `isSelf`, `isApplicant`, `isEmployer`, `employerOwnsVacancy`, `applicantOwnsResume`, `canAccessApplication` |
| [JwtAuthenticationFilter](../JobFinder/src/test/java/com/ofdun/jobfinder/features/auth/security/JwtAuthenticationFilterTest.java) | `doFilter (унаследованный)` |
| [SecurityConfiguration](../JobFinder/src/test/java/com/ofdun/jobfinder/features/auth/security/SecurityConfigurationTest.java) | `securityFilterChain`, `corsConfigurationSource` |
| [ApplicantAccountMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/auth/data/mapper/ApplicantAccountMapperTest.java) | `toModel` |
| [ApplicantAuthService](../JobFinder/src/test/java/com/ofdun/jobfinder/features/auth/domain/service/ApplicantAuthServiceTest.java) | `login`, `refreshToken`, `logout` |
| [ApplicantMongoMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/applicant/data/mongo/mapper/ApplicantMongoMapperTest.java) | `toEntity`, `toModel` |
| [ApplicantPostgreSQLMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/applicant/data/postgres/mapper/ApplicantPostgreSQLMapperTest.java) | `toEntity`, `toModel` |
| [ApplicationMongoMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/application/data/mongo/mapper/ApplicationMongoMapperTest.java) | `toEntity`, `toModel` |
| [ApplicationPostgreSQLMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/application/data/postgres/mapper/ApplicationPostgreSQLMapperTest.java) | `toEntity`, `toModel` |
| [BasicApplicantService](../JobFinder/src/test/java/com/ofdun/jobfinder/features/applicant/domain/service/BasicApplicantServiceTest.java) | `createApplicant`, `getApplicantById`, `updateApplicant`, `deleteApplicant` |
| [BasicApplicantValidator](../JobFinder/src/test/java/com/ofdun/jobfinder/features/applicant/domain/validator/BasicApplicantValidatorTest.java) | `validateApplicantForCreate`, `validateApplicantForUpdate`, `validateApplicantForDelete` |
| [BasicApplicationService](../JobFinder/src/test/java/com/ofdun/jobfinder/features/application/domain/service/BasicApplicationServiceTest.java) | `saveApplication`, `getApplication`, `getApplicationsByVacancyId`, `updateApplication`, `deleteApplication` |
| [BasicApplicationValidator](../JobFinder/src/test/java/com/ofdun/jobfinder/features/application/domain/validator/BasicApplicationValidatorTest.java) | `validateApplicationForCreate`, `validateApplicationForUpdate`, `validateApplicationForDelete` |
| [BasicCategoryService](../JobFinder/src/test/java/com/ofdun/jobfinder/features/category/domain/service/BasicCategoryServiceTest.java) | `getCategoryById`, `getAllCategories` |
| [BasicDraftVacancyService](../JobFinder/src/test/java/com/ofdun/jobfinder/features/vacancy/domain/service/BasicDraftVacancyServiceTest.java) | `createDraft`, `getDrafts`, `applyDraft`, `deleteDraft` |
| [BasicEmployerService](../JobFinder/src/test/java/com/ofdun/jobfinder/features/employer/domain/service/BasicEmployerServiceTest.java) | `createEmployer`, `getEmployerById`, `updateEmployer`, `deleteEmployer` |
| [BasicEmployerValidator](../JobFinder/src/test/java/com/ofdun/jobfinder/features/employer/domain/validator/BasicEmployerValidatorTest.java) | `validateEmployerForCreate`, `validateEmployerForUpdate`, `validateEmployerForDelete` |
| [BasicJwtProvider](../JobFinder/src/test/java/com/ofdun/jobfinder/features/auth/data/jwt/impl/BasicJwtProviderTest.java) | `getRefreshTokenExpiration`, `generateAccessToken`, `generateRefreshToken`, `validateToken`, `getUserId`, `getAccountType` |
| [BasicLanguageRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/language/domain/service/BasicLanguageRepositoryTest.java) | `getLanguageById`, `getAllLanguages` |
| [BasicLocationService](../JobFinder/src/test/java/com/ofdun/jobfinder/features/location/domain/service/BasicLocationServiceTest.java) | `getLocationById`, `searchLocations` |
| [BasicMatchingService](../JobFinder/src/test/java/com/ofdun/jobfinder/features/matching/service/BasicMatchingServiceTest.java) | `findSuitableCandidates` |
| [BasicResumeService](../JobFinder/src/test/java/com/ofdun/jobfinder/features/resume/domain/service/BasicResumeServiceTest.java) | `createResume`, `getResumeById`, `updateResume`, `deleteResume`, `searchResumes` |
| [BasicSkillService](../JobFinder/src/test/java/com/ofdun/jobfinder/features/skill/domain/service/BasicSkillServiceTest.java) | `getSkillById`, `getAllSkills` |
| [BasicVacancyService](../JobFinder/src/test/java/com/ofdun/jobfinder/features/vacancy/domain/service/BasicVacancyServiceTest.java) | `createVacancy`, `getVacancyById`, `updateVacancy`, `deleteVacancy`, `searchVacancies` |
| [BasicVacancyValidator](../JobFinder/src/test/java/com/ofdun/jobfinder/features/vacancy/domain/validator/BasicVacancyValidatorTest.java) | `validateVacancyForCreate`, `validateVacancyForUpdate`, `validateVacancyForDelete` |
| [BCryptEncryptionService](../JobFinder/src/test/java/com/ofdun/jobfinder/features/encrypt/BCryptEncryptionServiceTest.java) | `encrypt`, `matches` |
| [CategoryMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/category/data/postgres/mapper/CategoryMapperTest.java) | `toModel`, `toEntity` |
| [CategoryMongoMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/category/data/mongo/mapper/CategoryMongoMapperTest.java) | `toModel`, `toEntity` |
| [DraftVacancyMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/vacancy/data/postgres/mapper/DraftVacancyMapperTest.java) | `toModel`, `toEntity` |
| [DraftVacancyMongoMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/vacancy/data/mongo/mapper/DraftVacancyMongoMapperTest.java) | `toEntity`, `toModel` |
| [EducationMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/education/data/postgres/mapper/EducationMapperTest.java) | `toModel`, `toEntity` |
| [EducationMongoMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/education/data/mongo/mapper/EducationMongoMapperTest.java) | `toModel`, `toEntity` |
| [EmbeddingResumeHandler](../JobFinder/src/test/java/com/ofdun/jobfinder/features/resume/domain/chain/EmbeddingResumeHandlerTest.java) | `handle (унаследованный)` |
| [EmployerAccountMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/auth/data/mapper/EmployerAccountMapperTest.java) | `toModel` |
| [EmployerAuthService](../JobFinder/src/test/java/com/ofdun/jobfinder/features/auth/domain/service/EmployerAuthServiceTest.java) | `login`, `refreshToken`, `logout` |
| [EmployerMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/employer/data/postgres/mapper/EmployerMapperTest.java) | `toEntity`, `toModel` |
| [EmployerMongoMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/employer/data/mongo/mapper/EmployerMongoMapperTest.java) | `toEntity`, `toModel` |
| [JobExperienceMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/experience/data/postgres/mapper/JobExperienceMapperTest.java) | `toModel`, `toEntity` |
| [JobExperienceMongoMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/experience/data/mongo/mapper/JobExperienceMongoMapperTest.java) | `toModel`, `toEntity` |
| [LanguageMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/language/data/postgres/mapper/LanguageMapperTest.java) | `toModel`, `toEntity` |
| [LanguageMongoMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/language/data/mongo/mapper/LanguageMongoMapperTest.java) | `toModel`, `toEntity` |
| [LocationMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/location/data/postgres/mapper/LocationMapperTest.java) | `toEntity`, `toModel` |
| [LocationMongoMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/location/data/mongo/mapper/LocationMongoMapperTest.java) | `toEntity`, `toModel` |
| [MongoApplicantAccountRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/auth/data/repository/MongoApplicantAccountRepositoryTest.java) | `findByEmail` |
| [MongoApplicantRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/applicant/data/mongo/repository/MongoApplicantRepositoryTest.java) | `createApplicant`, `getApplicantById`, `getApplicantByEmail`, `updateApplicant`, `deleteApplicant` |
| [MongoApplicationRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/application/data/mongo/repository/MongoApplicationRepositoryTest.java) | `createApplication`, `getApplicationById`, `getApplicationsByVacancyId`, `updateApplication`, `deleteApplication` |
| [MongoCategoryRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/category/data/mongo/repository/MongoCategoryRepositoryTest.java) | `getCategoryById`, `getAllCategories` |
| [MongoDraftVacancyRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/vacancy/data/mongo/repository/MongoDraftVacancyRepositoryTest.java) | `save`, `getByVacancyId`, `getById`, `deleteById` |
| [MongoEducationRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/education/data/mongo/repository/MongoEducationRepositoryTest.java) | `createEducations`, `getEducationsByResumeId`, `deleteAllByResumeId` |
| [MongoEmployerAccountRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/auth/data/repository/MongoEmployerAccountRepositoryTest.java) | `findByEmail` |
| [MongoEmployerRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/employer/data/mongo/repository/MongoEmployerRepositoryTest.java) | `createEmployer`, `getEmployerById`, `getEmployerByEmail`, `updateEmployer`, `deleteEmployer` |
| [MongoJobExperienceRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/experience/data/mongo/repository/MongoJobExperienceRepositoryTest.java) | `createJobExperience`, `createJobExperiences`, `getJobExperiencesByResumeId`, `deleteAllByResumeId` |
| [MongoLanguageRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/language/data/mongo/repository/MongoLanguageRepositoryTest.java) | `getLanguageById`, `getAllLanguages` |
| [MongoLocationRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/location/data/mongo/repository/MongoLocationRepositoryTest.java) | `getLocationById`, `searchLocations` |
| [MongoResumeRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/resume/data/mongo/repository/MongoResumeRepositoryTest.java) | `createResume`, `getResumeById`, `updateResume`, `deleteResume`, `searchResumes` |
| [MongoSequenceService](../JobFinder/src/test/java/com/ofdun/jobfinder/common/data/mongo/service/MongoSequenceServiceTest.java) | `generateSequence` |
| [MongoSkillRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/skill/data/mongo/repository/MongoSkillRepositoryTest.java) | `getSkillById`, `getAllSkills` |
| [MongoVacancyRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/vacancy/data/mongo/repository/MongoVacancyRepositoryTest.java) | `createVacancy`, `getVacancyById`, `updateVacancy`, `deleteVacancy`, `searchVacancies` |
| [NoEncryptionService](../JobFinder/src/test/java/com/ofdun/jobfinder/features/encrypt/NoEncryptionServiceTest.java) | `encrypt`, `matches` |
| [PostgreSQLApplicantAccountRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/auth/data/repository/PostgreSQLApplicantAccountRepositoryTest.java) | `findByEmail` |
| [PostgreSQLApplicantRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/applicant/data/postgres/repository/PostgreSQLApplicantRepositoryTest.java) | `createApplicant`, `getApplicantById`, `getApplicantByEmail`, `updateApplicant`, `deleteApplicant` |
| [PostgreSQLApplicationRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/application/data/postgres/repository/PostgreSQLApplicationRepositoryTest.java) | `createApplication`, `getApplicationById`, `getApplicationsByVacancyId`, `updateApplication`, `deleteApplication` |
| [PostgreSQLCategoryRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/category/data/postgres/repository/PostgreSQLCategoryRepositoryTest.java) | `getCategoryById`, `getAllCategories` |
| [PostgreSQLDraftVacancyRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/vacancy/data/postgres/repository/PostgreSQLDraftVacancyRepositoryTest.java) | `save`, `getByVacancyId`, `getById`, `deleteById` |
| [PostgreSQLEmployerAccountRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/auth/data/repository/PostgreSQLEmployerAccountRepositoryTest.java) | `findByEmail` |
| [PostgreSQLEmployerRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/employer/data/postgres/repository/PostgreSQLEmployerRepositoryTest.java) | `createEmployer`, `getEmployerById`, `getEmployerByEmail`, `updateEmployer`, `deleteEmployer` |
| [PostgreSQLLanguageRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/language/data/postgres/repository/PostgreSQLLanguageRepositoryTest.java) | `getLanguageById`, `getAllLanguages` |
| [PostgreSQLLocationRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/location/data/postgres/repository/PostgreSQLLocationRepositoryTest.java) | `getLocationById`, `searchLocations` |
| [PostgreSQLResumeRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/resume/data/postgres/repository/PostgreSQLResumeRepositoryTest.java) | `createResume`, `getResumeById`, `updateResume`, `deleteResume`, `searchResumes` |
| [PostgreSQLSkillRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/skill/data/postgres/repository/PostgreSQLSkillRepositoryTest.java) | `getSkillById`, `getAllSkills` |
| [PostgreSQLVacancyRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/vacancy/data/postgres/repository/PostgreSQLVacancyRepositoryTest.java) | `createVacancy`, `getVacancyById`, `updateVacancy`, `deleteVacancy`, `searchVacancies` |
| [QdrantResumeRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/resume/data/postgres/repository/QdrantResumeRepositoryTest.java) | `createResume`, `getResumeById`, `updateResume`, `getMostSimilarResumes`, `deleteResume` |
| [RedisTokenRepository](../JobFinder/src/test/java/com/ofdun/jobfinder/features/auth/data/jwt/repository/RedisTokenRepositoryTest.java) | `saveToken`, `getUserIdByToken`, `deleteToken` |
| [RelationalResumeGetHandler](../JobFinder/src/test/java/com/ofdun/jobfinder/features/resume/domain/chain/get/RelationalResumeGetHandlerTest.java) | `handle (унаследованный)` |
| [RelationalResumeSaveHandler](../JobFinder/src/test/java/com/ofdun/jobfinder/features/resume/domain/chain/save/RelationalResumeSaveHandlerTest.java) | `handle (унаследованный)` |
| [RelationalResumeUpdateHandler](../JobFinder/src/test/java/com/ofdun/jobfinder/features/resume/domain/chain/update/RelationalResumeUpdateHandlerTest.java) | `handle (унаследованный)` |
| [ResumeHandler](../JobFinder/src/test/java/com/ofdun/jobfinder/features/resume/domain/chain/ResumeHandlerTest.java) | `setNext`, `handle` |
| [ResumeMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/resume/data/postgres/mapper/ResumeMapperTest.java) | `toModel`, `toEntity` |
| [ResumeMongoMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/resume/data/mongo/mapper/ResumeMongoMapperTest.java) | `toEntity`, `toModel` |
| [ResumeSpecifications](../JobFinder/src/test/java/com/ofdun/jobfinder/features/resume/data/spec/ResumeSpecificationsTest.java) | `byFilter` |
| [SkillMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/skill/data/postgres/mapper/SkillMapperTest.java) | `toModel`, `toEntity` |
| [SkillMongoMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/skill/data/mongo/mapper/SkillMongoMapperTest.java) | `toModel`, `toEntity` |
| [VacancyMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/vacancy/data/postgres/mapper/VacancyMapperTest.java) | `toEntity`, `toModel` |
| [VacancyMongoMapper](../JobFinder/src/test/java/com/ofdun/jobfinder/features/vacancy/data/mongo/mapper/VacancyMongoMapperTest.java) | `toEntity`, `toModel` |
| [VacancySpecifications](../JobFinder/src/test/java/com/ofdun/jobfinder/features/vacancy/data/spec/VacancySpecificationsTest.java) | `byFilter` |
| [VectorResumeGetHandler](../JobFinder/src/test/java/com/ofdun/jobfinder/features/resume/domain/chain/get/VectorResumeGetHandlerTest.java) | `handle (унаследованный)` |
| [VectorResumeSaveHandler](../JobFinder/src/test/java/com/ofdun/jobfinder/features/resume/domain/chain/save/VectorResumeSaveHandlerTest.java) | `handle (унаследованный)` |
| [VectorResumeUpdateHandler](../JobFinder/src/test/java/com/ofdun/jobfinder/features/resume/domain/chain/update/VectorResumeUpdateHandlerTest.java) | `handle (унаследованный)` |

## Изменения поведения

- `BasicApplicationValidator.validateApplicationForDelete` теперь разрешает удаление существующего отклика и выбрасывает `ApplicationNotFoundException` при отсутствии. Раньше проверка существования была инвертирована.
- `BasicResumeService` больше не связывает общие изменяемые экземпляры обработчиков через `setNext`. Создание, чтение и обновление используют независимые последовательности функций. Создание резюме вызывает vector create, обновление - vector update.
- `BasicVacancyValidator.validateVacancyForCreate` явно отклоняет `null`.
- В существующем тесте отсутствующего соискателя проверяется `ApplicantNotFoundException`, а не устаревшая строка про уже существующий аккаунт.
- Исправлено правило `**build**` в `.gitignore`: оно скрывало исходный файл `ApplicantBuilder.java`. Теперь игнорируются каталоги `build/`, и wrapper доступен для включения в репозиторий.

## Проверенные результаты

Проверено 13.09.2026: JDK 25.0.2, Gradle 9.4.0, macOS.

| Запуск | Результат |
| --- | --- |
| Обычный `test coverageSummary` | Успешно |
| `--offline --rerun-tasks -PrandomOrder=true -PtestSeed=20260913` | 523 теста, 85 классов, 0 ошибок, 0 пропусков |
| `--no-daemon --offline check coverageSummary --rerun-tasks` в sandbox с запретом внешней сети | 523 теста, 85 классов, 0 ошибок, 0 пропусков; Spotless пройден |
| После исправления классификации: `--offline check coverageSummary --rerun-tasks` | 523 теста, 85 классов, 0 ошибок, 0 пропусков; Spotless пройден |
| После добавления security-тестов: `--no-daemon --offline check coverageSummary --rerun-tasks -PrandomOrder=true -PtestSeed=20260913` в sandbox с запретом внешней сети | 621 test cases, 89 классов, 0 failures/errors/skipped; Spotless пройден |
| Процессы сетево-изолированного прогона после добавления security-тестов | 1 worker JVM, PID 61105; одинаковый PID у всех 89 классов |
| После добавления `unitTest`: `--offline unitTest coverageSummary --rerun-tasks` | 621 test cases, 89 классов, 0 failures/errors/skipped |
| Полный `--offline testAll coverageSummary --rerun-tasks --continue` | Unit: 621/621 passed; Integration: 79/83 passed, 4 ошибки PostgreSQLVacancyRepositoryIT из-за отсутствующего `vacancies.status` в тестовой схеме |
| Покрытие строк всего backend | 1524 / 2294 = 66.43% |
| Покрытие ветвлений всего backend | 405 / 851 = 47.59% |
| Покрытие пакета `auth.security` | LINE 130/130 = 100%; BRANCH 61/66 = 92.42% |

Счётчики тестов и PID взяты из созданных Gradle XML-отчётов; покрытие - из JaCoCo. Наблюдение за процессами относится к полному запуску с настройками по умолчанию. Режимы с другим `testForks/testForkEvery` доступны, но отдельно не измерялись.

Перед сетево-изолированным прогоном проверено, что sandbox разрешает локальное соединение и отклоняет соединение к внешнему адресу с `Operation not permitted`. Интеграционные `*IT` в эту сетево-изолированную проверку и в приведённое покрытие не входят; их отдельный полный запуск указан в строке `testAll`.

Низкое общее покрытие не означает выполнение всех ветвей перечисленных компонентов: таблица подтверждает наличие позитивных/негативных сценариев публичных операций, а точное покрытие каждого класса нужно смотреть в JaCoCo. Числовой порог покрытия не задан; отчёт намеренно не исключает непокрытые production-классы.

## Документация инструментов

- [JUnit: порядок классов и методов, parameterized tests](https://docs.junit.org/6.0.1/user-guide/)
- [Gradle: test workers, maxParallelForks, forkEvery и отчёты](https://docs.gradle.org/9.4.0/userguide/java_testing.html)
- [Gradle: JaCoCo](https://docs.gradle.org/9.4.0/userguide/jacoco_plugin.html)
- [JaCoCo: поддержка Java 25 в версии 0.8.14](https://www.jacoco.org/jacoco/trunk/doc/changes.html)

## Проверка контракта API

[swagger.yml](swagger.yml) — единый контракт JobFinder в OpenAPI 3.1. Адреса начинаются с
`/api/v2/`; прежние адреса `/api/v1/` сохранены для веб-клиента. CRUD вакансий: `POST /api/v2/vacancies`,
`GET /api/v2/vacancies/{id}`, `PUT /api/v2/vacancies/{id}`, `DELETE /api/v2/vacancies/{id}`. Замена подресурса статуса: `PUT /api/v2/applications/{id}/status`
с телом `{"applicationStatus":"INVITATION"}` сохраняет остальные поля отклика.

Из корня репозитория, без запуска backend или базы данных:

```sh
npm --prefix JobFinder-front ci
node scripts/check-api.cjs
```

Если зависимости frontend уже установлены, достаточно второй команды. Проверка использует
Redocly OpenAPI Core, AJV JSON Schema 2020-12 и ajv-formats из lock-файла:
проверяет структуру OpenAPI, ссылки, операции, схемы, форматы дат/email/URI и примеры запросов/ответов, а также
наличие примеров у всех описанных тел. Ошибки дают ненулевой код завершения. Проверка
выполняется локально, не отправляет спецификацию и не обращается к серверу API;
внешние `$ref` и `externalValue` запрещены. Для проверки копии можно передать её путь:
`node scripts/check-api.cjs /tmp/swagger.yml`.

Сервер реализует собственные отклики, полную замену резюме, ограничения владения,
заголовки `Location`/`WWW-Authenticate`, единый формат ошибок и проверку входных
данных по этой спецификации. Самостоятельная проверка YAML не заменяет HTTP-тесты.
Команда `npm --prefix JobFinder-front run api:generate` обновляет существующий файл
типов из контракта v2; работающий веб-клиент использует свои модули REST v1.

Backend реализует единые `POST /api/v2/auth/login` и `POST /api/v2/auth/refresh`
с `userType` и поиск через
`GET /api/v2/vacancies`, `GET /api/v2/resumes`, `GET /api/v2/locations` с query-параметрами.
Статус отклика заменяется через `PUT /api/v2/applications/{id}/status`, версия вакансии
восстанавливается через `POST /api/v2/vacancies/{id}/drafts/{draftId}`.
Прежние маршруты сохранены как алиасы для совместимости с существующими клиентами.

Проверка работающего backend (без Prism):

```sh
python3 scripts/check-rest.py
```

Проверка создаёт отдельные учётные записи, вакансию, резюме и отклик, проверяет роли,
ротацию refresh-токенов, фильтры, замену статуса и восстановление версии, затем удаляет
созданные записи. Проверяются также старые маршруты v1, GraphQL и доступность Swagger.
Нужны заполненные справочники; город для поиска можно задать через `--location-query`.
Контракт и маршруты дополнительно проверяет `RestContractAlignmentTest`.

## Проверка API через mock-сервер

Из корня репозитория:

```sh
./scripts/check-api-mock.sh
# Другой порт:
PORT=4011 ./scripts/check-api-mock.sh
```

Скрипт запускает Prism по `docs/swagger.yml` на `127.0.0.1`, дожидается готовности,
выполняет [Arazzo-сценарии](api-workflows.arazzo.yml) через Redocly Respect и останавливает mock.
Проверяются регистрация соискателя (201 и числовой ID), вход, чтение и удаление профиля,
поиск вакансий, резюме и локаций через GET коллекций, а также схемы и типы ответов. Источник контракта называется
`jobfinder`, поэтому переопределение сервера имеет вид `--server "jobfinder=http://127.0.0.1:4010"`.

Нужны Node.js/npm и curl. Версии инструментов закреплены: Prism 5.14.2 и Redocly CLI 2.58.2.
Если системный Node несовместим, скрипт использует Node 22.23.3 через npx без изменения
системной установки. Первый запуск скачивает инструменты из npm; контракт остаётся локальным,
телеметрия Redocly отключена. Prism возвращает статические примеры и не хранит созданные
записи: этот прогон проверяет контракт, а не реализацию, владение данными или реальный вход.

Документация: [Prism CLI](https://github.com/stoplightio/prism/blob/main/docs/getting-started/03-cli.md),
[Redocly Respect](https://redocly.com/docs/respect/commands/respect).

## Проверка контракта GraphQL

[SDL](schema.graphqls) и [описание с примерами](graphql.md) задают дополнительный
контракт `POST /api/v2`. После установки frontend-зависимостей из корня репозитория:

```sh
node scripts/check-graphql.cjs
```

Проверяются SDL, все именованные операции из документации и их переменные,
покрытие корневых полей примерами, типы и пользовательские скаляры (включая
календарные даты, email, HTTP(S) URL, денежные строки и диапазон Long).
Встроенные негативные проверки отклоняют неизвестные/секретные поля, неверные
типы, enum, обязательные параметры и невалидные скалярные значения. Для проверки
копий можно передать пути: `node scripts/check-graphql.cjs /tmp/schema.graphqls /tmp/graphql.md`.

Это локальная проверка контракта без HTTP-сервера, базы и resolvers. Она не проверяет
права доступа, бизнес-ограничения ID/пагинации/паролей или реальное исполнение мутаций;
реализация проверяется HTTP-тестами отдельно.


## Запуск и проверка интерфейсов

Из корня репозитория: `docker compose up --build`. Файл `JobFinder/.env` задаёт
`JWT_SECRET` и параметры окружения backend. Существующие тома базы данных сохраняются.
При запуске Flyway добавляет версии вакансий, статус вакансии и уникальность пары
«вакансия–резюме». Если старая база содержит повторные пары, миграция останавливается;
данные автоматически не удаляются.

- GraphQL: `POST http://localhost:8080/api/v2`, именованная операция, заголовок
  `Accept: application/graphql-response+json`; [операции и переменные](graphql.md).
- REST: `http://localhost:8080/api/v2/...`; предыдущие маршруты `/api/v1/...` работают одновременно.
- Swagger UI: `http://localhost:8080/api/v2/docs`; UI и ресурсы загружаются локально,
  контракт — `/api/v2/openapi.yml`, OpenAPI 3.1.
- Веб-клиент: `http://localhost:5173`, использует прежний REST v1.
- CLI: из корня репозитория запустить `./JobFinder/gradlew -p JobFinder-CLI run --console=plain`
  либо после `./JobFinder/gradlew -p JobFinder-CLI installDist` выполнить
  `JobFinder-CLI/build/install/JobFinder-CLI/bin/JobFinder-CLI`.
  `JOBFINDER_API_URL` по умолчанию равен `http://localhost:8080/api/v2`.

Проверки backend из `JobFinder` (JDK 25, Gradle Wrapper):

```sh
./gradlew test
./gradlew testPostgres --tests '*ApplicationRepositoryIT' --tests '*VacancyRepositoryIT'
./gradlew testMongo --tests '*ApplicationRepositoryIT'
```

`GraphQlHttpTest` проверяет HTTP-методы, заголовки, именованные операции, переменные,
форматы скаляров, ограничения глубины/числа полей, независимые ошибки полей,
авторизацию и владение, повторный отклик, сохранение связей/даты при смене статуса,
повторную проверку вложенного резюме и полную замену с очисткой списков.
`ApiContractTest` проверяет реальные схемы запросов, nullable даты, запрет лишних
полей, семантику POST/PUT и совместное существование REST v1/v2.
Репозиторные тесты используют отдельные PostgreSQL/MongoDB-контейнеры и проверяют
фильтрацию собственных откликов и пагинацию.

HTTP-проверка с запущенными PostgreSQL, Redis, Qdrant и Ollama выполнена:
регистрация/вход обоих типов аккаунтов; создание вакансии и резюме с образованием
и текущим местом работы; отклик, выборки и смена статуса; сохранение/восстановление
версии; полная замена резюме с очисткой текста и связей; чтение вакансии через обе
версии REST; отказ без токена и запрет повторного отклика. Проверены HTML Swagger UI,
локальные JS/CSS и адрес контракта. Эта HTTP-проверка не является проверкой
отрисовки UI в браузере.

Профиль MongoDB также запущен и проверен: публичный GraphQL-запрос вернул HTTP 200,
а индекс `applications_vacancy_resume_unique` присутствует с `unique: true`.
Проверка PostgreSQL/MongoDB не удаляет существующие пользовательские данные.
CLI проверен как установленный исполняемый файл с настоящим backend; веб-клиент
одновременно отдаёт страницу и проксирует REST v1. Тесты JWT отдельно проверяют
назначение access/refresh и отказ использовать refresh-токен как Bearer в v2.

Проверка 19.09.2026: `spotlessApply test spotlessCheck` — 647 тестов backend,
0 ошибок и пропусков; целевые PostgreSQL-тесты — 12, MongoDB — 2, все успешны.
CLI: `check installDist` — 8 HTTP-тестов, все успешны. Проверки контрактов:
39 REST-операций и 224 примера; 37 корневых GraphQL-полей, 25 операций документации,
7 наборов переменных; 16 некорректных GraphQL-примеров отклонены.

## Ограничения реализации

1. Проверены версии/профили, миграции, контроллеры, `SecurityExpressions`, сервисы резюме/подбора, маршруты SPA и Compose. В текущей физической схеме **нет отдельного поля названия вакансии**: краткий заголовок UI выводится из описания. В сценарии публикации перечислены существующие поля.
2. Таблица черновиков создаётся миграцией V8; актуальная схема включает последующие миграции вплоть до V10. Старые DBML-материалы не заменяют миграции.
3. API проверяет роль и владельца резюме при создании отклика; обновление доступно соответствующим сторонам с проверкой допустимых полей. Проверки находятся в `ApiAccess` и контроллерах.
4. «Мои отклики» в SPA строятся по ID в `sessionStorage` ([trackedApplications.ts](../JobFinder-front/src/shared/session/trackedApplications.ts)); это не гарантирует полный список с другого устройства или новой сессии. Серверный источник списка доступен через GraphQL `myApplications` и `GET /api/v2/applications`; существующий SPA пока использует прежний механизм.
5. Сохранение резюме использует синхронную цепочку PostgreSQL → эмбеддинг → Qdrant и зависит от доступности векторной инфраструктуры.
6. Автоматизированный подбор в коде — эмбеддинг и векторная близость, не генерация аргументированного заключения. UI принимает решение о статусе по действию работодателя.
7. В существующих диаграммах остались расхождения с реализацией: DBML и его изображение различаются в модели языков и содержат прежние типы/состав полей; ER требует уточнения связей местоположения и уровня языка. В компонентной C4-схеме обозначения JDBC у Redis/Qdrant и часть имён репозиториев устарели. При работе со схемой БД следует опираться на миграции, с компонентами — на соответствующие классы.


## Проверка сборки — 18.10.2026

Для состава с REST v1/v2, GraphQL и CLI без распределённого развёртывания выполнены:

- Backend: `./JobFinder/gradlew -p JobFinder test bootJar` — 665 тестов, 0 ошибок и пропусков.
- CLI: `./JobFinder/gradlew -p JobFinder-CLI test` — 8 тестов, 0 ошибок и пропусков.
- OpenAPI: 37 операций и 225 примеров прошли lint и проверку схем.
- GraphQL: 25 операций, 7 наборов переменных и 37 корневых полей проверены;
  16 некорректных примеров отклонены.
- `npm --prefix JobFinder-front run build` — успешная сборка прежнего веб-клиента.

В этом прогоне отдельный backend по HTTP не запускался; приведённые выше исторические
HTTP-проверки не заменяют проверку нового развёртывания.

## Регрессии REST v2 и CLI

Дополнительные сценарии используют существующие задачи JUnit и те же тестовые контейнеры.
Не добавлены отдельные тяжёлые стадии CI или контейнеры для каждого случая.

| Уровень | Проверяемое поведение | Тесты |
| --- | --- | --- |
| Unit | Полная замена резюме очищает необязательные поля, сохраняет дату публикации; чужой владелец и отсутствующая запись отклоняются до записи; ошибка БД не запускает индексацию | [BasicResumeServiceTest](../JobFinder/src/test/java/com/ofdun/jobfinder/features/resume/domain/service/BasicResumeServiceTest.java) |
| HTTP-компонентный | Границы пагинации, неверные типы фильтров, переполнение Long и недопустимые enum отклоняются до вызова сервиса | [RestContractAlignmentTest](../JobFinder/src/test/java/com/ofdun/jobfinder/features/api/RestContractAlignmentTest.java) |
| CLI с локальным HTTP-сервером | Неуспешный refresh не повторяет исходный запрос; второй 401 останавливает повторы; неполная пара токенов не портит сессию; частичный GraphQL-ответ с errors не считается успехом | [ApiClientTest](../JobFinder-CLI/src/test/java/com/ofdun/jobfinder/cli/http/ApiClientTest.java) |
| Интеграционный PostgreSQL | БД запрещает повтор пары вакансия–резюме; повторная смена статуса сохраняет ID, связи и дату; проверка перечитывает запись после очистки persistence context | [PostgreSQLApplicationRepositoryIT](../JobFinder/src/test/java/com/ofdun/jobfinder/features/application/data/repository/PostgreSQLApplicationRepositoryIT.java) |
| E2E | Вход и ротация токенов обеих ролей; отказ при чужой роли, повторном refresh и подмене access/refresh; создание отклика и конфликт повтора; PUT статуса с проверкой роли и идемпотентности; чтение результата через GraphQL; восстановление и удаление версии вакансии | [ApplicationJourneyE2E](../JobFinder/src/test/java/com/ofdun/jobfinder/features/application/api/ApplicationJourneyE2E.java) |

Удалены шесть проверок сгенерированных конструктора, геттеров, `equals/hashCode`
у `JobFinderPrincipal`. Роли и владение по-прежнему проверяются через
`SecurityExpressionsTest`, JWT-фильтр и HTTP-сценарии. Проверки мапперов сохранены:
они проверяют преобразование моделей между API, доменом и хранилищем.

Команды из корня репозитория:

```sh
./JobFinder/gradlew -p JobFinder test
./JobFinder/gradlew -p JobFinder-CLI test
./JobFinder/gradlew -p JobFinder testPostgres --tests '*PostgreSQLApplicationRepositoryIT'
./JobFinder/gradlew -p JobFinder e2eTest
```

Для двух последних команд нужен Docker. E2E использует один набор временных
PostgreSQL, Redis, MongoDB и Qdrant для всех сценариев; база приложения не затрагивается.
После каждого сценария восстанавливаются SQL-фикстуры и очищаются тестовые сессии.
После пересоздания схемы обновляются соединения пула, чтобы PostgreSQL не использовал
кэшированные планы запросов к удалённым типам.

Результат прогона 18.10.2026: 673 unit/компонентных теста backend, 12 тестов CLI,
9 целевых интеграционных PostgreSQL-тестов и 4 E2E-сценария — без ошибок и пропусков.
В сумме добавлены 23 сценария/параметризованных случая и удалены 6 проверок record.
Полный набор MongoDB-интеграционных тестов в этом прогоне не запускался.

## Расширенная проверка доступа и хранения

Следующий проход опирается на пропуски JaCoCo и бизнес-сценарии, а не на число тестов.
У `GraphQlOperations` до него выполнялось 69 из 295 строк. Проверки дополняют существующие
сценарии и используют прежние задачи `test`, `testPostgres`, `testMongo`, `e2eTest`.

| Область | Проверяемые гарантии |
| --- | --- |
| [ApiAccessTest](../JobFinder/src/test/java/com/ofdun/jobfinder/features/api/ApiAccessTest.java) | Неаутентифицированный запрос не читает хранилище; чтение резюме зависит от роли и владельца; доступ к отклику проверяется через правильный ресурс; дубликаты отклоняются; ID не переполняет Long |
| [GraphQlHttpTest](../JobFinder/src/test/java/com/ofdun/jobfinder/features/graphql/GraphQlHttpTest.java) | Регистрация/замена профилей и сохранение пароля; вход/refresh обеих ролей; запрет чужого удаления для пяти ресурсов; ограничение поиска собственными резюме; сортировка/пагинация и точность зарплаты; границы фильтров; версии вакансий; запрет переноса связей отклика; доступ к подбору и предел его размера; отсутствие секретов в ошибках |
| [ResumePersistenceContract](../JobFinder/src/test/java/com/ofdun/jobfinder/features/resume/data/repository/ResumePersistenceContract.java) | Одинаковые проверки для PostgreSQL и MongoDB: создание образования и опыта работы с открытой конечной датой; очистка вложенных записей и списков; замена без накопления дублей и изменения чужих вложенных записей; удаление без сирот; стабильные страницы при одинаковой дате; фильтрация по владельцу |
| [ApplicationJourneyE2E](../JobFinder/src/test/java/com/ofdun/jobfinder/features/application/api/ApplicationJourneyE2E.java) | CRUD обоих профилей с настоящей аутентификацией, сохранением пароля и запретом чужих изменений; CRUD вакансии, неизменность владельца/даты и поиск; запрет чтения/удаления чужого отклика и переноса связей; отклонение некорректной регистрации без записи в БД |

Общий E2E HTTP-клиент также проверяет `Location` при 201, отсутствие тела при 204
и отсутствие имени исключения, внутреннего сообщения и stack trace в REST v2 ошибках.
PostgreSQL-тесты перечитывают данные после flush/clear; MongoDB использует настоящие
listeners генерации ID и отдельные коллекции образования/опыта. Общий persistence-набор
не подменяет одну СУБД другой: каждый сценарий выполняется в обеих СУБД.

```sh
./JobFinder/gradlew -p JobFinder test
./JobFinder/gradlew -p JobFinder testPostgres --tests '*PostgreSQLResumeRepositoryIT'
./JobFinder/gradlew -p JobFinder testMongo --tests '*MongoResumeRepositoryIT'
./JobFinder/gradlew -p JobFinder e2eTest
```

Новых стадий CI и отдельных контейнеров на каждый сценарий нет. Все E2E выполняются
в одном контексте и наборе временных контейнеров. ValidatorFactory GraphQL переиспользуется
внутри тестового класса и закрывается после набора тестов.

Итог расширенного прогона 18.10.2026: 718 unit/компонентных тестов, 11 PostgreSQL,
8 MongoDB и 9 E2E — без ошибок и пропусков. Это ещё 62 случая относительно предыдущего
прогона: 45 unit/компонентных, 12 интеграционных (шесть гарантий в каждой БД), 5 E2E.
JaCoCo для unit/компонентного набора: `GraphQlOperations` — 198/295 строк (67,1%)
вместо 69/295 (23,4%); `ApiAccess` — 46/46 строк. Это не означает полное покрытие
всех возможных последовательностей действий. Полный набор интеграционных тестов других
репозиториев в этом прогоне не запускался.
