package com.example.map.dto;

import lombok.Data;

@Data
public class WeatherAPIDto {
    private String serviceKey;
    private String pageNo;
    private String numOfRows;
    private String dataType;
    private String base_date;
    private String base_time;
    private String nx;
    private String ny;
}
