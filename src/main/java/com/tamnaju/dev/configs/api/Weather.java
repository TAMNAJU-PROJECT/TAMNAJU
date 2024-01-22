//package com.tamnaju.dev.configs.api;
//
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.util.MultiValueMap;
//
//import java.beans.Encoder;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.URLDecoder;
//import java.net.URLEncoder;
//
//public class Weather {
//
//    public static void main(String[] args) throws IOException {
//        // 기상청 API 요청 URL
//        String apiUrl = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?serviceKey=hFAsHSPD%2BAwBzzeNDkOBJTRQMZrl%2B7He3Xb6y8RGldoSqs1MsVo9uTpcWvFw23oRbRMgUDEN3q%2FpHOocGTpopg%3D%3D&pageNo=1&numOfRows=1000&dataType=JSON&base_date=20240116&base_time=0500&nx=55&ny=127";
//
//        // 일반 인증키 : hFAsHSPD%2BAwBzzeNDkOBJTRQMZrl%2B7He3Xb6y8RGldoSqs1MsVo9uTpcWvFw23oRbRMgUDEN3q%2FpHOocGTpopg%3D%3D
////        String apiUrl = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?serviceKey=(인증키)&pageNo=1&numOfRows=1000&dataType=JSON&base_date=20240115&base_time=0500&nx=55&ny=127";
//
//
//        //AccessToken 추출
//        String accessToken =  principalDetails.getAccessToken();
//        //Requeset URL
//        String url = "https://kapi.kakao.com/v1/user/logout";
//        //Request Header
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-Type","application/x-www-form-urlencoded");
//        headers.add("Authorization","Bearer "+accessToken);
//
//        //parameter(생략)
//        //MultiValueMap<String,String> params =new LinkedMultiValueMap<>();
//
//        //Header + Parameter 단위 생성
//        HttpEntity<MultiValueMap<String,String>> entity = new HttpEntity(headers);
//
//        //Restamplate에 HttpEntity등록
//        ResponseEntity<String> resp =  restTemplate.exchange(url, HttpMethod.POST,entity,String.class);
//        System.out.println("[CustomLogoutHandler] logout() resp "+resp);
//        System.out.println("[CustomLogoutHandler] logout() resp.getBody() "+resp.getBody());
//    }
//}
