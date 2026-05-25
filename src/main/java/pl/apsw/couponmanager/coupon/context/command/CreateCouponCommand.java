package pl.apsw.couponmanager.coupon.context.command;

public record CreateCouponCommand(
        String code,
        String targetCountry,
        int maxUses
) {
}
