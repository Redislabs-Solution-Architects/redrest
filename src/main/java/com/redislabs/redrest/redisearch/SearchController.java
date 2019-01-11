package com.redislabs.redrest.redisearch;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.redislabs.redrest.SearchResults;

import io.redisearch.Query;

@RestController
@RequestMapping("/indexes")
public class SearchController {

	@Autowired
	private RediSearchTemplate template;

	@PostMapping
	public boolean create(@RequestBody CreateIndex options) {
		return template.create(options.getIndex(), options);
	}

	@GetMapping("/{index}")
	public Map<String, Object> info(@PathVariable("index") String index) {
		return template.getInfo(index);
	}

	@DeleteMapping("/{index}")
	public ResponseEntity<Boolean> drop(@PathVariable("index") String index, @RequestBody DropIndex options) {
		boolean dropped = template.drop(index, options);
		return ResponseEntity.ok(dropped);
	}

	@PostMapping("/{index}/documents")
	public ResponseEntity<Object> add(@PathVariable("index") String index, @RequestBody AddDocuments add) {
		for (Document document : add.getDocuments()) {
			template.add(index, document, add.getOptions());
		}
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{index}/documents")
	public ResponseEntity<Object> deleteDocuments(@PathVariable("index") String index, @RequestBody String[] docIds) {
		template.delete(index, docIds);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{index}/documents/{docId}")
	public boolean delete(@PathVariable("index") String index, @PathVariable("docId") String docId) {
		template.delete(index, docId);
		return true;
	}

	@PostMapping("/{index}/search")
	public SearchResults search(@PathVariable("index") String index, @RequestBody Search search) {
		Query q = new Query(search.getQuery());
		if (search.getLimit() != null) {
			q.limit(search.getOffset(), search.getLimit());
		}
		if (search.getSortBy() != null) {
			q.setSortBy(search.getSortBy(), search.isSortAscending());
		}
		if (search.getLanguage() != null) {
			q.setLanguage(search.getLanguage());
		}
		if (search.isNoContent()) {
			q.setNoContent();
		}
		if (search.isNoStopWords()) {
			q.setNoStopwords();
		}
		if (search.isVerbatim()) {
			q.setVerbatim();
		}
		if (search.isWithPayloads()) {
			q.setWithPayload();
		}
		if (search.isWithScores()) {
			q.setWithScores();
		}
		return template.search(index, q);
	}

}