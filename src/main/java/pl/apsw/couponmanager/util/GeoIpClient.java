package pl.apsw.couponmanager.util;

public interface GeoIpClient {

    String getCountryCodeByIp(String ipAddress);
}
