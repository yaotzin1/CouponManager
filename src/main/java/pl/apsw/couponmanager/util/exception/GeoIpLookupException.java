package pl.apsw.couponmanager.util.exception;

public class GeoIpLookupException extends RuntimeException {

    public GeoIpLookupException(String message) {
        super(message);
    }

    public GeoIpLookupException(String message, Throwable cause) {
        super(message, cause);
    }
}
