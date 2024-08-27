package com.project.apex.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Convert {

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String objectToString(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }
}
