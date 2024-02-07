package com.tamnaju.dev.configs.api;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.beans.Encoder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class Weather {

    public static void main(String[] args) throws IOException {
        // 기상청 API 요청 URL
        String apiUrl = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?serviceKey=hFAsHSPD%2BAwBzzeNDkOBJTRQMZrl%2B7He3Xb6y8RGldoSqs1MsVo9uTpcWvFw23oRbRMgUDEN3q%2FpHOocGTpopg%3D%3D&pageNo=1&numOfRows=1000&dataType=JSON&base_date=20240116&base_time=0500&nx=55&ny=127";

        // 일반 인증키 : hFAsHSPD%2BAwBzzeNDkOBJTRQMZrl%2B7He3Xb6y8RGldoSqs1MsVo9uTpcWvFw23oRbRMgUDEN3q%2FpHOocGTpopg%3D%3D
//        String apiUrl = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?serviceKey=(인증키)&pageNo=1&numOfRows=1000&dataType=JSON&base_date=20240115&base_time=0500&nx=55&ny=127";


        // HttpClient 생성
        HttpClient httpClient = HttpClientBuilder.create().build();

        // HTTP GET 요청
        HttpGet request = new HttpGet(apiUrl);
        HttpResponse response = httpClient.execute(request);


        // 응답 처리
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }
}
