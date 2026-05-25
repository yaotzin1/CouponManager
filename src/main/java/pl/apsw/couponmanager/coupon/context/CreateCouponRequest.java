package pl.apsw.couponmanager.coupon.context;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCouponRequest (
    @NotBlank
    @Size(max = 100)
    @Pattern(
            regexp = "^[A-Za-z0-9_-]+$",
            message = "Coupon code may contain only letters, numbers, hyphens and underscores"
    )
    String code,

    @NotBlank
    @Pattern(regexp = "^[A-Za-z]{2}$", message = "Country must be a two-letter ISO country code")
    String targetCountry,

    @Min(1)
    int maxUses
){}
