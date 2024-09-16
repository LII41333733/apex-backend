package com.project.apex.data.trades;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


public class TradeLegMap extends HashMap<TradeLeg, JsonNode> {}