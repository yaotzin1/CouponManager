package pl.apsw.couponmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient geoIpRestClient(
            GeoIpIntegrationProperties geoIpIntegrationProperties
    ){
        return RestClient.builder().
                baseUrl(geoIpIntegrationProperties.baseUrl()).build();
    }
}
