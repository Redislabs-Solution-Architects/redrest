package com.redislabs.redrest.redisearch;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/suggestions")
public class SuggestController {

	@Autowired
	private RediSearchTemplate template;

	@PostMapping("/{index}")
	public ResponseEntity<Object> add(@PathVariable("index") String index, @RequestBody AddSuggestions suggestions) {
		for (Suggestion suggestion : suggestions.getSuggestions()) {
			template.addSuggestion(index, suggestion, suggestions.isIncrement());
		}
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{index}")
	public List<Suggestion> getSuggestion(@PathVariable("index") String index, @RequestParam("prefix") String prefix,
			@RequestParam(name = "fuzzy", required = false) boolean fuzzy,
			@RequestParam(name = "max", required = false) Integer max,
			@RequestParam(name = "withPayloads", required = false) boolean withPayloads,
			@RequestParam(name = "withScores", required = false) boolean withScores) {
		return template.getSuggestion(index, prefix, fuzzy, max, withPayloads, withScores);
	}

}
