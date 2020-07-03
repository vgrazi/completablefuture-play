package com.vgrazi.study.completablefuture.parser.account;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Account {
    private String city;
    private String state;
    private String zip;
    private int timezone;
    private double longitude;
    private double latitude;
    private String dst;
    private String license;
    private String currency;

}
