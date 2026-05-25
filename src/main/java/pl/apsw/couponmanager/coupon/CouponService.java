package pl.apsw.couponmanager.coupon;

import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pl.apsw.couponmanager.config.CouponRedemptionProperties;
import pl.apsw.couponmanager.coupon.context.command.CreateCouponCommand;
import pl.apsw.couponmanager.coupon.context.command.UseCouponCommand;
import pl.apsw.couponmanager.coupon.exception.CouponAlreadyExistsException;
import pl.apsw.couponmanager.coupon.exception.CouponNotFoundException;
import pl.apsw.couponmanager.util.GeoIpClient;

import java.util.UUID;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final GeoIpClient geoIpClient;
    private final CouponRedemptionProperties properties;

    public CouponService(CouponRepository couponRepository, GeoIpClient geoIpClient, CouponRedemptionProperties properties) {
        this.couponRepository = couponRepository;
        this.geoIpClient = geoIpClient;
        this.properties = properties;
    }

    @Transactional
    public UUID createCoupon(CreateCouponCommand command) {
        String normalizedCode = Coupon.normalizeCode(command.code());
        if (couponRepository.existsByCode(normalizedCode)) {
            throw new CouponAlreadyExistsException("Coupon with code " + normalizedCode + " already exists");
        }

        Coupon coupon = new Coupon(
                command.code(),
                command.targetCountry(),
                command.maxUses()
        );

        try {
            return couponRepository.save(coupon).getId();
        } catch (DataIntegrityViolationException exception) {
            throw new CouponAlreadyExistsException("Coupon with code " + normalizedCode + " already exists");
        }
    }

    @Transactional
    public void useCoupon(UseCouponCommand command) {
        String normalizedCode = Coupon.normalizeCode(command.code());

        Coupon coupon = couponRepository.findByCodeForUpdate(normalizedCode)
                .orElseThrow(() -> new CouponNotFoundException("Coupon with code " + normalizedCode + " was not found"));

        String userCountry = resolveUserCountry(command.ipAddress(), coupon);

        coupon.use(command.userId(), userCountry);
    }

    private String resolveUserCountry(String ipAddress, Coupon coupon) {
        if (!properties.countryRestriction().enabled()) {
            return coupon.getTargetCountry();
        }

        return geoIpClient.getCountryCodeByIp(ipAddress);
    }
}
