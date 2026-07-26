package com.acrist.utils;

import com.acrist.data.Address;

import java.util.List;

public class JsonSerializer {

    public static String toJSON(List<Address> addresses){
        StringBuilder sb = new StringBuilder();
        sb.append("\n    \"addresses\": [\n");

        for (int i = 0; i < addresses.size(); i++) {
            Address address = addresses.get(i);
            sb.append("    {\"domain\": \"")
                    .append(escapeJson(address.getDomain()))
                    .append("\", \"ip\": \"")
                    .append(escapeJson(address.getIp()))
                    .append("\"}");

            if (i < addresses.size() - 1) {
                sb.append(",");
            }

            sb.append("\n");
        }
        sb.append("]\n");

        return sb.toString();
    }

    private static String escapeJson(String value){
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
