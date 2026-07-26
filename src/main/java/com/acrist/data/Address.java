package com.acrist.data;

import java.util.Objects;

public class Address {
    private String domain;
    private String ip;

    public Address(String domain, String ip) {
        this.domain = domain;
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDomain() {
        return domain;
    }
    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(domain, address.domain) && Objects.equals(ip, address.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, ip);
    }

    @Override
    public String toString() {
        return "Address{" +
                "domain='" + domain + '\'' +
                ", ip='" + ip + '\'' +
                '}';
    }
}
