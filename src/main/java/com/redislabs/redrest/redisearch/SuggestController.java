package com.redislabs.redrest.redisearch;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.redislabs.lettusearch.suggest.SuggestResult;

@RestController
@RequestMapping("/suggestions")
public class SuggestController {

	@Autowired
	private RediSearchTemplate template;

	@GetMapping("/{index}")
	public List<SuggestResult<String>> getSuggestions(@PathVariable("index") String index,
			@RequestParam("prefix") String prefix, @RequestParam(name = "fuzzy", required = false) boolean fuzzy,
			@RequestParam(name = "max", required = false) Long max,
			@RequestParam(name = "withPayloads", required = false) boolean withPayloads,
			@RequestParam(name = "withScores", required = false) boolean withScores) throws Exception {
		return template.sugget(index, prefix, fuzzy, withScores, withPayloads, max);
	}

}
