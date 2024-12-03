package com.project.apex.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.apex.data.websocket.WebSocketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record Record<T>(String type, T data) {

    private static final Logger logger = LoggerFactory.getLogger(Record.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)  // Enables pretty print
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public Record {
        if (!WebSocketData.QUOTE.name().equals(type)) {
            print(type, data);
        }
    }

    public void print(String type, T data) {
        try {
            String pretty = objectMapper.writeValueAsString(data);
            logger.debug("{}: {}", type, pretty);
        } catch (JsonProcessingException e) {
            logger.error("print: (JsonProcessingException) Error processing record for {}", type, e);
        } catch (Exception e) {
            logger.error("print: (Exception) Error processing record for {}", type, e);
        }
    }
}