package com.vgrazi.study.completablefuture.parser.geo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/*
    "geometry": {
      "type": "Point",
      "coordinates": [
        -80.16509,
        26.452473
      ]
    }
 */
@Setter @Getter @ToString
public class Geometry {
    private String type;
    @ToString.Exclude
    private float[] coordinates;
}
