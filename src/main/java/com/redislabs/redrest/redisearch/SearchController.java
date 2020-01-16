package com.redislabs.redrest.redisearch;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.redislabs.lettusearch.search.DropOptions;
import com.redislabs.lettusearch.search.SearchResults;

@RestController
@RequestMapping("/indexes")
public class SearchController {

	@Autowired
	private RediSearchTemplate template;

	@GetMapping("/{index}")
	public List<Object> info(@PathVariable("index") String index) throws Exception {
		return template.info(index);
	}

	@DeleteMapping("/{index}")
	public String drop(@PathVariable("index") String index, @RequestBody Drop drop) throws Exception {
		return template.drop(index, DropOptions.builder().keepDocs(drop.isKeepDocs()).build());
	}

	@PostMapping("/{index}/search")
	public SearchResults<String, String> search(@PathVariable("index") String index, @RequestBody Search search)
			throws Exception {
		return template.search(index, search.getQuery());
	}

}