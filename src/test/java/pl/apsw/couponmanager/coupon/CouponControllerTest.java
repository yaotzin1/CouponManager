package pl.apsw.couponmanager.coupon;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.apsw.couponmanager.coupon.context.command.CreateCouponCommand;
import pl.apsw.couponmanager.coupon.context.command.UseCouponCommand;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CouponController.class)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CouponService couponService;

    @Test
    void shouldCreateCoupon() throws Exception {
        UUID couponId = UUID.randomUUID();

        when(couponService.createCoupon(any(CreateCouponCommand.class))).thenReturn(couponId);

        mockMvc.perform(post("/api/v1/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "wiosna",
                                  "targetCountry": "pl",
                                  "maxUses": 10
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/coupons/WIOSNA"))
                .andExpect(jsonPath("$.id").value(couponId.toString()))
                .andExpect(jsonPath("$.code").value("WIOSNA"));

        ArgumentCaptor<CreateCouponCommand> captor = ArgumentCaptor.forClass(CreateCouponCommand.class);
        verify(couponService).createCoupon(captor.capture());

        CreateCouponCommand command = captor.getValue();

        assertThat(command.code()).isEqualTo("wiosna");
        assertThat(command.targetCountry()).isEqualTo("pl");
        assertThat(command.maxUses()).isEqualTo(10);
    }

    @Test
    void shouldRejectCreateCouponRequestWithBlankCode() throws Exception {
        mockMvc.perform(post("/api/v1/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": " ",
                                  "targetCountry": "PL",
                                  "maxUses": 10
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCreateCouponRequestWithInvalidCountry() throws Exception {
        mockMvc.perform(post("/api/v1/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "WIOSNA",
                                  "targetCountry": "POLAND",
                                  "maxUses": 10
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCreateCouponRequestWithInvalidMaxUses() throws Exception {
        mockMvc.perform(post("/api/v1/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "WIOSNA",
                                  "targetCountry": "PL",
                                  "maxUses": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUseCouponWithXForwardedForHeader() throws Exception {
        mockMvc.perform(post("/api/v1/coupons/wiosna/use")
                        .header("X-Forwarded-For", "83.1.2.3, 10.0.0.1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "user-1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Coupon applied successfully"));

        ArgumentCaptor<UseCouponCommand> captor = ArgumentCaptor.forClass(UseCouponCommand.class);
        verify(couponService).useCoupon(captor.capture());

        UseCouponCommand command = captor.getValue();

        assertThat(command.code()).isEqualTo("wiosna");
        assertThat(command.userId()).isEqualTo("user-1");
        assertThat(command.ipAddress()).isEqualTo("83.1.2.3");
    }

    @Test
    void shouldRejectUseCouponRequestWithBlankUserId() throws Exception {
        mockMvc.perform(post("/api/v1/coupons/wiosna/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": " "
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
