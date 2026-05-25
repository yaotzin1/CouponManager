package pl.apsw.couponmanager.coupon;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponUsageTest {

    @Test
    void shouldCreateCouponUsage() {
        Coupon coupon = new Coupon("WIOSNA", "PL", 10);

        CouponUsage usage = new CouponUsage(coupon, " user-1 ");

        assertThat(usage.getCoupon()).isSameAs(coupon);
        assertThat(usage.getUserId()).isEqualTo("user-1");
        assertThat(usage.getUsedAt()).isNotNull();
    }

    @Test
    void shouldRejectNullCoupon() {
        assertThatThrownBy(() -> new CouponUsage(null, "user-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Coupon must not be null");
    }

    @Test
    void shouldRejectBlankUserId() {
        Coupon coupon = new Coupon("WIOSNA", "PL", 10);

        assertThatThrownBy(() -> new CouponUsage(coupon, " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User id must not be blank");
    }

    @Test
    void shouldTreatUsagesForSameCouponAndSameUserAsEqual() {
        Coupon coupon = new Coupon("WIOSNA", "PL", 10);

        CouponUsage firstUsage = new CouponUsage(coupon, "user-1");
        CouponUsage secondUsage = new CouponUsage(coupon, "user-1");

        assertThat(firstUsage).isEqualTo(secondUsage);
        assertThat(firstUsage.hashCode()).isEqualTo(secondUsage.hashCode());
    }

    @Test
    void shouldTreatUsagesForSameCouponAndDifferentUsersAsDifferent() {
        Coupon coupon = new Coupon("WIOSNA", "PL", 10);

        CouponUsage firstUsage = new CouponUsage(coupon, "user-1");
        CouponUsage secondUsage = new CouponUsage(coupon, "user-2");

        assertThat(firstUsage).isNotEqualTo(secondUsage);
    }

    @Test
    void shouldTreatUsagesForDifferentCouponsAndSameUserAsDifferent() {
        Coupon firstCoupon = new Coupon("WIOSNA", "PL", 10);
        Coupon secondCoupon = new Coupon("LATO", "PL", 10);

        CouponUsage firstUsage = new CouponUsage(firstCoupon, "user-1");
        CouponUsage secondUsage = new CouponUsage(secondCoupon, "user-1");

        assertThat(firstUsage).isNotEqualTo(secondUsage);
    }
}
