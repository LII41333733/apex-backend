package com.project.apex.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Convert {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String objectToString(Object object) throws JsonProcessingException {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper.writeValueAsString(object);
    }

    public static Long getMomentAsCode() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/New_York"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMHHmmyyyyss");
        return Long.valueOf(now.format(formatter));
    }

    public static double roundedDouble(double value) {
        BigDecimal bd = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();

    }
}
