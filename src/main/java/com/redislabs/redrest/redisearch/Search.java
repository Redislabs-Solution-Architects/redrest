package com.redislabs.redrest.redisearch;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Search {
	private String query;
	private int offset;
	private Integer limit;
	private String sortBy;
	private boolean sortAscending;
	private String language;
	private boolean noContent;
	private boolean noStopWords;
	private boolean verbatim;
	private boolean withPayloads;
	private boolean withScores;
}
