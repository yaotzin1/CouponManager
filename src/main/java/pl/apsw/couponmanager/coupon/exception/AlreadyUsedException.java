package pl.apsw.couponmanager.coupon.exception;

public class AlreadyUsedException extends RuntimeException {

    public AlreadyUsedException(String message) {
        super(message);
    }

    public AlreadyUsedException(String message, Throwable cause) {
        super(message, cause);
    }
}
