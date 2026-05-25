package pl.apsw.couponmanager.coupon;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.apsw.couponmanager.coupon.context.CreateCouponRequest;
import pl.apsw.couponmanager.coupon.context.CreateCouponResponse;
import pl.apsw.couponmanager.coupon.context.UseCouponRequest;
import pl.apsw.couponmanager.coupon.context.UseCouponResponse;
import pl.apsw.couponmanager.coupon.context.command.CreateCouponCommand;
import pl.apsw.couponmanager.coupon.context.command.UseCouponCommand;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController {
    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    public ResponseEntity<CreateCouponResponse> createCoupon(@RequestBody @Valid CreateCouponRequest request) {
        CreateCouponCommand command = new CreateCouponCommand(
                request.code(),
                request.targetCountry(),
                request.maxUses()
        );

        UUID couponId = couponService.createCoupon(command);
        String normalizedCode = Coupon.normalizeCode(request.code());

        return ResponseEntity
                .created(URI.create("/api/v1/coupons/" + normalizedCode))
                .body(new CreateCouponResponse(couponId, normalizedCode));
    }

    @PostMapping("/{code}/use")
    public ResponseEntity<UseCouponResponse> useCoupon(
            @PathVariable String code,
            @RequestBody @Valid UseCouponRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = extractClientIp(httpRequest);

        UseCouponCommand command = new UseCouponCommand(
                code,
                request.userId(),
                clientIp
        );

        couponService.useCoupon(command);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new UseCouponResponse("Coupon applied successfully"));
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
