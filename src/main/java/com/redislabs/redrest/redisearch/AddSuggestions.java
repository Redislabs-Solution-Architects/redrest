package com.redislabs.redrest.redisearch;

import java.util.List;

import lombok.Data;

@Data
public class AddSuggestions {

	List<Suggestion> suggestions;
	boolean increment;

}
