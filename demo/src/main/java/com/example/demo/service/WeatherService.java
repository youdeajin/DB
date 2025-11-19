package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class WeatherService {

    @Value("${kma.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 서울 좌표 (기본값)
    private static final int NX = 60;
    private static final int NY = 127;

    public String getCurrentWeather() {
        try {
            // 1. 현재 시간 기준, 40분 전 시간을 base_time으로 설정 (기상청 데이터 생성 딜레이 고려)
            LocalDateTime now = LocalDateTime.now().minusMinutes(40);
            String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String baseTime = now.format(DateTimeFormatter.ofPattern("HH00")); // 정시 기준

            // 2. URL 생성 (초단기실황조회)
            StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst");
            urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + apiKey); /*Service Key*/
            urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
            urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("1000", "UTF-8")); /*한 페이지 결과 수*/
            urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode("JSON", "UTF-8")); /*요청자료형식(XML/JSON)*/
            urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(baseDate, "UTF-8")); /*‘21년 6월 28일 발표*/
            urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8")); /*06시 발표(정시단위) - 매시각 40분 이후 호출*/
            urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(NX), "UTF-8")); /*예보지점의 X 좌표값*/
            urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(NY), "UTF-8")); /*예보지점의 Y 좌표값*/

            URI uri = new URI(urlBuilder.toString());

            // 3. API 호출
            String jsonString = restTemplate.getForObject(uri, String.class);
            
            // 4. 응답 파싱 (PTY: 강수형태, SKY: 하늘상태 - 실황에는 SKY가 없을 수 있어 PTY 위주로 판단)
            JsonNode root = objectMapper.readTree(jsonString);
            JsonNode items = root.path("response").path("body").path("items").path("item");

            String pty = "0"; // 강수형태 (0:없음, 1:비, 2:비/눈, 3:눈, 5:빗방울, 6:빗방울눈날림, 7:눈날림)
            
            if (items.isArray()) {
                for (JsonNode item : items) {
                    if ("PTY".equals(item.path("category").asText())) {
                        pty = item.path("obsrValue").asText();
                        break;
                    }
                }
            }

            // 5. 날씨 상태 텍스트로 변환
            switch (pty) {
                case "1": return "Rainy";       // 비
                case "2": return "Rainy";       // 비/눈
                case "3": return "Snowy";       // 눈
                case "5": return "Rainy";       // 빗방울
                case "6": return "Snowy";       // 빗방울/눈날림
                case "7": return "Snowy";       // 눈날림
                default: return "Sunny";        // 강수 없음 (맑음으로 간주)
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Sunny"; // 에러 시 기본값
        }
    }
}