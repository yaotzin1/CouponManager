package pl.apsw.couponmanager.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;

class GeoIpServiceTest {

    private MockRestServiceServer server;
    private GeoIpService geoIpService;

    @BeforeEach
    void setUp() {
        RestClient.Builder restClientBuilder = RestClient.builder()
                .baseUrl("http://ip-api.com");

        server = MockRestServiceServer.bindTo(restClientBuilder)
                .build();

        geoIpService = new GeoIpService(restClientBuilder.build());
    }

    @Test
    void getCountryCodeByIpValidIpShouldReturnCountryCode() {
        server.expect(once(), requestTo("http://ip-api.com/json/8.8.8.8?fields=status,countryCode,message"))
                .andRespond(withSuccess("""
                        {
                          "status": "success",
                          "countryCode": "US",
                          "message": null
                        }
                        """, MediaType.APPLICATION_JSON));
        String countryCode = geoIpService.getCountryCodeByIp("8.8.8.8");

        assertEquals("US", countryCode);
        server.verify();
    }

    @Test
    void getCountryCodeByIpInvalidResponseStatusShouldThrowException() {
        server.expect(once(), requestTo("http://ip-api.com/json/192.168.1.1?fields=status,countryCode,message"))
                .andRespond(withSuccess("""
                        {
                          "status": "fail",
                          "countryCode": null,
                          "message": "private range"
                        }
                        """, MediaType.APPLICATION_JSON));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> geoIpService.getCountryCodeByIp("192.168.1.1")
        );

        assertEquals("Could not resolve country from IP: private range", exception.getMessage());
        server.verify();
    }

    @Test
    void getCountryCodeByIpNullResponseShouldThrowException() {
        server.expect(once(), requestTo("http://ip-api.com/json/8.8.8.8?fields=status,countryCode,message"))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> geoIpService.getCountryCodeByIp("8.8.8.8")
        );

        assertEquals("Failed to retrieve GeoIP data for IP address: 8.8.8.8", exception.getMessage());
        server.verify();
    }
}