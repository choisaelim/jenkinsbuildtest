package com.example.map.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseMap {
    private String mapId;
    private String xlocation;
    private String ylocation;
    private String lat;
    private String lon;
    private String userId;
    private String mapType;
    private Date createdAt;
    private String region3;
    private String addr;

}
