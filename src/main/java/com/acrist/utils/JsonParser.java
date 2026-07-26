package com.acrist.utils;

import com.acrist.data.Address;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonParser {

    private static final Pattern OBJECT_PATTERN = Pattern
            .compile("\\{([^}]+)}");
    private static final Pattern DOMAIN_PATTERN = Pattern
            .compile("\"domain\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern IP_PATTERN = Pattern
            .compile("\"ip\"\\s*:\\s*\"([^\"]+)\"");

    public static List<Address> parse(String json){
        List<Address> addresses = new ArrayList<>();

        if (json == null || json.trim().isEmpty()){
            return addresses;
        }

        Matcher objMatcher = OBJECT_PATTERN.matcher(json);

        while (objMatcher.find()){
            String objContent = objMatcher.group(1);

            String domain = extractValue(objContent, DOMAIN_PATTERN);
            String ip = extractValue(objContent, IP_PATTERN);

            if (domain != null && ip != null){
                addresses.add(new Address(domain.trim(), ip.trim()));
            }
        }

        return addresses;
    }

    private static String extractValue(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()){
            return matcher.group(1);
        }

        return null;
    }
}
