package com.ofdun.jobfinder.features.auth.mfa;

import com.ofdun.jobfinder.features.api.ApiFailure;
import com.ofdun.jobfinder.features.auth.domain.model.TokenPair;
import com.ofdun.jobfinder.features.auth.domain.repository.TokenRepository;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.auth.security.JobFinderPrincipal;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/api/v1/auth/security", "/api/v2/auth/security"})
public class AccountSecurityController {
    private final AuthenticationProtection protection;
    private final TokenRepository tokens;

    public AccountSecurityController(AuthenticationProtection protection, TokenRepository tokens) {
        this.protection = protection;
        this.tokens = tokens;
    }

    public record Password(@NotBlank String password) {}

    public record Code(@NotBlank @Size(max = 64) String code) {}

    public record Verify(
            @NotBlank @Size(max = 128) String challengeToken,
            @NotBlank @Size(max = 64) String code) {}

    public record Disable(@NotBlank String password, @NotBlank @Size(max = 64) String code) {}

    public record Change(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 12, max = 72) String newPassword,
            String code) {}

    public record Renew(
            @NotBlank @Size(max = 128) String challengeToken,
            @NotBlank @Size(min = 12, max = 72) String newPassword) {}

    public record Recovery(
            @NotNull AccountType userType,
            @NotBlank @Email String email,
            @NotBlank String password,
            @NotBlank @Size(max = 128) String recoveryCode,
            @NotBlank @Size(min = 12, max = 72) String newPassword,
            Boolean resetAuthenticator) {}

    public record Logout(@NotBlank String refreshToken) {}

    @ModelAttribute
    public void preventCaching(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
    }

    @ExceptionHandler({
        org.springframework.web.bind.MethodArgumentNotValidException.class,
        org.springframework.http.converter.HttpMessageNotReadableException.class
    })
    @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
    public com.ofdun.jobfinder.features.api.ApiErrorResponse invalidRequest() {
        return new com.ofdun.jobfinder.features.api.ApiErrorResponse(
                "Некорректные данные запроса", 400, null, null, java.util.List.of());
    }

    @GetMapping
    public Map<String, Object> status(@AuthenticationPrincipal JobFinderPrincipal principal) {
        var actor = actor(principal);
        return protection.status(actor.accountType(), actor.id());
    }

    @PostMapping("/totp/setup")
    public Map<String, String> setup(
            @AuthenticationPrincipal JobFinderPrincipal principal,
            @Valid @RequestBody Password request) {
        var actor = actor(principal);
        return protection.setup(actor.accountType(), actor.id(), request.password());
    }

    @PostMapping("/totp/enable")
    public Map<String, Object> enable(
            @AuthenticationPrincipal JobFinderPrincipal principal,
            @Valid @RequestBody Code request) {
        var actor = actor(principal);
        return Map.of(
                "recoveryCodes",
                protection.enable(actor.accountType(), actor.id(), request.code()));
    }

    @PostMapping("/totp/verify")
    public TokenPair verify(@Valid @RequestBody Verify request) {
        return protection.verify(request.challengeToken(), request.code());
    }

    @PostMapping("/totp/disable")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void disable(
            @AuthenticationPrincipal JobFinderPrincipal principal,
            @Valid @RequestBody Disable request) {
        var actor = actor(principal);
        protection.disable(actor.accountType(), actor.id(), request.password(), request.code());
    }

    @PostMapping("/password")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void change(
            @AuthenticationPrincipal JobFinderPrincipal principal,
            @Valid @RequestBody Change request) {
        var actor = actor(principal);
        protection.changePassword(
                actor.accountType(),
                actor.id(),
                request.currentPassword(),
                request.newPassword(),
                request.code());
    }

    @PostMapping("/password/renew")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void renew(@Valid @RequestBody Renew request) {
        protection.renewPassword(request.challengeToken(), request.newPassword());
    }

    @PostMapping("/recover")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void recover(@Valid @RequestBody Recovery request) {
        protection.recover(
                request.userType(),
                request.email(),
                request.password(),
                request.recoveryCode(),
                request.newPassword(),
                Boolean.TRUE.equals(request.resetAuthenticator()));
    }

    @PostMapping("/logout")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody Logout request) {
        tokens.deleteToken(request.refreshToken());
    }

    private JobFinderPrincipal actor(JobFinderPrincipal principal) {
        if (principal == null)
            throw new ApiFailure("UNAUTHENTICATED", 401, "Требуется авторизация");
        return principal;
    }
}
