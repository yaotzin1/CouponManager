package pl.apsw.couponmanager.coupon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import pl.apsw.couponmanager.config.CouponRedemptionProperties;
import pl.apsw.couponmanager.coupon.context.command.CreateCouponCommand;
import pl.apsw.couponmanager.coupon.context.command.UseCouponCommand;
import pl.apsw.couponmanager.coupon.exception.CouponAlreadyExistsException;
import pl.apsw.couponmanager.coupon.exception.CouponNotFoundException;
import pl.apsw.couponmanager.util.GeoIpClient;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CouponServiceTest {

    private CouponRepository couponRepository;
    private GeoIpClient geoIpClient;
    private CouponService couponService;

    @BeforeEach
    void setUp() {
        couponRepository = mock(CouponRepository.class);
        geoIpClient = mock(GeoIpClient.class);

        CouponRedemptionProperties properties = new CouponRedemptionProperties(
                new CouponRedemptionProperties.CountryRestriction(true)
        );

        couponService = new CouponService(couponRepository, geoIpClient, properties);
    }

    @Test
    void shouldCreateCoupon() {
        CreateCouponCommand command = new CreateCouponCommand(" wiosna ", " pl ", 10);

        when(couponRepository.existsByCode("WIOSNA")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UUID result = couponService.createCoupon(command);

        ArgumentCaptor<Coupon> couponCaptor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponRepository).save(couponCaptor.capture());

        Coupon savedCoupon = couponCaptor.getValue();

        assertThat(result).isNull(); 
        assertThat(savedCoupon.getCode()).isEqualTo("WIOSNA");
        assertThat(savedCoupon.getTargetCountry()).isEqualTo("PL");
        assertThat(savedCoupon.getMaxUses()).isEqualTo(10);
    }

    @Test
    void shouldRejectCreationWhenCouponAlreadyExists() {
        CreateCouponCommand command = new CreateCouponCommand("wiosna", "PL", 10);

        when(couponRepository.existsByCode("WIOSNA")).thenReturn(true);

        assertThatThrownBy(() -> couponService.createCoupon(command))
                .isInstanceOf(CouponAlreadyExistsException.class)
                .hasMessage("Coupon with code WIOSNA already exists");

        verify(couponRepository, never()).save(any());
    }

    @Test
    void shouldMapDataIntegrityViolationToCouponAlreadyExists() {
        CreateCouponCommand command = new CreateCouponCommand("wiosna", "PL", 10);

        when(couponRepository.existsByCode("WIOSNA")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> couponService.createCoupon(command))
                .isInstanceOf(CouponAlreadyExistsException.class)
                .hasMessage("Coupon with code WIOSNA already exists");
    }

    @Test
    void shouldUseCoupon() {
        Coupon coupon = new Coupon("WIOSNA", "PL", 10);
        UseCouponCommand command = new UseCouponCommand(" wiosna ", "user-1", "8.8.8.8");

        when(couponRepository.findByCodeForUpdate("WIOSNA")).thenReturn(Optional.of(coupon));
        when(geoIpClient.getCountryCodeByIp("8.8.8.8")).thenReturn("PL");

        couponService.useCoupon(command);

        assertThat(coupon.getCurrentUses()).isEqualTo(1);
        assertThat(coupon.getUsages()).hasSize(1);

        verify(couponRepository).findByCodeForUpdate("WIOSNA");
        verify(geoIpClient).getCountryCodeByIp("8.8.8.8");
    }

    @Test
    void shouldThrowWhenCouponDoesNotExist() {
        UseCouponCommand command = new UseCouponCommand("unknown", "user-1", "8.8.8.8");

        when(couponRepository.findByCodeForUpdate("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.useCoupon(command))
                .isInstanceOf(CouponNotFoundException.class)
                .hasMessage("Coupon with code UNKNOWN was not found");

        verifyNoInteractions(geoIpClient);
    }

    @Test
    void shouldNotCallGeoIpWhenCouponDoesNotExist() {
        UseCouponCommand command = new UseCouponCommand("unknown", "user-1", "8.8.8.8");

        when(couponRepository.findByCodeForUpdate("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.useCoupon(command))
                .isInstanceOf(CouponNotFoundException.class);

        verifyNoInteractions(geoIpClient);
    }

    @Test
    void shouldSkipGeoIpWhenCountryRestrictionIsDisabled() {
        couponService = new CouponService(
                couponRepository,
                geoIpClient,
                new CouponRedemptionProperties(
                        new CouponRedemptionProperties.CountryRestriction(false)
                )
        );

        Coupon coupon = new Coupon("WIOSNA", "PL", 10);
        UseCouponCommand command = new UseCouponCommand("wiosna", "user-1", "8.8.8.8");

        when(couponRepository.findByCodeForUpdate("WIOSNA")).thenReturn(Optional.of(coupon));

        couponService.useCoupon(command);

        assertThat(coupon.getCurrentUses()).isEqualTo(1);
        verifyNoInteractions(geoIpClient);
    }
}
