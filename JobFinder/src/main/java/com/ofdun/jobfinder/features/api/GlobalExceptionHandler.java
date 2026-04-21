package com.ofdun.jobfinder.features.api;

import com.ofdun.jobfinder.features.applicant.exception.ApplicantAlreadyExistsException;
import com.ofdun.jobfinder.features.applicant.exception.ApplicantNotFoundException;
import com.ofdun.jobfinder.features.application.exception.ApplicationAlreadyExistsException;
import com.ofdun.jobfinder.features.application.exception.ApplicationNotFoundException;
import com.ofdun.jobfinder.features.auth.exception.InvalidPasswordException;
import com.ofdun.jobfinder.features.auth.exception.InvalidRefreshTokenException;
import com.ofdun.jobfinder.features.auth.exception.SessionIsOverException;
import com.ofdun.jobfinder.features.category.exception.CategoryNotFoundException;
import com.ofdun.jobfinder.features.clients.ai.exception.AiEmptyRespondException;
import com.ofdun.jobfinder.features.employer.exception.EmployerAlreadyExistsException;
import com.ofdun.jobfinder.features.employer.exception.EmployerNotFoundException;
import com.ofdun.jobfinder.features.language.exception.LanguageNotFoundException;
import com.ofdun.jobfinder.features.location.exception.LocationNotFoundException;
import com.ofdun.jobfinder.features.resume.exception.FailedToCreateResumeException;
import com.ofdun.jobfinder.features.resume.exception.ResumeNotFoundException;
import com.ofdun.jobfinder.features.skill.exception.SkillNotFoundException;
import com.ofdun.jobfinder.features.vacancy.exception.VacancyAlreadyExistsException;
import com.ofdun.jobfinder.features.vacancy.exception.VacancyNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiErrorResponse handleAuthorizationDenied(AuthorizationDeniedException ex) {
        return createErrorResponse("Access denied", HttpStatus.FORBIDDEN, "AUTH_FORBIDDEN", ex);
    }

    @ExceptionHandler(LocationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleLocationNotFound(LocationNotFoundException ex) {
        return createErrorResponse(
                "Location not found", HttpStatus.NOT_FOUND, "LOCATION_NOT_FOUND", ex);
    }

    @ExceptionHandler(AiEmptyRespondException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleAiEmptyRespond(AiEmptyRespondException ex) {
        return createErrorResponse(
                "AI service returned an empty response",
                HttpStatus.INTERNAL_SERVER_ERROR,
                "AI_EMPTY_RESPONSE",
                ex);
    }

    @ExceptionHandler(SessionIsOverException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleSessionIsOver(SessionIsOverException ex) {
        return createErrorResponse(
                "Session is over", HttpStatus.UNAUTHORIZED, "SESSION_IS_OVER", ex);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return createErrorResponse(
                "Invalid refresh token", HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", ex);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleInvalidPassword(InvalidPasswordException ex) {
        return createErrorResponse(
                "Invalid password", HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD", ex);
    }

    @ExceptionHandler(VacancyNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleVacancyNotFound(VacancyNotFoundException ex) {
        return createErrorResponse(
                "Vacancy not found", HttpStatus.NOT_FOUND, "VACANCY_NOT_FOUND", ex);
    }

    @ExceptionHandler(VacancyAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleVacancyAlreadyExists(VacancyAlreadyExistsException ex) {
        return createErrorResponse(
                "Vacancy already exists", HttpStatus.CONFLICT, "VACANCY_ALREADY_EXISTS", ex);
    }

    @ExceptionHandler(EmployerNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleEmployerNotFound(EmployerNotFoundException ex) {
        return createErrorResponse(
                "Employer not found", HttpStatus.NOT_FOUND, "EMPLOYER_NOT_FOUND", ex);
    }

    @ExceptionHandler(EmployerAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleEmployerAlreadyExists(EmployerAlreadyExistsException ex) {
        return createErrorResponse(
                "Employer already exists", HttpStatus.CONFLICT, "EMPLOYER_ALREADY_EXISTS", ex);
    }

    @ExceptionHandler(LanguageNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleLanguageNotFound(LanguageNotFoundException ex) {
        return createErrorResponse(
                "Language not found", HttpStatus.NOT_FOUND, "LANGUAGE_NOT_FOUND", ex);
    }

    @ExceptionHandler(SkillNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleSkillNotFound(SkillNotFoundException ex) {
        return createErrorResponse("Skill not found", HttpStatus.NOT_FOUND, "SKILL_NOT_FOUND", ex);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleCategoryNotFound(CategoryNotFoundException ex) {
        return createErrorResponse(
                "Category not found", HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", ex);
    }

    @ExceptionHandler(ApplicantNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleApplicantNotFound(ApplicantNotFoundException ex) {
        return createErrorResponse(
                "Applicant not found", HttpStatus.NOT_FOUND, "APPLICANT_NOT_FOUND", ex);
    }

    @ExceptionHandler(ApplicationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleApplicationNotFound(ApplicationNotFoundException ex) {
        return createErrorResponse(
                "Application not found", HttpStatus.NOT_FOUND, "APPLICATION_NOT_FOUND", ex);
    }

    @ExceptionHandler(ApplicationAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleApplicationAlreadyExists(ApplicationAlreadyExistsException ex) {
        return createErrorResponse(
                "Application already exists",
                HttpStatus.CONFLICT,
                "APPLICATION_ALREADY_EXISTS",
                ex);
    }

    @ExceptionHandler(ApplicantAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleApplicantAlreadyExists(ApplicantAlreadyExistsException ex) {
        return createErrorResponse(
                "Applicant already exists", HttpStatus.CONFLICT, "APPLICANT_ALREADY_EXISTS", ex);
    }

    @ExceptionHandler(ResumeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleResumeNotFound(ResumeNotFoundException ex) {
        return createErrorResponse(
                "Resume not found", HttpStatus.NOT_FOUND, "RESUME_NOT_FOUND", ex);
    }

    @ExceptionHandler(FailedToCreateResumeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleFailedToCreateResume(FailedToCreateResumeException ex) {
        return createErrorResponse(
                "Failed to create resume",
                HttpStatus.INTERNAL_SERVER_ERROR,
                "RESUME_CREATE_FAILED",
                ex);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleIllegalArgument(IllegalArgumentException ex) {
        return createErrorResponse(
                "Invalid request parameters", HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        return createErrorResponse(
                "Validation failed", HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", ex);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleConstraintViolation(ConstraintViolationException ex) {
        return createErrorResponse(
                "Validation failed", HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", ex);
    }

    @ExceptionHandler(RedisConnectionFailureException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiErrorResponse handleRedisConnectionFailure(RedisConnectionFailureException ex) {
        return createErrorResponse(
                "Redis is unavailable (refresh token storage)",
                HttpStatus.SERVICE_UNAVAILABLE,
                "REDIS_UNAVAILABLE",
                ex);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleGenericException(Exception ex) {
        return createErrorResponse(
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR,
                "UNEXPECTED_ERROR",
                ex);
    }

    private ApiErrorResponse createErrorResponse(
            String message, HttpStatus status, String errorCode, Exception ex) {
        logBusinessException(message, status, errorCode, ex);
        List<String> stackTrace =
                Arrays.stream(ex.getStackTrace()).map(StackTraceElement::toString).toList();
        return new ApiErrorResponse(
                message,
                status.value(),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                stackTrace);
    }

    private void logBusinessException(
            String message, HttpStatus status, String errorCode, Exception ex) {
        MDC.put("error.code", errorCode);
        MDC.put("error.type", ex.getClass().getSimpleName());
        MDC.put("http.status", String.valueOf(status.value()));

        try {
            if (status.is5xxServerError()) {
                log.error("Business exception handled: {}", message, ex);
                return;
            }
            log.warn("Business exception handled: {} ({})", message, ex.getMessage());
        } finally {
            MDC.remove("error.code");
            MDC.remove("error.type");
            MDC.remove("http.status");
        }
    }
}
