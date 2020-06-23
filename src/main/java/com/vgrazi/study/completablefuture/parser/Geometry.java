package com.vgrazi.study.completablefuture.parser;

import lombok.Getter;
import lombok.Setter;


/*
    "geometry": {
      "type": "Point",
      "coordinates": [
        -80.16509,
        26.452473
      ]
    }
 */
@Setter @Getter
public class Geometry {
    private String type;
    private float[] coordinates;
}
