package pl.apsw.couponmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.apsw.couponmanager.util.exception.GeoIpLookupException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GeoIpLookupException.class)
    public ProblemDetail handleGeoIpLookupException(GeoIpLookupException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        problemDetail.setTitle("GeoIP Lookup Error");
        problemDetail.setDetail(ex.getMessage());

        return problemDetail;
    }
}
