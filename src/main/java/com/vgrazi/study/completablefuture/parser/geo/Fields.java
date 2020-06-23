package com.vgrazi.study.completablefuture.parser.geo;

import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class Fields {
//          "city": "Delray Beach",
//          "zip": "33446",
//          "dst": 1,
//          "geopoint": [
//              26.452473,
//              -80.16509
//          ]
    private float[] geopoint;
    private String city;
    private String state;
    private String zip;
    private String timezone;
    private float longitude;
    private float latitude;
    private int dst;

}
