package pl.apsw.couponmanager.coupon.exception;

public class LimitReachedException extends RuntimeException {

    public LimitReachedException(String message) {
        super(message);
    }

    public LimitReachedException(String message, Throwable cause) {
        super(message, cause);
    }
}
