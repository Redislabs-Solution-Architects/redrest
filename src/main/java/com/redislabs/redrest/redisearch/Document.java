package com.redislabs.redrest.redisearch;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class Document {

	private String id;
	private Double score;
	private byte[] payload;
	private Map<String, Object> fields = new HashMap<>();
}
