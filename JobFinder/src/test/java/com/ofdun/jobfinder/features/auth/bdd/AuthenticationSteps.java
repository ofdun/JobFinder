package com.ofdun.jobfinder.features.auth.bdd;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.auth.mfa.SecurityStateStore;
import com.ofdun.jobfinder.features.auth.mfa.TotpSupport;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public class AuthenticationSteps {
    @LocalServerPort int port;
    @Autowired AuthenticationBddContext.MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired SecurityStateStore states;
    @Autowired StringRedisTemplate redis;
    private final HttpClient http =
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final JsonMapper json = JsonMapper.builder().build();
    private final List<String> challengeTokens = new ArrayList<>();
    private final List<String> refreshTokens = new ArrayList<>();
    private String role;
    private String email;
    private String password;
    private String oldPassword;
    private String secret;
    private String backup;
    private String access;
    private String refresh;
    private String challenge;
    private String acceptedCode;
    private long id;
    private int status;
    private JsonNode response;

    @Before
    public void resetTime() {
        clock.reset();
    }

    @Given("создан технический пользователь с ролью {string} и подключённым аутентификатором")
    public void enrolled(String role) throws Exception {
        registered(role);
        beginSetup();
        confirmSetup();
    }

    @When("пользователь подтверждает подключение аутентификатора")
    public void confirmSetup() throws Exception {
        send("POST", security("/totp/enable"), Map.of("code", code(0)), access);
        expect(200);
        backup = response.get("recoveryCodes").get(0).asText();
        assertEquals(10, response.get("recoveryCodes").size());
        var state = states.get(type(), id);
        assertFalse(
                secret.equals(state.secret),
                "Секрет аутентификатора должен храниться зашифрованным");
        assertFalse(state.recoveryHashes.contains(backup));
        send("GET", profile(), null, access);
        expect(401);
        clock.advance(Duration.ofSeconds(30));
        access = null;
    }

    @Given("создан технический пользователь с ролью {string} без аутентификатора")
    public void registered(String role) throws Exception {
        this.role =
                switch (role) {
                    case "соискатель" -> "applicant";
                    case "работодатель" -> "employer";
                    default -> throw new IllegalArgumentException("Неизвестная роль");
                };
        email = "bdd-" + UUID.randomUUID() + "@example.com";
        String seed = System.getenv("BDD_PASSWORD_SEED");
        password =
                TotpSupport.digest(
                        (seed == null ? AuthenticationBddContext.randomSecret() : seed)
                                + UUID.randomUUID());
        send(
                "POST",
                "/api/v2/" + this.role + "s",
                Map.of(
                        "name",
                        "Тестовый пользователь",
                        "email",
                        email,
                        "password",
                        password,
                        "locationId",
                        1),
                null);
        expect(201);
        id = response.asLong();
        correctPassword();
        expect(200);
        rememberTokens();
    }

    @When("пользователь начинает подключение аутентификатора")
    public void beginSetup() throws Exception {
        send("POST", security("/totp/setup"), Map.of("password", password), access);
        expect(200);
        secret = response.get("secret").asText();
        assertTrue(response.get("qrCode").asText().startsWith("data:image/png;base64,"));
    }

    @Then("неверный код подтверждения не включает аутентификатор")
    public void invalidSetup() throws Exception {
        send("POST", security("/totp/enable"), Map.of("code", "invalid"), access);
        expect(401);
        assertNull(states.get(type(), id).secret);
    }

    @Then("просроченное подключение нельзя подтвердить")
    public void expiredSetup() throws Exception {
        clock.advance(Duration.ofMinutes(11));
        send("POST", security("/totp/enable"), Map.of("code", code(0)), access);
        expect(401);
        assertNull(states.get(type(), id).secret);
    }

    @io.cucumber.java.AfterAll
    public static void stopInfrastructure() {
        try {
            AuthenticationBddContext.postgres.stop();
        } finally {
            try {
                AuthenticationBddContext.redis.stop();
            } finally {
                try {
                    AuthenticationBddContext.mongo.stop();
                } finally {
                    AuthenticationBddContext.qdrant.stop();
                }
            }
        }
    }

    @When("пользователь вводит правильный пароль")
    @When("пользователь снова вводит правильный пароль")
    public void correctPassword() throws Exception {
        send(
                "POST",
                "/api/v2/auth/login",
                Map.of("email", email, "password", password, "userType", type().name()),
                null);
        if (status == 202) {
            challenge = response.get("challengeToken").asText();
            challengeTokens.add(challenge);
        }
    }

    @Then("сервер возвращает только запрос второго фактора")
    public void totpRequired() {
        expect(202);
        assertEquals("TOTP", response.get("nextStep").asText());
        assertFalse(response.has("accessToken"));
        assertFalse(response.has("refreshToken"));
    }

    @Then("запрос подтверждения не позволяет открыть профиль")
    public void challengeHasNoAccess() throws Exception {
        send("GET", profile(), null, challenge);
        expect(401);
        send(
                "POST",
                "/api/v2/auth/refresh",
                Map.of("userType", type().name(), "refreshToken", challenge),
                null);
        expect(401);
    }

    @When("пользователь вводит действующий код из аутентификатора")
    public void validCode() throws Exception {
        acceptedCode = code(0);
        send(
                "POST",
                security("/totp/verify"),
                Map.of("challengeToken", challenge, "code", acceptedCode),
                null);
        if (status == 200) rememberTokens();
        if (status == 202) {
            challenge = response.get("challengeToken").asText();
            challengeTokens.add(challenge);
        }
    }

    @Then("пользователь получает доступ к своему профилю")
    public void profileAvailable() throws Exception {
        send("GET", profile(), null, access);
        expect(200);
        assertEquals(email, response.get("email").asText());
        send("GET", profile(), null, refresh);
        expect(401);
        send("GET", profile().replace("/v2/", "/v1/"), null, refresh);
        expect(403);
    }

    @Then("завершённый запрос подтверждения нельзя использовать повторно")
    public void usedChallenge() throws Exception {
        clock.advance(Duration.ofSeconds(30));
        validCode();
        expect(401);
    }

    @When("пользователь вводит неверный код из аутентификатора")
    public void invalidCode() throws Exception {
        send(
                "POST",
                security("/totp/verify"),
                Map.of("challengeToken", challenge, "code", "invalid"),
                null);
    }

    @When("пользователь вводит просроченный код из аутентификатора")
    public void expiredCode() throws Exception {
        send(
                "POST",
                security("/totp/verify"),
                Map.of("challengeToken", challenge, "code", code(-10)),
                null);
    }

    @Then("сервер отклоняет аутентификацию")
    public void rejected() {
        expect(401);
    }

    @When("пользователь входит с обоими факторами")
    public void signIn() throws Exception {
        correctPassword();
        totpRequired();
        validCode();
        expect(200);
    }

    @When("пользователь повторяет уже принятый код аутентификатора")
    public void replayCode() throws Exception {
        send(
                "POST",
                security("/totp/verify"),
                Map.of("challengeToken", challenge, "code", acceptedCode),
                null);
    }

    @When("пользователь исчерпывает лимит кодов аутентификатора в одном запросе входа")
    public void lockTotpChallenge() throws Exception {
        correctPassword();
        totpRequired();
        for (int attempt = 1; attempt <= 3; attempt++) {
            invalidCode();
            expect(attempt < 3 ? 401 : 423);
        }
    }

    @When("посторонний многократно вводит неверный пароль через публичные маршруты входа")
    public void wrongPasswordsAcrossRoutes() throws Exception {
        var before = SecurityStateStore.encode(states.get(type(), id));
        for (int attempt = 0; attempt < 4; attempt++) {
            String wrongPassword = AuthenticationBddContext.randomSecret();
            for (String version : List.of("v1", "v2")) {
                send(
                        "POST",
                        "/api/" + version + "/auth/" + role + "/login",
                        Map.of("email", email, "password", wrongPassword),
                        null);
                expect(401);
            }
            send(
                    "POST",
                    "/api/v2/auth/login",
                    Map.of("email", email, "password", wrongPassword, "userType", type().name()),
                    null);
            expect(401);
            send(
                    "POST",
                    "/api/v2",
                    Map.of(
                            "operationName",
                            "Login",
                            "query",
                            "mutation Login($input: LoginInput!) { login(input: $input) {"
                                    + " accessToken refreshToken challengeToken nextStep } }",
                            "variables",
                            Map.of(
                                    "input",
                                    Map.of(
                                            "accountType",
                                            type().name(),
                                            "email",
                                            email,
                                            "password",
                                            wrongPassword))),
                    null);
            expect(200);
            assertTrue(response.has("errors"));
            assertTrue(
                    response.path("data").path("login").isMissingNode()
                            || response.path("data").path("login").isNull());
        }
        assertEquals(before, SecurityStateStore.encode(states.get(type(), id)));
    }

    @When("посторонний многократно передаёт код без действительного запроса второго фактора")
    public void invalidChallengesDoNotLock() throws Exception {
        var before = SecurityStateStore.encode(states.get(type(), id));
        for (int attempt = 0; attempt < 10; attempt++) {
            send(
                    "POST",
                    security("/totp/verify"),
                    Map.of(
                            "challengeToken",
                            AuthenticationBddContext.randomSecret(),
                            "code",
                            "123456"),
                    null);
            expect(401);
        }
        assertEquals(before, SecurityStateStore.encode(states.get(type(), id)));
    }

    @Then("аккаунт не заблокирован и действующая сессия сохранена")
    public void accountAndSessionRemainAvailable() throws Exception {
        var state = states.get(type(), id);
        assertFalse(state.locked);
        assertEquals(0, state.lockedUntil);
        assertEquals(0, state.failures);
        profileAvailable();
        rotateRefresh();
        profileAvailable();
    }

    @Then("вход заблокирован даже с правильным паролем")
    public void correctBlocked() throws Exception {
        correctPassword();
        expect(423);
    }

    @When("пользователь многократно ошибается в текущем пароле при подключении аутентификатора")
    public void wrongPasswordsDuringSetup() throws Exception {
        for (int attempt = 0; attempt < 10; attempt++) {
            send(
                    "POST",
                    security("/totp/setup"),
                    Map.of("password", AuthenticationBddContext.randomSecret()),
                    access);
            expect(401);
            assertTrue(response.get("description").asText().contains("текущий пароль"));
        }
    }

    @When("пользователь исчерпывает попытки кода при подключении аутентификатора")
    public void lockDuringConfirmation() throws Exception {
        for (int attempt = 1; attempt <= 3; attempt++) {
            send("POST", security("/totp/enable"), Map.of("code", "invalid"), access);
            expect(attempt < 3 ? 401 : 423);
        }
    }

    @Then("сервер сообщает о временной блокировке без требования резервного кода")
    public void temporaryLockExplained() {
        lockHasDeadline();
        assertTrue(states.get(type(), id).recoveryHashes.isEmpty());
    }

    @Then("срок блокировки составляет 900 секунд")
    public void lockHasDeadline() {
        expect(423);
        assertTrue(response.get("description").asText().contains("900 сек."));
        var state = states.get(type(), id);
        assertTrue(state.locked);
        assertEquals(clock.millis() + 900_000, state.lockedUntil);
    }

    @Then("повторные отправки кода сохраняют сообщение и срок блокировки")
    public void repeatedCodesRemainLocked() throws Exception {
        long deadline = states.get(type(), id).lockedUntil;
        assertTrue(deadline > clock.millis());
        String message = null;
        for (int attempt = 0; attempt < 10; attempt++) {
            invalidCode();
            expect(423);
            String current = response.get("description").asText();
            assertTrue(current.contains("Повторите вход через"));
            if (message != null) assertEquals(message, current);
            message = current;
            assertEquals(deadline, states.get(type(), id).lockedUntil);
            assertEquals(3, states.get(type(), id).failures);
        }
    }

    @Then("прежний запрос второго фактора не работает после окончания блокировки")
    public void staleChallengeAfterLock() throws Exception {
        validCode();
        expect(401);
        assertFalse(states.get(type(), id).locked);
        assertEquals(0, states.get(type(), id).lockedUntil);
        assertEquals(0, states.get(type(), id).failures);
    }

    @When("проходит {int} секунд(а)")
    public void timePasses(int seconds) {
        clock.advance(Duration.ofSeconds(seconds));
    }

    @Then("после паузы пароль снова проверяется, старые сессии остаются отозванными")
    public void loginAfterTemporaryLock() throws Exception {
        String previousAccess = access;
        String previousRefresh = refresh;
        send(
                "POST",
                "/api/v2/auth/login",
                Map.of(
                        "email",
                        email,
                        "password",
                        AuthenticationBddContext.randomSecret(),
                        "userType",
                        type().name()),
                null);
        expect(401);
        passwordOnly();
        assertEquals(0, states.get(type(), id).failures);
        assertFalse(states.get(type(), id).locked);
        assertEquals(0, states.get(type(), id).lockedUntil);
        send("GET", profile(), null, previousAccess);
        expect(401);
        send(
                "POST",
                "/api/v2/auth/refresh",
                Map.of("userType", type().name(), "refreshToken", previousRefresh),
                null);
        expect(401);
    }

    @Given("у пользователя сохранена старая блокировка без срока окончания")
    public void legacyLock() {
        states.update(
                type(),
                id,
                state -> {
                    state.locked = true;
                    state.failures = 3;
                    state.lockedUntil = 0;
                    state.sessionVersion++;
                    return null;
                });
    }

    @When("пользователь восстанавливает доступ с паролем и резервным кодом")
    public void recover() throws Exception {
        oldPassword = password;
        String replacement = AuthenticationBddContext.randomSecret();
        send(
                "POST",
                security("/recover"),
                Map.of(
                        "userType",
                        type().name(),
                        "email",
                        email,
                        "password",
                        password,
                        "recoveryCode",
                        backup,
                        "newPassword",
                        replacement),
                null);
        expect(204);
        password = replacement;
    }

    @When("пользователь восстанавливает доступ после потери телефона")
    public void recoverLostAuthenticator() throws Exception {
        oldPassword = password;
        String replacement = AuthenticationBddContext.randomSecret();
        send(
                "POST",
                security("/recover"),
                Map.of(
                        "userType",
                        type().name(),
                        "email",
                        email,
                        "password",
                        password,
                        "recoveryCode",
                        backup,
                        "newPassword",
                        replacement,
                        "resetAuthenticator",
                        true),
                null);
        expect(204);
        password = replacement;
    }

    @Then("пользователь входит с новым паролем и аутентификатором")
    public void newPasswordWorks() throws Exception {
        clock.advance(Duration.ofSeconds(30));
        signIn();
        profileAvailable();
    }

    @Then("старый пароль и использованный резервный код больше не принимаются")
    public void recoveryNotReusable() throws Exception {
        oldPasswordRejected();
        send(
                "POST",
                security("/recover"),
                Map.of(
                        "userType",
                        type().name(),
                        "email",
                        email,
                        "password",
                        password,
                        "recoveryCode",
                        backup,
                        "newPassword",
                        AuthenticationBddContext.randomSecret()),
                null);
        expect(401);
    }

    @When("пользователь исчерпывает лимит кодов аутентификатора через новые запросы входа")
    public void lockTotp() throws Exception {
        for (int attempt = 1; attempt <= 3; attempt++) {
            correctPassword();
            totpRequired();
            invalidCode();
            expect(attempt < 3 ? 401 : 423);
        }
    }

    @Then("слишком короткий пароль отклоняется без раскрытия секретов")
    public void invalidPasswordIsPrivate() throws Exception {
        String replacement = AuthenticationBddContext.randomSecret().substring(0, 8);
        send(
                "POST",
                "/api/v1/auth/security/password",
                Map.of("currentPassword", password, "newPassword", replacement, "code", code(0)),
                access);
        expect(400);
        assertFalse(response.toString().contains(password));
        assertFalse(response.toString().contains(replacement));
        profileAvailable();
    }

    @When("пользователь меняет пароль с новым кодом аутентификатора")
    public void changePassword() throws Exception {
        clock.advance(Duration.ofSeconds(30));
        oldPassword = password;
        String replacement = AuthenticationBddContext.randomSecret();
        send(
                "POST",
                security("/password"),
                Map.of("currentPassword", password, "newPassword", replacement, "code", code(0)),
                access);
        expect(204);
        password = replacement;
    }

    @Then("старые токены доступа и обновления больше не принимаются")
    public void revoked() throws Exception {
        send("GET", profile(), null, access);
        expect(401);
        send(
                "POST",
                "/api/v2/auth/refresh",
                Map.of("userType", type().name(), "refreshToken", refresh),
                null);
        expect(401);
    }

    @Then("старый пароль больше не принимается")
    public void oldPasswordRejected() throws Exception {
        send(
                "POST",
                "/api/v2/auth/login",
                Map.of("email", email, "password", oldPassword, "userType", type().name()),
                null);
        expect(401);
    }

    @Given("срок действия пароля истёк")
    public void agePassword() {
        clock.advance(Duration.ofDays(91));
    }

    @Then("сервер возвращает только запрос смены пароля")
    public void passwordRequired() {
        expect(202);
        assertEquals("PASSWORD_CHANGE", response.get("nextStep").asText());
        assertFalse(response.has("accessToken"));
    }

    @When("пользователь заменяет просроченный пароль")
    public void renew() throws Exception {
        oldPassword = password;
        String replacement = AuthenticationBddContext.randomSecret();
        send(
                "POST",
                security("/password/renew"),
                Map.of("challengeToken", challenge, "newPassword", replacement),
                null);
        expect(204);
        send(
                "POST",
                security("/password/renew"),
                Map.of("challengeToken", challenge, "newPassword", oldPassword),
                null);
        expect(401);
        password = replacement;
    }

    @Then("каждый маршрут входа требует второй фактор")
    public void loginRoutes() throws Exception {
        for (String version : List.of("v1", "v2")) {
            send(
                    "POST",
                    "/api/" + version + "/auth/" + role + "/login",
                    Map.of("email", email, "password", password),
                    null);
            totpRequired();
            challengeTokens.add(response.get("challengeToken").asText());
        }
        correctPassword();
        totpRequired();
    }

    @Then("редактирование профиля не позволяет обойти защищённую смену пароля")
    public void noPasswordBypass() throws Exception {
        signIn();
        send(
                "PUT",
                profile(),
                Map.of(
                        "name",
                        "BDD User",
                        "email",
                        email,
                        "locationId",
                        1,
                        "password",
                        AuthenticationBddContext.randomSecret()),
                access);
        expect(409);
    }

    @Then("обновление сессии заменяет токен обновления")
    public void rotateRefresh() throws Exception {
        String previous = refresh;
        send(
                "POST",
                "/api/v2/auth/refresh",
                Map.of("userType", type().name(), "refreshToken", previous),
                null);
        expect(200);
        rememberTokens();
        assertFalse(previous.equals(refresh), "Токен обновления должен заменяться");
        send(
                "POST",
                "/api/v2/auth/refresh",
                Map.of("userType", type().name(), "refreshToken", previous),
                null);
        expect(401);
    }

    @Then("после выхода сессию нельзя обновить")
    public void logout() throws Exception {
        send("POST", security("/logout"), Map.of("refreshToken", refresh), null);
        expect(204);
        send(
                "POST",
                "/api/v2/auth/refresh",
                Map.of("userType", type().name(), "refreshToken", refresh),
                null);
        expect(401);
    }

    @After
    public void cleanup() {
        try {
            if (email != null) {
                jdbc.update("DELETE FROM jobfinder." + role + "s WHERE email = ?", email);
                assertEquals(
                        0,
                        jdbc.queryForObject(
                                "SELECT COUNT(*) FROM jobfinder." + role + "s WHERE email = ?",
                                Integer.class,
                                email));
            }
        } finally {
            if (id > 0) {
                states.delete(states.key(type(), id));
                redis.delete("auth:recovery:" + states.key(type(), id));
                assertEquals(-1, states.read(states.key(type(), id)).version());
            }
            for (String token : challengeTokens)
                redis.delete("auth:challenge:" + TotpSupport.digest(token));
            for (String token : refreshTokens) redis.delete(token);
            password = null;
            oldPassword = null;
            secret = null;
            backup = null;
            access = null;
            refresh = null;
            clock.reset();
        }
    }

    @When("срок действия запроса подтверждения истекает")
    public void expireChallenge() {
        String key = "auth:challenge:" + TotpSupport.digest(challenge);
        Long ttl = redis.getExpire(key);
        assertTrue(ttl != null && ttl > 0 && ttl <= 300);
        redis.expire(key, Duration.ZERO);
    }

    @Then("восстановление с неверными данными не снимает блокировку")
    public void invalidRecovery() throws Exception {
        for (int attempt = 0; attempt < 13; attempt++) {
            boolean wrongPassword = attempt < 12;
            send(
                    "POST",
                    security("/recover"),
                    Map.of(
                            "userType",
                            type().name(),
                            "email",
                            email,
                            "password",
                            wrongPassword ? AuthenticationBddContext.randomSecret() : password,
                            "recoveryCode",
                            wrongPassword ? backup : AuthenticationBddContext.randomSecret(),
                            "newPassword",
                            AuthenticationBddContext.randomSecret()),
                    null);
            expect(401);
            correctBlocked();
        }
    }

    @Then("из двух одновременных подтверждений успешно только одно")
    public void concurrentCode() throws Exception {
        correctPassword();
        String first = challenge;
        correctPassword();
        String code = code(0);
        var requests =
                new ArrayList<java.util.concurrent.CompletableFuture<HttpResponse<String>>>();
        for (String token : List.of(first, challenge)) {
            var request =
                    HttpRequest.newBuilder(
                                    URI.create(
                                            "http://localhost:" + port + security("/totp/verify")))
                            .header("Content-Type", "application/json")
                            .timeout(Duration.ofSeconds(20))
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(
                                            json.writeValueAsString(
                                                    Map.of("challengeToken", token, "code", code))))
                            .build();
            requests.add(http.sendAsync(request, HttpResponse.BodyHandlers.ofString()));
        }
        List<Integer> statuses = new ArrayList<>();
        for (var pending : requests) {
            var result = pending.get(30, java.util.concurrent.TimeUnit.SECONDS);
            statuses.add(result.statusCode());
            if (result.statusCode() == 200)
                refreshTokens.add(json.readTree(result.body()).get("refreshToken").asText());
        }
        assertEquals(List.of(200, 401), statuses.stream().sorted().toList());
    }

    @Then("неверный код не позволяет отключить аутентификатор")
    public void invalidDisable() throws Exception {
        send(
                "POST",
                security("/totp/disable"),
                Map.of("password", password, "code", "invalid"),
                access);
        expect(401);
        assertNotNull(states.get(type(), id).secret);
    }

    @When("пользователь отключает аутентификатор с новым кодом")
    public void disable() throws Exception {
        clock.advance(Duration.ofSeconds(30));
        send(
                "POST",
                security("/totp/disable"),
                Map.of("password", password, "code", code(0)),
                access);
        expect(204);
        assertTrue(states.get(type(), id).recoveryHashes.isEmpty());
    }

    @Then("пользователь может войти без второго фактора")
    public void passwordOnly() throws Exception {
        correctPassword();
        expect(200);
        rememberTokens();
        profileAvailable();
    }

    @Then("GraphQL запрашивает и проверяет второй фактор")
    public void graphQl() throws Exception {
        send(
                "POST",
                "/api/v2",
                Map.of(
                        "operationName",
                        "Login",
                        "query",
                        "mutation Login($input: LoginInput!) { login(input: $input) { accessToken"
                                + " refreshToken challengeToken nextStep } }",
                        "variables",
                        Map.of(
                                "input",
                                Map.of(
                                        "accountType",
                                        type().name(),
                                        "email",
                                        email,
                                        "password",
                                        password))),
                null);
        expect(200);
        assertFalse(response.has("errors"), "Вход через GraphQL должен быть успешным");
        var login = response.get("data").get("login");
        assertEquals("TOTP", login.get("nextStep").asText());
        assertTrue(login.get("accessToken").isNull());
        challenge = login.get("challengeToken").asText();
        challengeTokens.add(challenge);
        send(
                "POST",
                "/api/v2",
                Map.of(
                        "operationName",
                        "Verify",
                        "query",
                        "mutation Verify($token: String!, $code: String!) {"
                                + " verifyTotp(challengeToken: $token, code: $code) { accessToken"
                                + " refreshToken nextStep } }",
                        "variables",
                        Map.of("token", challenge, "code", code(0))),
                null);
        expect(200);
        assertFalse(response.has("errors"), "Подтверждение через GraphQL должно быть успешным");
        response = response.get("data").get("verifyTotp");
        rememberTokens();
        profileAvailable();
    }

    private void rememberTokens() {
        access = response.get("accessToken").asText();
        refresh = response.get("refreshToken").asText();
        refreshTokens.add(refresh);
    }

    private String code(int offset) throws Exception {
        return new DefaultCodeGenerator().generate(secret, clock.millis() / 30_000 + offset);
    }

    private AccountType type() {
        return AccountType.valueOf(role.toUpperCase(java.util.Locale.ROOT));
    }

    private String security(String path) {
        return "/api/v2/auth/security" + path;
    }

    private String profile() {
        return "/api/v2/" + role + "s/" + id;
    }

    private void expect(int expected) {
        assertEquals(expected, status, "Неожиданный HTTP-статус");
    }

    private void send(String method, String path, Object payload, String bearer) throws Exception {
        var builder =
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                        .timeout(Duration.ofSeconds(20));
        if (bearer != null) builder.header("Authorization", "Bearer " + bearer);
        builder.header("Content-Type", "application/json");
        builder.method(
                method,
                payload == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload)));
        var result = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        status = result.statusCode();
        response = result.body().isBlank() ? json.createObjectNode() : json.readTree(result.body());
    }
}
