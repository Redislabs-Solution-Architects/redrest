package com.redislabs.redrest;

import java.util.Map;

import lombok.Data;

@Data
public class SearchResult {
	private String id;
	private double score;
	private byte[] payload;
	private Map<String, Object> fields;
}
