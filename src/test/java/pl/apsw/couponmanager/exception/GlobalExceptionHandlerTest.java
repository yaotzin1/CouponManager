package pl.apsw.couponmanager.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.apsw.couponmanager.coupon.CouponController;
import pl.apsw.couponmanager.coupon.CouponService;
import pl.apsw.couponmanager.coupon.exception.AlreadyUsedException;
import pl.apsw.couponmanager.coupon.exception.CouponNotFoundException;
import pl.apsw.couponmanager.coupon.exception.InvalidCountryException;
import pl.apsw.couponmanager.coupon.exception.LimitReachedException;
import pl.apsw.couponmanager.util.exception.GeoIpLookupException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(CouponController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CouponService couponService;

    @Test
    void shouldReturn404ForCouponNotFound() throws Exception {
        doThrow(new CouponNotFoundException("Coupon with code UNKNOWN was not found"))
                .when(couponService).useCoupon(any());

        mockMvc.perform(post("/api/v1/coupons/unknown/use")
                        .contentType("application/json")
                        .content("""
                                {
                                  "userId": "user-1"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COUPON_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Coupon with code UNKNOWN was not found"));
    }

    @Test
    void shouldReturn409ForLimitReached() throws Exception {
        doThrow(new LimitReachedException("Coupon has reached maximum uses"))
                .when(couponService).useCoupon(any());

        mockMvc.perform(post("/api/v1/coupons/wiosna/use")
                        .contentType("application/json")
                        .content("""
                                {
                                  "userId": "user-1"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("COUPON_LIMIT_REACHED"));
    }

    @Test
    void shouldReturn409ForAlreadyUsedCoupon() throws Exception {
        doThrow(new AlreadyUsedException("User user-1 already used this coupon"))
                .when(couponService).useCoupon(any());

        mockMvc.perform(post("/api/v1/coupons/wiosna/use")
                        .contentType("application/json")
                        .content("""
                                {
                                  "userId": "user-1"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("COUPON_ALREADY_USED"));
    }

    @Test
    void shouldReturn403ForInvalidCountry() throws Exception {
        doThrow(new InvalidCountryException("Coupon is not valid for country: DE"))
                .when(couponService).useCoupon(any());

        mockMvc.perform(post("/api/v1/coupons/wiosna/use")
                        .contentType("application/json")
                        .content("""
                                {
                                  "userId": "user-1"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COUPON_INVALID_COUNTRY"));
    }

    @Test
    void shouldReturn503ForGeoIpFailure() throws Exception {
        doThrow(new GeoIpLookupException("GeoIP provider request failed"))
                .when(couponService).useCoupon(any());

        mockMvc.perform(post("/api/v1/coupons/wiosna/use")
                        .contentType("application/json")
                        .content("""
                                {
                                  "userId": "user-1"
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("GEO_IP_LOOKUP_FAILED"));
    }
}
