package pl.apsw.couponmanager.coupon.exception;

public class InvalidCountryException extends RuntimeException{

    public InvalidCountryException(String message) {
        super(message);
    }


    public InvalidCountryException(String message, Throwable cause) {
        super(message, cause);
    }
}
