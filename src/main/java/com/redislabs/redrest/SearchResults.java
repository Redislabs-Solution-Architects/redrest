package com.redislabs.redrest;

import java.util.List;

import lombok.Data;

@Data
public class SearchResults {

	private long numberOfResults;
	private List<SearchResult> documents;

}
