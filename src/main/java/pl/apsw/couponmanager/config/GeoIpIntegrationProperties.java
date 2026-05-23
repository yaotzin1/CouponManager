package pl.apsw.couponmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "coupon-manager.integrations.geo-ip")
public record GeoIpIntegrationProperties(
        String provider,
        String baseUrl
) {
}
