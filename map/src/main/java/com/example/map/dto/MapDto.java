package com.example.map.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class MapDto implements Serializable {
    private String mapId;
    private String xlocation;
    private String ylocation;
    private String userId;
    private String mapType;
}
