package com.vgrazi.study.completablefuture.parser;

import lombok.Getter;
import lombok.Setter;


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
@Getter @Setter
public class Dataset {
    private String datasetid;
    private String recordid;
    private String record_timestamp;
    private Fields fields;
    private Geometry geometry;
}

