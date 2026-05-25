package pl.apsw.couponmanager.coupon.context;

import java.util.UUID;

public record CreateCouponResponse (
        UUID id,
        String code
){
}
