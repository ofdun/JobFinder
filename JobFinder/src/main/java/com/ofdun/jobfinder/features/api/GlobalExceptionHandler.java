package com.ofdun.jobfinder.features.api;

import com.ofdun.jobfinder.features.applicant.exception.ApplicantAlreadyExistsException;
import com.ofdun.jobfinder.features.applicant.exception.ApplicantNotFoundException;
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
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiErrorResponse handleAuthorizationDenied(AuthorizationDeniedException ex) {
        return createErrorResponse("Access denied", HttpStatus.FORBIDDEN.value(), ex);
    }

    @ExceptionHandler(LocationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleLocationNotFound(LocationNotFoundException ex) {
        return createErrorResponse("Location not found", HttpStatus.NOT_FOUND.value(), ex);
    }

    @ExceptionHandler(AiEmptyRespondException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleAiEmptyRespond(AiEmptyRespondException ex) {
        return createErrorResponse("AI service returned an empty response", HttpStatus.INTERNAL_SERVER_ERROR.value(), ex);
    }

    @ExceptionHandler(SessionIsOverException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleSessionIsOver(SessionIsOverException ex) {
        return createErrorResponse("Session is over", HttpStatus.UNAUTHORIZED.value(), ex);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return createErrorResponse("Invalid refresh token", HttpStatus.UNAUTHORIZED.value(), ex);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleInvalidPassword(InvalidPasswordException ex) {
        return createErrorResponse("Invalid password", HttpStatus.UNAUTHORIZED.value(), ex);
    }

    @ExceptionHandler(VacancyNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleVacancyNotFound(VacancyNotFoundException ex) {
        return createErrorResponse("Vacancy not found", HttpStatus.NOT_FOUND.value(), ex);
    }

    @ExceptionHandler(VacancyAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleVacancyAlreadyExists(VacancyAlreadyExistsException ex) {
        return createErrorResponse("Vacancy already exists", HttpStatus.CONFLICT.value(), ex);
    }

    @ExceptionHandler(EmployerNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleEmployerNotFound(EmployerNotFoundException ex) {
        return createErrorResponse("Employer not found", HttpStatus.NOT_FOUND.value(), ex);
    }

    @ExceptionHandler(EmployerAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleEmployerAlreadyExists(EmployerAlreadyExistsException ex) {
        return createErrorResponse("Employer already exists", HttpStatus.CONFLICT.value(), ex);
    }

    @ExceptionHandler(LanguageNotFoundException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleLanguageNotFound(LanguageNotFoundException ex) {
        return createErrorResponse(
                "Language not found",
                HttpStatus.NOT_FOUND.value(),
                ex
        );
    }

    @ExceptionHandler(SkillNotFoundException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleSkillNotFound(SkillNotFoundException ex) {
        return createErrorResponse(
                "Skill not found",
                HttpStatus.NOT_FOUND.value(),
                ex
        );
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleCategoryNotFound(CategoryNotFoundException ex) {
        return createErrorResponse(
                "Category not found",
                HttpStatus.NOT_FOUND.value(),
                ex
        );
    }

    @ExceptionHandler(ApplicantNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleApplicantNotFound(ApplicantNotFoundException ex) {
        return createErrorResponse(
                "Applicant not found",
                HttpStatus.NOT_FOUND.value(),
                ex
        );
    }

    @ExceptionHandler(ApplicantAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleApplicantAlreadyExists(ApplicantAlreadyExistsException ex) {
        return createErrorResponse(
                "Applicant already exists",
                HttpStatus.CONFLICT.value(),
                ex
        );
    }

    @ExceptionHandler(ResumeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleResumeNotFound(ResumeNotFoundException ex) {
        return createErrorResponse("Resume not found", HttpStatus.NOT_FOUND.value(), ex);
    }

    @ExceptionHandler(FailedToCreateResumeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleFailedToCreateResume(FailedToCreateResumeException ex) {
        return createErrorResponse("Failed to create resume", HttpStatus.INTERNAL_SERVER_ERROR.value(), ex);
    }

    @ExceptionHandler(RedisConnectionFailureException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiErrorResponse handleRedisConnectionFailure(RedisConnectionFailureException ex) {
        return createErrorResponse(
                "Redis is unavailable (refresh token storage)",
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                ex);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleGenericException(Exception ex) {
        return createErrorResponse(
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex
        );
    }

    private ApiErrorResponse createErrorResponse(String message, Integer code, Exception ex) {
        List<String> stackTrace = Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .toList();
        return new ApiErrorResponse(
                message,
                code,
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                stackTrace);
    }
}
