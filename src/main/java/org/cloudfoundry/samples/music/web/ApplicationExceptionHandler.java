package org.cloudfoundry.samples.music.web;

import org.apache.commons.lang3.StringUtils;
import org.cloudfoundry.samples.music.domain.ApiError;
import org.cloudfoundry.samples.music.errors.ApplicationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class ApplicationExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {ApplicationException.class})
    public ResponseEntity<List<ApiError>> handleValidationFailures(ApplicationException ex) {
        return new ResponseEntity<>(ex.getErrors(), ex.getStatus());
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<List<ApiError>> handleError(Exception ex) {
        List<ApiError> errors = Arrays.asList(new ApiError("operation-failed", null, "an unexpected error occurred"));
        return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<ApiError> errors = Arrays.asList(new ApiError("invalid-request", null, ex.getMessage()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<ApiError> errors = Arrays.asList(new ApiError("resource-not-found", null, null));
        return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<ApiError> errors = Arrays.asList(new ApiError("invalid-method", null, null));
        return new ResponseEntity<>(errors, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<ApiError> errors = Arrays.asList(new ApiError("unsupported-request-media-type", null, ex.getMessage()));
        return new ResponseEntity<>(errors, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        BindingResult bindingResult = ex.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        List<ApiError> apiErrors = fieldErrors.stream()
                .map(this::createApiError)
                .collect(Collectors.toList());
        List<ObjectError> globalErrors = bindingResult.getGlobalErrors();
        apiErrors.addAll(globalErrors.stream()
                .map(e -> new ApiError(e.getCode(), null, e.getDefaultMessage()))
                .collect(Collectors.toList()));

        return new ResponseEntity(apiErrors, status);
    }

    private ApiError createApiError(FieldError fieldError) {
        ApiError apiError;
        if (StringUtils.equals("artist", fieldError.getField())) {
            apiError = new ApiError("missing-information", "artist-required", fieldError.getDefaultMessage());
        } else if (StringUtils.equals("title", fieldError.getField())) {
            apiError = new ApiError("missing-information", "title-required", fieldError.getDefaultMessage());
        } else if (StringUtils.equals("releaseYear", fieldError.getField())) {
            apiError = new ApiError("invalid-release-year", null, fieldError.getDefaultMessage());
        } else {
            apiError = new ApiError("missing-information", null, fieldError.getDefaultMessage());
        }
        return apiError;
    }

}
