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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

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

    public void getWeather(WeatherAPIDto api) throws ParseException {
        String url = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst"
                + "?serviceKey=" + api.getServiceKey()
                + "&pageNo=" + api.getPageNo()
                + "&numOfRows=" + api.getNumOfRows()
                + "&dataType=" + api.getDataType()
                + "&base_date=" + api.getBase_date()
                + "&base_time=" + api.getBase_time()
                + "&nx=" + api.getNx()
                + "&ny=" + api.getNy();

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
        // // 가장 큰 JSON 객체 response 가져오기
        // JSONObject jsonResponse = (JSONObject) jsonObject.get("response");

        // // 그 다음 body 부분 파싱
        // JSONObject jsonBody = (JSONObject) jsonResponse.get("body");

        // // 그 다음 위치 정보를 배열로 담은 items 파싱
        // JSONObject jsonItems = (JSONObject) jsonBody.get("items");

        // items는 JSON임, 이제 그걸 또 배열로 가져온다

        // List<WeatherDto> jsonList = new ArrayList<>();
        List<String> weatherList = new ArrayList<>();
        String content = "";
        WeatherInfoDto adto = new WeatherInfoDto();
        WeatherInfoDto bdto = new WeatherInfoDto();

        // 포맷 정의
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        // 포맷 적용
        String formatDate = LocalDate.now().format(formatter);

        for (Object o : jsonItemList) {
            JSONObject item = (JSONObject) o;
            // WeatherDto dto = new WeatherDto((JSONObject) o);
            // List로 8시, 18시 추가
            // 0번째는 출근길 온도는 10도, 날씨는 맑고
            // 1번째는 퇴근길 온도는 7도, 날씨는 비가 오며 강수확률 POP 강수량은 10mm(PCP)
            // REH 습도(여름) SNO 적설량(겨울)
            // 퇴근시 우산을 챙기세요
            if (formatDate.equals(item.get("fcstDate").toString()) && "0800".equals(item.get("fcstTime").toString())) {
                adto.mapper(item.get("fcstTime").toString(), item.get("category").toString(),
                        item.get("fcstValue").toString());

            } else if (formatDate.equals(item.get("fcstDate").toString())
                    && "1800".equals(item.get("fcstTime").toString())) {
                bdto.mapper(item.get("fcstTime").toString(), item.get("category").toString(),
                        item.get("fcstValue").toString());
            }
        }

        adto.weatherMessage();
        bdto.weatherMessage();

        System.out.println(adto.getMessage());
        System.out.println(bdto.getMessage());
        int count = weatherList.size();

    }

    @GetMapping("/weather/{userId}")
    public ResponseEntity<List<ResponseMap>> getWeather(@PathVariable("userId") String userId,
            @ModelAttribute("api") WeatherAPIDto api) throws ParseException {

        List<ResponseMap> list = new ArrayList<>();
        // 59, 126
        // 61, 125
        getWeather(api);

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
