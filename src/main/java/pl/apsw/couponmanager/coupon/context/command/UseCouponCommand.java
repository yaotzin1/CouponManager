package pl.apsw.couponmanager.coupon.context.command;

public record UseCouponCommand(
        String code,
        String userId,
        String ipAddress
) {
}
