package com.vgrazi.study.completablefuture.parser.geo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/*
  {
    "datasetid": "us-zip-code-latitude-and-longitude",
    "recordid": "e1b291a937642b62bfb49cbff7d1d25486bf4afd",
    "fields": {
      "city": "Delray Beach",
      "zip": "33446",
      "dst": 1,
      "geopoint": [
        26.452473,
        -80.16509
      ],
      "longitude": -80.16509,
      "state": "FL",
      "latitude": 26.452473,
      "timezone": -5
    },
    "geometry": {
      "type": "Point",
      "coordinates": [
        -80.16509,
        26.452473
      ]
    }
 */
@Getter @Setter @ToString
public class Dataset {
    @ToString.Exclude
    private String datasetid;
    @ToString.Exclude
    private String recordid;
    @ToString.Exclude
    private String record_timestamp;
    private Fields fields;
    @ToString.Exclude
    private Geometry geometry;
}

