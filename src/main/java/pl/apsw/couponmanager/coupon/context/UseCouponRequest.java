package pl.apsw.couponmanager.coupon.context;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UseCouponRequest(
        @NotBlank
        @Size(max = 200)
        String userId
) {
}
