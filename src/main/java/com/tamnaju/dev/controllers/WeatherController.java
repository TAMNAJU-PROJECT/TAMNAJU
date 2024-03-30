package com.tamnaju.dev.controllers;

import lombok.Data;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;


@Controller
@RequestMapping("/jejumap")
public class WeatherController
{

    @GetMapping("/weather")
    public void getWeather(){ // @RespondBody 는 현재 창에서 작업하겠다는 뜻이다. 따라서 @RespondBody를 여기서 사용할 시 Controller 와 View 를 따로 연결해줘야 웹 페이지를 불러왔을 때 화면이 보이게 된다.


        // 기상청 API 요청 URL
//        String url = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?serviceKey=hFAsHSPD%2BAwBzzeNDkOBJTRQMZrl%2B7He3Xb6y8RGldoSqs1MsVo9uTpcWvFw23oRbRMgUDEN3q%2FpHOocGTpopg%3D%3D&pageNo=1&numOfRows=100&dataType=JSON&base_date=20240122&base_time=0500&nx=55&ny=127";
//
//
//        //Request Header
//        HttpHeaders headers = new HttpHeaders();
//
//        //parameter(생략)
//        //MultiValueMap<String,String> params =new LinkedMultiValueMap<>();
//
//        //Header + Parameter 단위 생성
//        //HttpEntity<MultiValueMap<String,String>> entity = new HttpEntity(headers);
//
//        //Restamplate에 HttpEntity등록
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<Root> resp =  restTemplate.exchange(url, HttpMethod.GET,null,Root.class);
////        System.out.println("[CustomLogoutHandler] logout() resp "+resp);
//        System.out.println("[CustomLogoutHandler] logout() resp.getBody() "+resp.getBody());
//        ArrayList<Item> item =  resp.getBody().response.getBody().getItems().getItem();
//        for(Item i :item){
//            System.out.println(i);
//        }
//
    }
}
//@Data
//class Body{
//    public String dataType;
//    public Items items;
//    public int pageNo;
//    public int numOfRows;
//    public int totalCount;
//}
//
//@Data
//class Header{
//    public String resultCode;
//    public String resultMsg;
//}
//
//@Data
//class Item{
//    public String baseDate;
//    public String baseTime;
//    public String category;
//    public String fcstDate;
//    public String fcstTime;
//    public String fcstValue;
//    public int nx;
//    public int ny;
//}
//
//@Data
//class Items{
//    public ArrayList<Item> item;
//}
//
//@Data
//class Response{
//    public Header header;
//    public Body body;
//}
//
//@Data
//class Root{
//    public Response response;
//}
