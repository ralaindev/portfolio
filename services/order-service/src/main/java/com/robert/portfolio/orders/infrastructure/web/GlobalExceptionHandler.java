package com.robert.portfolio.orders.infrastructure.web;

import com.robert.portfolio.orders.domain.exception.IdempotencyConflictException;
import com.robert.portfolio.orders.domain.exception.InvalidOrderException;
import com.robert.portfolio.orders.domain.exception.OrderNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Request validation failed",
                "One or more request fields are invalid", request);
        List<FieldErrorResponse> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
                .toList();
        problem.setProperty("fieldErrors", fieldErrors);
        return problem;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingRequestHeaderException.class})
    ProblemDetail handleMalformedRequest(Exception exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Malformed request",
                "The request could not be read", request);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    ProblemDetail handleNotFound(OrderNotFoundException exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Order not found", exception.getMessage(), request);
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    ProblemDetail handleIdempotencyConflict(IdempotencyConflictException exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Idempotency conflict", exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidOrderException.class)
    ProblemDetail handleInvalidOrder(InvalidOrderException exception, HttpServletRequest request) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid order", exception.getMessage(), request);
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(java.net.URI.create("https://distributed-orders.example/problems/" + status.value()));
        problem.setInstance(java.net.URI.create(request.getRequestURI()));
        problem.setProperty("correlationId", request.getAttribute(CorrelationIdFilter.REQUEST_ATTRIBUTE));
        return problem;
    }

    private record FieldErrorResponse(String field, String message) {
    }
}
