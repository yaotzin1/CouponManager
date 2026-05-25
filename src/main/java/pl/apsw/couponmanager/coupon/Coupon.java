package pl.apsw.couponmanager.coupon;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.apsw.couponmanager.coupon.exception.AlreadyUsedException;
import pl.apsw.couponmanager.coupon.exception.InvalidCountryException;
import pl.apsw.couponmanager.coupon.exception.LimitReachedException;

import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
@Table(
        name = "coupons",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_coupons_code", columnNames = "code")
        }
)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", nullable = false, updatable = false, length = 100)
    private String code;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "max_uses", nullable = false)
    private int maxUses;

    @Column(name = "current_uses", nullable = false)
    private int currentUses;

    @Column(name = "target_country", nullable = false, length = 2)
    private String targetCountry;

    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CouponUsage> usages = new HashSet<>();

    public Coupon(String code, String targetCountry, int maxUses) {
        if (maxUses < 1) {
            throw new IllegalArgumentException("Coupon maxUses must be greater than zero");
        }

        this.code = normalizeCode(code);
        this.targetCountry = normalizeCountry(targetCountry);
        this.createdAt = Instant.now();
        this.maxUses = maxUses;
        this.currentUses = 0;
    }

    public void use(String userId, String userCountry) {
        String normalizedUserCountry = normalizeCountry(userCountry);

        if (!this.targetCountry.equals(normalizedUserCountry)) {
            throw new InvalidCountryException("Coupon is not valid for country: " + normalizedUserCountry);
        }

        if (this.currentUses >= this.maxUses) {
            throw new LimitReachedException("Coupon has reached maximum uses");
        }

        CouponUsage usage = new CouponUsage(this, userId);

        if (this.usages.contains(usage)) {
            throw new AlreadyUsedException("User " + userId + " already used this coupon");
        }

        this.usages.add(usage);
        this.currentUses++;
    }

    public static String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Coupon code must not be blank");
        }

        return code.trim().toUpperCase(Locale.ROOT);
    }

    public static String normalizeCountry(String country) {
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Country must not be blank");
        }

        return country.trim().toUpperCase(Locale.ROOT);
    }
}
