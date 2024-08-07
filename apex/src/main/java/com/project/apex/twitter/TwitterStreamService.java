//package com.project.apex.twitter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.http.*;
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.ByteArrayInputStream;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.*;
//
//@Service
//public class TwitterStreamService {
//
//    @Autowired
//    private TwitterConfig twitterConfig;
//
//    private final RestTemplate restTemplate;
//
//    public TwitterStreamService() {
//        this.restTemplate = new RestTemplate();
//    }
//
//    public void run() throws Exception {
//        String bearerToken = twitterConfig.getBearerToken();
//        if (bearerToken != null) {
//            Map<String, String> rules = new HashMap<>();
//            rules.put("cats has:images", "cat images");
//            rules.put("dogs has:images", "dog images");
//            setupRules(bearerToken, rules);
//            connectStream(bearerToken);
//        } else {
//            System.out.println("There was a problem getting your bearer token. Please make sure you set the BEARER_TOKEN environment variable");
//        }
//    }
//
//    private void connectStream(String bearerToken) throws Exception {
//        String url = "https://api.twitter.com/2/tweets/search/stream";
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(bearerToken);
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//
//        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
//
//        if (response.getStatusCode() == HttpStatus.OK) {
//            InputStream inputStream = new ByteArrayInputStream(response.getBody());
//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
//            }
//        }
//    }
//
//    private void setupRules(String bearerToken, Map<String, String> rules) throws Exception {
//        List<String> existingRules = getRules(bearerToken);
//        if (!existingRules.isEmpty()) {
//            deleteRules(bearerToken, existingRules);
//        }
//        createRules(bearerToken, rules);
//    }
//
//    private void createRules(String bearerToken, Map<String, String> rules) throws Exception {
//        String url = "https://api.twitter.com/2/tweets/search/stream/rules";
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(bearerToken);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        String body = getFormattedString("{\"add\": [%s]}", rules);
//        HttpEntity<String> entity = new HttpEntity<>(body, headers);
//
//        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
//        if (response.getStatusCode() == HttpStatus.OK) {
//            System.out.println(response.getBody());
//        }
//    }
//
//    private List<String> getRules(String bearerToken) throws Exception {
//        String url = "https://api.twitter.com/2/tweets/search/stream/rules";
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(bearerToken);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//
//        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
//
//        List<String> rules = new ArrayList<>();
//        if (response.getStatusCode() == HttpStatus.OK) {
//            JSONObject json = new JSONObject(response.getBody());
//            if (json.has("data")) {
//                JSONArray array = json.getJSONArray("data");
//                for (int i = 0; i < array.length(); i++) {
//                    JSONObject jsonObject = array.getJSONObject(i);
//                    rules.add(jsonObject.getString("id"));
//                }
//            }
//        }
//        return rules;
//    }
//
//    private void deleteRules(String bearerToken, List<String> existingRules) throws Exception {
//        String url = "https://api.twitter.com/2/tweets/search/stream/rules";
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(bearerToken);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        String body = getFormattedString("{ \"delete\": { \"ids\": [%s]}}", existingRules);
//        HttpEntity<String> entity = new HttpEntity<>(body, headers);
//
//        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
//        if (response.getStatusCode() == HttpStatus.OK) {
//            System.out.println(response.getBody());
//        }
//    }
//
//    private String getFormattedString(String string, List<String> ids) {
//        StringBuilder sb = new StringBuilder();
//        if (ids.size() == 1) {
//            return String.format(string, "\"" + ids.get(0) + "\"");
//        } else {
//            for (String id : ids) {
//                sb.append("\"").append(id).append("\"").append(",");
//            }
//            String result = sb.toString();
//            return String.format(string, result.substring(0, result.length() - 1));
//        }
//    }
//
//    private String getFormattedString(String string, Map<String, String> rules) {
//        StringBuilder sb = new StringBuilder();
//        if (rules.size() == 1) {
//            String key = rules.keySet().iterator().next();
//            return String.format(string, "{\"value\": \"" + key + "\", \"tag\": \"" + rules.get(key) + "\"}");
//        } else {
//            for (Map.Entry<String, String> entry : rules.entrySet()) {
//                String value = entry.getKey();
//                String tag = entry.getValue();
//                sb.append("{\"value\": \"").append(value).append("\", \"tag\": \"").append(tag).append("\"}").append(",");
//            }
//            String result = sb.toString();
//            return String.format(string, result.substring(0, result.length() - 1));
//        }
//    }
//}