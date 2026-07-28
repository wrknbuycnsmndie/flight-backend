package wrknbuycnsmndie.flight.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.validation.method.ParameterErrors;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<Object> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "Resource Not Found", exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(BusinessValidationException.class)
    ResponseEntity<Object> handleBusinessValidation(
            BusinessValidationException exception,
            HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "Validation Failed", exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(BusinessConflictException.class)
    ResponseEntity<Object> handleBusinessConflict(
            BusinessConflictException exception,
            HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "Conflict", exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Object> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request) {
        return response(
                HttpStatus.CONFLICT,
                "Conflict",
                "The requested data conflicts with an existing record",
                request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Object> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request) {
        logger.error("Unexpected error while processing " + request.getRequestURI(), exception);
        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred",
                request.getRequestURI());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), defaultMessage(error.getDefaultMessage())));
        return response(HttpStatus.BAD_REQUEST, "Validation Failed", "Request contains invalid fields", request, errors);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getParameterValidationResults().forEach(result -> {
            if (result instanceof ParameterErrors parameterErrors) {
                parameterErrors.getFieldErrors().forEach(error ->
                        errors.putIfAbsent(error.getField(), defaultMessage(error.getDefaultMessage())));
                return;
            }
            String field = result.getMethodParameter().getParameterName();
            String message = result.getResolvableErrors().stream()
                    .map(error -> defaultMessage(error.getDefaultMessage()))
                    .findFirst()
                    .orElse("Invalid value");
            errors.put(field == null ? "parameter" : field, message);
        });
        return response(HttpStatus.BAD_REQUEST, "Validation Failed", "Request contains invalid parameters", request, errors);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        return response(HttpStatus.BAD_REQUEST, "Malformed Request", "Request body is invalid", request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            org.springframework.beans.TypeMismatchException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        return response(HttpStatus.BAD_REQUEST, "Invalid Parameter", "Request parameter has an invalid value", request);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            org.springframework.web.servlet.resource.NoResourceFoundException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        return response(HttpStatus.NOT_FOUND, "Resource Not Found", "Resource not found", request);
    }

    @Override
    protected ResponseEntity<Object> createResponseEntity(
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        return super.createResponseEntity(body, headers, statusCode, request);
    }

    private ResponseEntity<Object> response(
            HttpStatus status,
            String title,
            String detail,
            String path) {
        return response(status, title, detail, path, null);
    }

    private ResponseEntity<Object> response(
            HttpStatus status,
            String title,
            String detail,
            WebRequest request) {
        return response(status, title, detail, path(request), null);
    }

    private ResponseEntity<Object> response(
            HttpStatus status,
            String title,
            String detail,
            WebRequest request,
            Map<String, String> errors) {
        return response(status, title, detail, path(request), errors);
    }

    private ResponseEntity<Object> response(
            HttpStatus status,
            String title,
            String detail,
            String path,
            Map<String, String> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("path", path);
        if (errors != null && !errors.isEmpty()) {
            problem.setProperty("errors", errors);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        return new ResponseEntity<>(problem, headers, status);
    }

    private String path(WebRequest request) {
        String description = request.getDescription(false);
        return description.startsWith("uri=") ? description.substring(4) : description;
    }

    private String defaultMessage(String message) {
        return message == null ? "Invalid value" : message;
    }
}
