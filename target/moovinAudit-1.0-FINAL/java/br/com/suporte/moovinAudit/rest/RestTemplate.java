package br.com.suporte.moovinAudit.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class RestTemplate {

    private static final Logger logger = Logger.getLogger(RestTemplate.class.getName());
    public static Map<String,Object> sendRequest(String url,
                                                  String method,
                                                  Map<String, String> body) throws IOException {
        URL requestUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);

        if (body != null ) {
            StringBuilder postData = new StringBuilder();
            for(Map.Entry<String,String> param: body.entrySet()){
                if(postData.length() != 0){
                    postData.append('&');
                }
                postData.append(param.getKey());
                postData.append('=');
                postData.append(param.getValue());
            }
            byte[] postDataByte = postData.toString().getBytes(StandardCharsets.UTF_8);
            connection.setRequestProperty("Content-Lenght",String.valueOf(postDataByte.length));
            try(OutputStream outputStream = connection.getOutputStream()){
                outputStream.write(postDataByte);
                outputStream.flush();
            }
        }

        int responseCode = connection.getResponseCode();
        StringBuilder response = new StringBuilder();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            String erroLine;
            while ((erroLine = in.readLine()) != null) {
                response.append(erroLine);
            }
            in.close();
        }
        connection.disconnect();
        if(response.toString().isEmpty()) {
            logger.warning("A resposta está vazia!");
        }else if(responseCode != 200){
            logger.warning("erro: " + response.toString());
        }else{
            Map<String,Object> data = convertPayloadToListOfMaps(response.toString());
            return data;
        }
        return  null;
    }

    public static Map<String,Object> convertPayloadToListOfMaps(String data) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String,Object> listofmaps = objectMapper.readValue(data, new TypeReference<Map<String,Object>>() {
        });
        return listofmaps;
    }
}

