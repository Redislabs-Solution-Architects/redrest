package com.redislabs.redrest.redisearch;

import lombok.Data;

@Data
public class Suggestion {

	private String string;
	private Double score;
	private String payload;
}
