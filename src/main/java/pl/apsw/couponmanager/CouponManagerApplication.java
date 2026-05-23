package pl.apsw.couponmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class CouponManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CouponManagerApplication.class, args);
    }

}
