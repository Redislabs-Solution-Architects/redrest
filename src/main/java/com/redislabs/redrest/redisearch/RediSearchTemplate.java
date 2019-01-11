package com.redislabs.redrest.redisearch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.redislabs.redrest.RediSearchClientConfiguration;
import com.redislabs.redrest.SearchResult;
import com.redislabs.redrest.SearchResults;

import io.redisearch.Query;
import io.redisearch.Schema;
import io.redisearch.Schema.FieldType;
import io.redisearch.Suggestion.Builder;
import io.redisearch.client.Client;
import io.redisearch.client.Client.IndexOptions;
import io.redisearch.client.SuggestionOptions;
import io.redisearch.client.SuggestionOptions.With;

@Component
public class RediSearchTemplate {

	@Autowired
	private RediSearchClientConfiguration rediSearchConfig;

	public SearchResults search(String index, Query q) {
		io.redisearch.SearchResult result = getClient(index).search(q);
		SearchResults results = new SearchResults();
		results.setNumberOfResults(result.totalResults);
		results.setDocuments(result.docs.stream().map(doc -> newSearchResult(doc)).collect(Collectors.toList()));
		return results;
	}

	private SearchResult newSearchResult(io.redisearch.Document doc) {
		SearchResult result = new SearchResult();
		result.setId(doc.getId());
		if (doc.getPayload() != null) {
			result.setPayload(doc.getPayload());
		}
		result.setScore(doc.getScore());
		Map<String, Object> fields = new HashMap<>();
		doc.getProperties().forEach(entry -> fields.put(entry.getKey(), entry.getValue()));
		result.setFields(fields);
		return result;
	}

	public void delete(String index, String... docIds) {
		for (String id : docIds) {
			getClient(index).deleteDocument(id);
		}
	}

	public boolean create(String indexName, CreateIndex index) {
		Schema schema = getSchema(index);
		IndexOptions options = getOptions(index);
		return getClient(indexName).createIndex(schema, options);
	}

	private Client getClient(String index) {
		return rediSearchConfig.getClient(index);
	}

	private IndexOptions getOptions(CreateIndex index) {
		IndexOptions options = IndexOptions.Default();
		return options;
	}

	private Schema getSchema(CreateIndex index) {
		Schema schema = new Schema();
		for (Field field : index.getFields()) {
			schema.addField(getField(field));
		}
		return schema;
	}

	private io.redisearch.Schema.Field getField(Field field) {
		return new io.redisearch.Schema.Field(field.getName(), getFieldType(field), field.isSortable(),
				field.isNoIndex());
	}

	private FieldType getFieldType(Field field) {
		switch (field.getType()) {
		case geo:
			return FieldType.Geo;
		case numeric:
			return FieldType.Numeric;
		default:
			return FieldType.FullText;
		}
	}

	public boolean add(String index, Document document, AddDocumentOptions options) {
		return getClient(index).addDocument(getDocument(document), getOptions(options));
	}

	private io.redisearch.Document getDocument(Document document) {
		return new io.redisearch.Document(document.getId(), document.getFields(), document.getScore(),
				document.getPayload());
	}

	private io.redisearch.client.AddOptions getOptions(AddDocumentOptions addOptions) {
		io.redisearch.client.AddOptions options = new io.redisearch.client.AddOptions();
		if (addOptions != null) {
			if (addOptions.getLanguage() != null) {
				options.setLanguage(addOptions.getLanguage());
			}
			options.setNosave(addOptions.isNoSave());
			options.setReplacementPolicy(addOptions.getReplacementPolicy());
		}
		return options;
	}

	public boolean drop(String index, DropIndex options) {
		return getClient(index).dropIndex(options.isMissingOk());
	}

	public Map<String, Object> getInfo(String index) {
		return getClient(index).getInfo();
	}

	public Long addSuggestion(String index, Suggestion suggestion, boolean increment) {
		Builder builder = io.redisearch.Suggestion.builder().str(suggestion.getString());
		if (suggestion.getPayload() != null) {
			builder.payload(suggestion.getPayload());
		}
		if (suggestion.getScore() != null) {
			builder.score(suggestion.getScore());
		}
		return getClient(index).addSuggestion(builder.build(), increment);
	}

	public List<Suggestion> getSuggestion(String index, String prefix, boolean fuzzy, Integer max, boolean withPayloads,
			boolean withScores) {
		io.redisearch.client.SuggestionOptions.Builder builder = SuggestionOptions.builder();
		if (fuzzy) {
			builder.fuzzy();
		}
		if (max != null) {
			builder.max(max);
		}
		With with = getWith(withPayloads, withScores);
		if (with != null) {
			builder.with(with);
		}
		return getClient(index).getSuggestion(prefix, builder.build()).stream().map(sug -> getSuggestion(sug))
				.collect(Collectors.toList());
	}

	private Suggestion getSuggestion(io.redisearch.Suggestion sug) {
		Suggestion suggestion = new Suggestion();
		suggestion.setPayload(sug.getPayload());
		suggestion.setScore(sug.getScore());
		suggestion.setString(sug.getString());
		return suggestion;
	}

	private With getWith(boolean withPayloads, boolean withScores) {
		if (withPayloads) {
			if (withScores) {
				return With.PAYLOAD_AND_SCORES;
			}
			return With.PAYLOAD;
		}
		if (withScores) {
			return With.SCORES;
		}
		return null;
	}
}
