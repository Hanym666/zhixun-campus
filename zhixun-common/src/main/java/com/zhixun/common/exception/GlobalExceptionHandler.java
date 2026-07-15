package com.zhixun.common.exception;

import com.zhixun.common.api.ApiResponse;
import com.zhixun.common.api.CommonResultCode;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        return ResponseEntity
                .status(resolveHttpStatus(exception.getHttpStatus()))
                .body(ApiResponse.failure(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception
    ) {
        return validationFailure(formatFieldErrors(exception.getBindingResult().getFieldErrors()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException exception) {
        return validationFailure(formatFieldErrors(exception.getBindingResult().getFieldErrors()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException exception
    ) {
        String message = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        return validationFailure(message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableMessage(
            HttpMessageNotReadableException exception
    ) {
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.failure(CommonResultCode.BAD_REQUEST, "请求体格式错误"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        log.error("Unhandled server exception", exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure(CommonResultCode.INTERNAL_ERROR));
    }

    private ResponseEntity<ApiResponse<Void>> validationFailure(String message) {
        String finalMessage = message == null || message.isBlank()
                ? CommonResultCode.VALIDATION_ERROR.message()
                : message;
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.failure(CommonResultCode.VALIDATION_ERROR, finalMessage));
    }

    private String formatFieldErrors(java.util.List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .map(error -> error.getField() + ": "
                        + Objects.requireNonNullElse(error.getDefaultMessage(), "参数不合法"))
                .distinct()
                .collect(Collectors.joining("; "));
    }

    private HttpStatus resolveHttpStatus(int status) {
        HttpStatus resolved = HttpStatus.resolve(status);
        return resolved == null ? HttpStatus.BAD_REQUEST : resolved;
    }
}
