package pl.apsw.couponmanager.exception;

import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.apsw.couponmanager.coupon.exception.*;
import pl.apsw.couponmanager.util.exception.GeoIpLookupException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CouponNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleCouponNotFound(CouponNotFoundException exception) {
        return error(
                HttpStatus.NOT_FOUND,
                "COUPON_NOT_FOUND",
                exception.getMessage()
        );
    }

    @ExceptionHandler(CouponAlreadyExistsException.class)
    ResponseEntity<ApiErrorResponse> handleCouponAlreadyExists(CouponAlreadyExistsException exception) {
        return error(
                HttpStatus.CONFLICT,
                "COUPON_ALREADY_EXISTS",
                exception.getMessage()
        );
    }

    @ExceptionHandler(LimitReachedException.class)
    ResponseEntity<ApiErrorResponse> handleLimitReached(LimitReachedException exception) {
        return error(
                HttpStatus.CONFLICT,
                "COUPON_LIMIT_REACHED",
                exception.getMessage()
        );
    }

    @ExceptionHandler(AlreadyUsedException.class)
    ResponseEntity<ApiErrorResponse> handleAlreadyUsed(AlreadyUsedException exception) {
        return error(
                HttpStatus.CONFLICT,
                "COUPON_ALREADY_USED",
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidCountryException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidCountry(InvalidCountryException exception) {
        return error(
                HttpStatus.FORBIDDEN,
                "COUPON_INVALID_COUNTRY",
                exception.getMessage()
        );
    }

    @ExceptionHandler(GeoIpLookupException.class)
    ResponseEntity<ApiErrorResponse> handleGeoIpLookup(GeoIpLookupException exception) {
        return error(
                HttpStatus.SERVICE_UNAVAILABLE,
                "GEO_IP_LOOKUP_FAILED",
                exception.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage())
        );

        HttpStatus status = HttpStatus.BAD_REQUEST;

        ApiErrorResponse response = ApiErrorResponse.withFieldErrors(
                status.value(),
                status.getReasonPhrase(),
                "VALIDATION_FAILED",
                "Request validation failed",
                fieldErrors
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler({
            CannotAcquireLockException.class,
            LockTimeoutException.class,
            PessimisticLockException.class,
            TransactionTimedOutException.class
    })
    ResponseEntity<ApiErrorResponse> handleLockingFailure(Exception exception) {
        return error(
                HttpStatus.CONFLICT,
                "COUPON_CURRENTLY_LOCKED",
                "Coupon is currently being processed. Please try again."
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        return error(
                HttpStatus.CONFLICT,
                "DATA_INTEGRITY_VIOLATION",
                "Request violates data integrity constraints"
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return error(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                exception.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Unexpected server error"
        );
    }

    private ResponseEntity<ApiErrorResponse> error(
            HttpStatus status,
            String code,
            String message
    ) {
        ApiErrorResponse response = ApiErrorResponse.of(
                status.value(),
                status.getReasonPhrase(),
                code,
                message
        );

        return ResponseEntity.status(status).body(response);
    }
}
