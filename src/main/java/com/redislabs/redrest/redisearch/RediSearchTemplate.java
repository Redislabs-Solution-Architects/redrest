package com.redislabs.redrest.redisearch;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.redislabs.lettusearch.RediSearchCommands;
import com.redislabs.lettusearch.StatefulRediSearchConnection;
import com.redislabs.lettusearch.search.AddOptions;
import com.redislabs.lettusearch.search.DropOptions;
import com.redislabs.lettusearch.search.Schema;
import com.redislabs.lettusearch.search.SearchResults;
import com.redislabs.lettusearch.suggest.SuggestGetOptions;
import com.redislabs.lettusearch.suggest.SuggestResult;

@Component
public class RediSearchTemplate {

	@Autowired
	private GenericObjectPool<StatefulRediSearchConnection<String, String>> pool;

	public <O> O execute(Function<RediSearchCommands<String, String>, O> function) throws Exception {
		try (StatefulRediSearchConnection<String, String> connection = pool.borrowObject()) {
			return function.apply(connection.sync());
		}
	}

	public SearchResults<String, String> search(String index, String query) throws Exception {
		return execute(commands -> commands.search(index, query));
	}

	public void del(String index, String... docIds) throws Exception {
		for (String docId : docIds) {
			execute(redis -> redis.del(index, docId, true));
		}
	}

	public String create(String index, Schema schema) throws Exception {
		return execute(c -> c.create(index, schema));
	}

	public String add(String index, String docId, double score, Map<String, String> fields, AddOptions options,
			String payload) throws Exception {
		return execute(c -> c.add(index, docId, score, fields, options, payload));
	}

	public String drop(String index, DropOptions options) throws Exception {
		return execute(c -> c.drop(index, options));
	}

	public List<Object> info(String index) throws Exception {
		return execute(c -> c.indexInfo(index));
	}

	public Long sugadd(String index, String string, double score, boolean increment, String payload) throws Exception {
		return execute(c -> c.sugadd(index, string, score, increment, payload));
	}

	public List<SuggestResult<String>> sugget(String index, String prefix, boolean fuzzy, boolean withScores,
			boolean withPayloads, Long max) throws Exception {
		return execute(c -> c.sugget(index, prefix, SuggestGetOptions.builder().fuzzy(fuzzy).withScores(withScores)
				.withPayloads(withPayloads).max(max).build()));
	}

}
