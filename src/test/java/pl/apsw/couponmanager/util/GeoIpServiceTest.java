package pl.apsw.couponmanager.util;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import pl.apsw.couponmanager.util.exception.GeoIpLookupException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeoIpServiceTest {

    @Test
    void shouldRejectNullIpAddress() {
        GeoIpService service = new GeoIpService(RestClient.create("http://localhost"));

        assertThatThrownBy(() -> service.getCountryCodeByIp(null))
                .isInstanceOf(GeoIpLookupException.class)
                .hasMessage("IP address must not be blank");
    }

    @Test
    void shouldRejectBlankIpAddress() {
        GeoIpService service = new GeoIpService(RestClient.create("http://localhost"));

        assertThatThrownBy(() -> service.getCountryCodeByIp(" "))
                .isInstanceOf(GeoIpLookupException.class)
                .hasMessage("IP address must not be blank");
    }

    @Test
    void shouldWrapRestClientException() {
        GeoIpService service = new GeoIpService(RestClient.create("http://localhost:1"));

        assertThatThrownBy(() -> service.getCountryCodeByIp("8.8.8.8"))
                .isInstanceOf(GeoIpLookupException.class)
                .hasMessage("GeoIP provider request failed");
    }
}