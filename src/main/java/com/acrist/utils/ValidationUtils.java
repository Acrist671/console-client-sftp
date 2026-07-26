package com.acrist.utils;

import com.acrist.data.Address;

import java.util.List;
import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern IPV4 = Pattern.compile(
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static boolean isValidIpv4(String ip){
        if (ip == null || ip.isEmpty()) return false;
        return IPV4.matcher(ip).matches();
    }

    public static boolean isUniqueIp(List<Address> addresses, String newIp){
        return addresses.stream().noneMatch(address -> address.getIp().equals(newIp));
    }

    public static boolean isDomainUnique(List<Address> addresses, String newDomain){
        return addresses.stream().noneMatch(address -> address.getDomain().equals(newDomain));
    }
}
