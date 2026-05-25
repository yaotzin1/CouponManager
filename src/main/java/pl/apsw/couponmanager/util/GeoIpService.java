package pl.apsw.couponmanager.util;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pl.apsw.couponmanager.util.exception.GeoIpLookupException;

@Service
public class GeoIpService implements GeoIpClient {

    private final RestClient geoIpRestClient;

    public GeoIpService(RestClient geoIpRestClient) {
        this.geoIpRestClient = geoIpRestClient;
    }

    @Override
    public String getCountryCodeByIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            throw new GeoIpLookupException("IP address must not be blank");
        }

        try {
            GeoIpResponse response = geoIpRestClient.get()
                    .uri("/json/{ipAddress}?fields=status,countryCode,message", ipAddress)
                    .retrieve()
                    .body(GeoIpResponse.class);

            if (response == null) {
                throw new GeoIpLookupException("Failed to retrieve GeoIP data for IP address: " + ipAddress);
            }

            if (!"success".equalsIgnoreCase(response.status())) {
                throw new GeoIpLookupException("Could not resolve country from IP: " + response.message());
            }

            if (response.countryCode() == null || response.countryCode().isBlank()) {
                throw new GeoIpLookupException("GeoIP response does not contain country code");
            }

            return response.countryCode();
        } catch (RestClientException exception) {
            throw new GeoIpLookupException("GeoIP provider request failed", exception);
        }
    }

    private record GeoIpResponse(String countryCode, String status, String message) {
    }
}
