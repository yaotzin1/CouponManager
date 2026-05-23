package pl.apsw.couponmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "coupon-manager.redemption")
public record CouponRedemptionProperties(
        CountryRestriction countryRestriction
) {

    public record CountryRestriction(
            boolean enabled
    ) {
    }
}
