package pl.apsw.couponmanager.coupon.exception;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        Map<String, String> fieldErrors
) {

    public static ApiErrorResponse of(
      int status,
      String error,
      String code,
      String message
    ){
        return new ApiErrorResponse(
          Instant.now(),
          status,
          error,
          code,
          message,
          Map.of()
        );
    }

    public static ApiErrorResponse withFieldErrors(
            int status,
            String error,
            String code,
            String message,
            Map<String, String> fieldErrors
    ){
        return new ApiErrorResponse(
          Instant.now(),
          status,
          error,
          code,
          message,
          fieldErrors
        );
    }
}
