package com.example.map.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.map.dto.CarInfoDto;
import com.example.map.dto.ResponseMap;
import com.example.map.dto.WeatherAPIDto;
import com.example.map.dto.WeatherInfoDto;
import com.example.map.jpa.MapEntity;
import com.example.map.service.MapService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class MapController {
    private final MapService mapService;
    private final Environment env;

    static class map {
        static double Re = 6371.00877; // 지도반경
        static double grid = 5.0; // 격자간격 (km)
        static double slat1 = 30.0; // 표준위도 1
        static double slat2 = 60.0; // 표준위도 2
        static double olon = 126.0; // 기준점 경도
        static double olat = 38.0; // 기준점 위도
        static double xo = 210 / grid; // 기준점 X좌표
        static double yo = 675 / grid; // 기준점 Y좌표
        static double first = 0;
    }

    public void mapper(double lon, double lat) {
        double x = 0;
        double y = 0;

        double PI = Math.asin(1.0) * 2.0;
        double DEGRAD = PI / 180.0;
        double RADDEG = 180.0 / PI;

        double re = map.Re / map.grid;
        double slat1 = map.slat1 * DEGRAD;
        double slat2 = map.slat2 * DEGRAD;
        double olon = map.olon * DEGRAD;
        double olat = map.olat * DEGRAD;

        double sn = Math.tan(PI * 0.25 + slat2 * 0.5) / Math.tan(PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);

        map.first = 1;

        double ra = Math.tan(PI * 0.25 + (lat) * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);
        double theta = (lon) * DEGRAD - olon;
        if (theta > PI)
            theta -= 2.0 * PI;
        if (theta < -PI)
            theta += 2.0 * PI;
        theta *= sn;
        x = (float) (ra * Math.sin(theta)) + map.xo;
        y = (float) (ro - ra * Math.cos(theta)) + map.yo;

        System.out.println("x : " + String.valueOf(Math.floor(x + 1.5)) + " y : "
                + String.valueOf(Math.floor(y + 1.5)));
    }

    public CarInfoDto getCarTime(List<ResponseMap> mapList) throws ParseException {
        CarInfoDto dto = new CarInfoDto();
        ResponseMap start = mapList.stream().filter(x -> "S".equals(x.getMapType())).findFirst().orElse(null);
        ResponseMap end = mapList.stream().filter(x -> "E".equals(x.getMapType())).findFirst().orElse(null);

        int duration = 0;
        double distance = 0;
        String message = "";
        // 카카오 내비 API
        // https://apis-navi.kakaomobility.com/v1/directions?
        // origin=126.885209391838,37.5156016724837&destination=127.028234154874,37.4945246017971
        // &waypoints=&priority=RECOMMEND&car_fuel=GASOLINE&car_hipass=false&alternatives=false&road_details=false
        if (start != null && end != null) {
            String url = "https://apis-navi.kakaomobility.com/v1/directions?"
                    // + "?serviceKey=" + env.getProperty("api.kakaorestkey")
                    + "priority=RECOMMEND&car_fuel=GASOLINE&car_hipass=false&alternatives=false&road_details=false"
                    + "&origin=" + start.getLon() + "," + start.getLat()
                    + "&destination=" + end.getLon() + "," + end.getLat();

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", env.getProperty("api.kakaorestkey"));

            HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
            ResponseEntity<String> jsonString = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            // String jsonString = restTemplate.getForObject(url, String.class);
            // 헤더 넣어야 함
            JSONParser jsonParser = new JSONParser();

            JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonString.getBody());
            JSONArray jsonItemList = (JSONArray) jsonObject.get("routes");
            ;

            if (jsonItemList != null && jsonItemList.size() > 0) {
                JSONObject jsonItem = (JSONObject) ((JSONObject) jsonItemList.get(0)).get("summary");
                duration = Math.round(Integer.parseInt(jsonItem.get("duration").toString()) / 60); // 초단위로 되어 있으므로 60초로
                                                                                                   // 나눈다
                distance = (double) (Math.round((Double.parseDouble(jsonItem.get("distance").toString()) / 1000) * 100))
                        / 100;
            }
        }

        dto.setDuration(duration);
        dto.setDistance(distance);
        message = start.getAddr() + "에서 " + end.getAddr() + "까지 자동차로 이동시 거리는 " + distance + "km, 소요시간은 약" + duration
                + "분 입니다.";
        dto.setMessage(message);

        return dto;
    }

    public WeatherInfoDto getWeather(ResponseMap map) throws ParseException {
        WeatherInfoDto dto = new WeatherInfoDto();
        // 오늘 날짜
        String formatDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // 단기예보 공공 API URL
        String url = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst"
                + "?serviceKey=" + env.getProperty("api.weatherkey")
                + "&pageNo=1&numOfRows=500&dataType=JSON&base_time=0500"
                + "&base_date=" + formatDate
                + "&nx=" + map.getXlocation()
                + "&ny=" + map.getYlocation();

        RestTemplate restTemplate = new RestTemplate();
        String jsonString = restTemplate.getForObject(url, String.class);
        JSONParser jsonParser = new JSONParser();

        JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonString);
        JSONArray jsonItemList = new JSONArray();

        if ((JSONObject) jsonObject.get("response") != null) {
            JSONObject jsonItems = (JSONObject) ((JSONObject) ((JSONObject) jsonObject.get("response")).get("body"))
                    .get("items");
            jsonItemList = (JSONArray) jsonItems.get("item");
        }

        for (Object o : jsonItemList) {
            JSONObject item = (JSONObject) o;
            // WeatherDto dto = new WeatherDto((JSONObject) o);
            // List로 8시, 18시 추가
            // 0번째는 출근길 온도는 10도, 날씨는 맑고
            // 1번째는 퇴근길 온도는 7도, 날씨는 비가 오며 강수확률 POP 강수량은 10mm(PCP)
            // REH 습도(여름) SNO 적설량(겨울)
            // 퇴근시 우산을 챙기세요
            if (formatDate.equals(item.get("fcstDate").toString())) {
                if ("S".equals(map.getMapType()) && "0800".equals(item.get("fcstTime").toString())) {
                    dto.mapper(item.get("fcstTime").toString(), item.get("category").toString(),
                            item.get("fcstValue").toString());
                } else if ("E".equals(map.getMapType()) && "1800".equals(item.get("fcstTime").toString())) {
                    dto.mapper(item.get("fcstTime").toString(), item.get("category").toString(),
                            item.get("fcstValue").toString());
                }

            }
        }

        dto.weatherMessage();

        return dto;
    }

    @GetMapping("/weather/{userId}")
    public ResponseEntity<List<WeatherInfoDto>> getWeather(@PathVariable("userId") String userId)
            throws ParseException {

        Iterable<MapEntity> mapList = mapService.getMapbyUserId(userId);
        List<WeatherInfoDto> list = new ArrayList<>();

        mapList.forEach(t -> {
            ResponseMap map = new ModelMapper().map(t, ResponseMap.class);
            if ("S".equals(map.getMapType())) {
                // 날씨 조회
                try {
                    list.add(getWeather(map));
                } catch (ParseException e) {
                    e.toString();
                }
            } else if ("E".equals(map.getMapType())) {
                try {
                    list.add(getWeather(map));
                } catch (ParseException e) {
                    e.toString();
                }
            }

        });

        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @GetMapping("/car/{userId}")
    public ResponseEntity<List<CarInfoDto>> getCar(@PathVariable("userId") String userId) throws ParseException {

        Iterable<MapEntity> mapList = mapService.getMapbyUserId(userId);
        List<CarInfoDto> list = new ArrayList<>();
        List<ResponseMap> res = new ArrayList<>();

        mapList.forEach(t -> {
            ResponseMap map = new ModelMapper().map(t, ResponseMap.class);
            res.add(map);
        });

        try {
            list.add(getCarTime(res));
        } catch (ParseException e) {
            e.toString();
        }

        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @GetMapping("/map/{userId}")
    public ResponseEntity<List<ResponseMap>> getMapdata(@PathVariable("userId") String userId) {
        Iterable<MapEntity> mapList = mapService.getMapbyUserId(userId);
        List<ResponseMap> list = new ArrayList<>();
        // 59, 126
        // 61, 125

        // mapper(126.929810, 37.488201);
        mapper(126.8852269678076, 37.51557330796054);

        mapList.forEach(t -> {
            list.add(new ModelMapper().map(t, ResponseMap.class));
        });

        return ResponseEntity.status(HttpStatus.OK).body(list);
    }
}
