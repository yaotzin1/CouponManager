package pl.apsw.couponmanager.util;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pl.apsw.couponmanager.util.exception.GeoIpLookupException;

@Service
public class GeoIpService implements GeoIpClient {

    private final RestClient restClient;

    public GeoIpService(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public String getCountryCodeByIp(String ipAddress) {
        GeoIpResponse response = restClient.get()
                .uri("/json/{ipAddress}?fields=status,countryCode,message", ipAddress)
                .retrieve()
                .body(GeoIpResponse.class);
        if(response == null) {
            throw new GeoIpLookupException("Failed to retrieve GeoIP data for IP address: " + ipAddress);
        }

        if(!"success".equals(response.status())) {
            throw new GeoIpLookupException("Could not resolve country from IP: " + response.message());
        }

        return response.countryCode();
    }

    private record GeoIpResponse(String countryCode, String status, String message) {
    }
}
