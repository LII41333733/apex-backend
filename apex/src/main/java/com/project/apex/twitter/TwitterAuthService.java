//package com.project.apex.twitter;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.util.EntityUtils;
//import org.springframework.stereotype.Service;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.Base64;
//import java.util.Map;
//
//@Service
//public class TwitterAuthService {
//
//    private static final Logger logger = LoggerFactory.getLogger(TwitterAuthService.class);
//
//    public String getBearerToken() throws IOException {
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//
//        HttpPost httpPost = new HttpPost("https://api.twitter.com/oauth2/token");
//        String auth = "AAAAAAAAAAAAAAAAAAAAACt8vAEAAAAAUyYx5qjWzwSA3vGlCCxaN%2B%2B2cUk%3DLpz7fIMqEcdN9LLgARLZSFTKD44VmjdzfH4fGvYVOjU1x0w62k";
//
//        httpPost.setHeader("Authorization", "Bearer " + auth);
//        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
//        httpPost.setEntity(new StringEntity("grant_type=client_credentials"));
//
//        var response = httpClient.execute(httpPost);
//        var entity = response.getEntity();
//        String json = EntityUtils.toString(entity);
//
//        logger.info("Response: {}", json);
//
//        if (response.getStatusLine().getStatusCode() != 200) {
//            logger.error("Failed to obtain Bearer Token. Status: {}, Response: {}", response.getStatusLine(), json);
//            throw new IOException("Failed to obtain Bearer Token");
//        }
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        Map<String, String> result = objectMapper.readValue(json, Map.class);
//        return result.get("access_token");
//    }
//}