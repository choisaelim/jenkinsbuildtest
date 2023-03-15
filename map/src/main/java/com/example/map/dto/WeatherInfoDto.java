package com.example.map.dto;

import lombok.Data;
import lombok.Getter;

enum SKY {
    NONE, 맑음, NON, 구름, 흐림
}

enum RAIN {
    없음, 비, 눈비, 눈, 소나기
}

@Data
public class WeatherInfoDto {
    private String tmp;
    private RAIN rain;
    private SKY sky;
    private String rainQty;
    private int rainExp;
    private String message;
    private String time;
    private int humi;
    private String snow;

    public void mapper(String time, String code, String value) {
        this.time = time;
        switch (code) {
            case "SKY":
                this.sky = SKY.values()[Integer.parseInt(value)];
                break;
            case "PTY":
                this.rain = RAIN.values()[Integer.parseInt(value)];
                break;
            case "TMP":
                this.tmp = value;
                break;
            case "POP":
                this.rainExp = Integer.parseInt(value);
                break;
            case "PCP":
                this.rainQty = value;
                break;
            case "REH":
                this.humi = Integer.parseInt(value);
                break;
            case "SNO":
                this.snow = value;
                break;
            default:
                break;
        }
    }

    public void weatherMessage() {

        this.message = "오늘";
        if ("0800".equals(this.time))
            this.message += " 출근길 날씨는";
        else if ("1800".equals(this.time))
            this.message += " 퇴근길 날씨는";
        this.message += " 기온 " + this.tmp + "도,";
        this.message += " 습도 " + this.humi + "%,";

        // 강수확률 있으면
        if (rainExp > 40) {
            switch (this.sky) {
                case 맑음:
                    this.message += " 하늘은 맑고";
                    break;
                case 구름:
                    this.message += " 구름이 많고";
                    break;
                case 흐림:
                    this.message += " 흐리고";
                    break;
                default:
                    break;
            }

            this.message += " 강수확률은 " + rainExp;
            switch (this.rain) {
                case 비:
                    this.message += " 비가 오고";
                    break;
                case 눈비:
                    this.message += " 눈 또는 비가 내리고";
                    break;
                case 눈:
                    this.message += " 눈이 오고";
                    break;
                case 소나기:
                    this.message += " 소나기가 오고";
                    break;
                default:
                    break;
            }

            this.message += " 강수량은 " + this.rainQty + "mm 입니다.";

        } else {
            switch (this.sky) {
                case 맑음:
                    this.message += " 하늘은 맑습니다.";
                    break;
                case 구름:
                    this.message += " 구름이 많습니다.";
                    break;
                case 흐림:
                    this.message += " 흐립니다.";
                    break;
                default:
                    break;
            }
        }

    }

}
