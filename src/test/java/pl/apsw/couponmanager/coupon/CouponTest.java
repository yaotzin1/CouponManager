package pl.apsw.couponmanager.coupon;

import org.junit.jupiter.api.Test;
import pl.apsw.couponmanager.coupon.exception.AlreadyUsedException;
import pl.apsw.couponmanager.coupon.exception.InvalidCountryException;
import pl.apsw.couponmanager.coupon.exception.LimitReachedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponTest {

    @Test
    void shouldCreateCouponWithNormalizedCodeAndCountry() {
        Coupon coupon = new Coupon("  wiosna2026  ", " pl ", 10);

        assertThat(coupon.getCode()).isEqualTo("WIOSNA2026");
        assertThat(coupon.getTargetCountry()).isEqualTo("PL");
        assertThat(coupon.getMaxUses()).isEqualTo(10);
        assertThat(coupon.getCurrentUses()).isZero();
        assertThat(coupon.getCreatedAt()).isNotNull();
        assertThat(coupon.getUsages()).isEmpty();
    }

    @Test
    void shouldRejectCouponWithBlankCode() {
        assertThatThrownBy(() -> new Coupon("   ", "PL", 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Coupon code must not be blank");
    }

    @Test
    void shouldRejectCouponWithBlankCountry() {
        assertThatThrownBy(() -> new Coupon("WIOSNA", "   ", 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Country must not be blank");
    }

    @Test
    void shouldRejectCouponWithZeroMaxUses() {
        assertThatThrownBy(() -> new Coupon("WIOSNA", "PL", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Coupon maxUses must be greater than zero");
    }

    @Test
    void shouldUseCouponSuccessfully() {
        Coupon coupon = new Coupon("WIOSNA", "PL", 2);

        coupon.use("user-1", "PL");

        assertThat(coupon.getCurrentUses()).isEqualTo(1);
        assertThat(coupon.getUsages()).hasSize(1);
    }

    @Test
    void shouldNormalizeUserCountryWhenUsingCoupon() {
        Coupon coupon = new Coupon("WIOSNA", "PL", 2);

        coupon.use("user-1", " pl ");

        assertThat(coupon.getCurrentUses()).isEqualTo(1);
        assertThat(coupon.getUsages()).hasSize(1);
    }

    @Test
    void shouldRejectUsageFromInvalidCountry() {
        Coupon coupon = new Coupon("WIOSNA", "PL", 2);

        assertThatThrownBy(() -> coupon.use("user-1", "DE"))
                .isInstanceOf(InvalidCountryException.class)
                .hasMessage("Coupon is not valid for country: DE");

        assertThat(coupon.getCurrentUses()).isZero();
        assertThat(coupon.getUsages()).isEmpty();
    }

    @Test
    void shouldRejectUsageWhenLimitIsReached() {
        Coupon coupon = new Coupon("WIOSNA", "PL", 1);

        coupon.use("user-1", "PL");

        assertThatThrownBy(() -> coupon.use("user-2", "PL"))
                .isInstanceOf(LimitReachedException.class)
                .hasMessage("Coupon has reached maximum uses");

        assertThat(coupon.getCurrentUses()).isEqualTo(1);
        assertThat(coupon.getUsages()).hasSize(1);
    }

    @Test
    void shouldRejectSecondUsageBySameUser() {
        Coupon coupon = new Coupon("WIOSNA", "PL", 10);

        coupon.use("user-1", "PL");

        assertThatThrownBy(() -> coupon.use("user-1", "PL"))
                .isInstanceOf(AlreadyUsedException.class)
                .hasMessage("User user-1 already used this coupon");

        assertThat(coupon.getCurrentUses()).isEqualTo(1);
        assertThat(coupon.getUsages()).hasSize(1);
    }

    @Test
    void shouldNotIncreaseUsageCounterWhenUsageFailsBecauseOfCountry() {
        Coupon coupon = new Coupon("WIOSNA", "PL", 10);

        assertThatThrownBy(() -> coupon.use("user-1", "DE"))
                .isInstanceOf(InvalidCountryException.class);

        assertThat(coupon.getCurrentUses()).isZero();
        assertThat(coupon.getUsages()).isEmpty();
    }

    @Test
    void shouldNormalizeCodeUsingLocaleRoot() {
        assertThat(Coupon.normalizeCode("  wiosna  ")).isEqualTo("WIOSNA");
    }

    @Test
    void shouldNormalizeCountryUsingLocaleRoot() {
        assertThat(Coupon.normalizeCountry(" pl ")).isEqualTo("PL");
    }

    @Test
    void shouldRejectBlankCodeNormalization() {
        assertThatThrownBy(() -> Coupon.normalizeCode(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Coupon code must not be blank");
    }

    @Test
    void shouldRejectBlankCountryNormalization() {
        assertThatThrownBy(() -> Coupon.normalizeCountry(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Country must not be blank");
    }
}
