package pl.apsw.couponmanager.coupon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
@Table(
        name = "coupon_usages",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_coupon_usages_coupon_id_user_id",
                        columnNames = {"coupon_id", "user_id"}
                )
        }
)
public class CouponUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(name = "user_id", nullable = false, length = 200)
    private String userId;

    @Column(name = "used_at", nullable = false, updatable = false)
    private Instant usedAt;

    public CouponUsage(Coupon coupon, String userId) {
        if (coupon == null) {
            throw new IllegalArgumentException("Coupon must not be null");
        }

        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User id must not be blank");
        }

        this.coupon = coupon;
        this.userId = userId.trim();
        this.usedAt = Instant.now();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof CouponUsage that)) {
            return false;
        }

        UUIDSafeCouponId thisCouponId = UUIDSafeCouponId.from(this.coupon);
        UUIDSafeCouponId thatCouponId = UUIDSafeCouponId.from(that.coupon);

        return Objects.equals(thisCouponId, thatCouponId)
                && Objects.equals(this.userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(UUIDSafeCouponId.from(this.coupon), this.userId);
    }

    private record UUIDSafeCouponId(Object value) {

        private static UUIDSafeCouponId from(Coupon coupon) {
            if (coupon == null) {
                return new UUIDSafeCouponId(null);
            }

            if (coupon.getId() != null) {
                return new UUIDSafeCouponId(coupon.getId());
            }

            return new UUIDSafeCouponId(coupon.getCode());
        }
    }
}
