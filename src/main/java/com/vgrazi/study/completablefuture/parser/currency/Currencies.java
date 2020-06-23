package com.vgrazi.study.completablefuture.parser.currency;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString @Getter @Setter
public class Currencies {
    long timestamp;
    private double usdusd;
    private double usdaud;
    private double usdcad;
    private double usdpln;
    private double usdmxn;
}
