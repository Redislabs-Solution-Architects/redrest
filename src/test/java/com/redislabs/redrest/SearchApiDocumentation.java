package com.redislabs.redrest;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redislabs.lettusearch.search.CreateOptions;
import com.redislabs.lettusearch.search.DropOptions;
import com.redislabs.lettusearch.search.Schema;
import com.redislabs.lettusearch.search.field.Field;
import com.redislabs.redrest.redisearch.Drop;
import com.redislabs.redrest.redisearch.RediSearchTemplate;
import com.redislabs.redrest.redisearch.Search;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SearchApiDocumentation {

	private final static String INDEXES = "/indexes";
	private final static String TESTINDEX = INDEXES + "/testIndex";
	private final static String SUGGESTIONS = "/suggestions";
	private final static String SUGGESTIONINDEX = SUGGESTIONS + "/suggestionIndex";
	private final static String SEARCH = TESTINDEX + "/search";
	@Rule
	public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private WebApplicationContext context;
	@Autowired
	private StringRedisTemplate template;
	private MockMvc mockMvc;
	@Autowired
	private RediSearchTemplate redisearch;

	@Before
	public void setUp() throws Exception {
		this.template.getConnectionFactory().getConnection().flushAll();
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
				.apply(documentationConfiguration(this.restDocumentation)).build();
		String index = "testIndex";
		try {
			redisearch.execute(c -> c.drop(index, DropOptions.builder().keepDocs(false).build()));
		} catch (Exception e) {
			// ignore
		}
		Schema schema = Schema.builder().field(Field.text("field1")).field(Field.numeric("field2").sortable(true))
				.build();
		redisearch.execute(c -> c.create(index, schema,
				CreateOptions.builder().stopWords(Arrays.asList("a", "i", "the")).build()));
		redisearch.execute(c -> c.add(index, "doc1", 1.0, fields("malibu", null), null, "payload1"));
		redisearch.execute(c -> c.add(index, "doc2", 1.0, fields("santa monica", 123.321), null, "payload2"));
		redisearch.execute(c -> c.add(index, "doc3", 1.0, fields("santa cruz", 122.0), null, null));
		String suggestIndex = "suggestionIndex";
		redisearch.execute(c -> c.sugadd(suggestIndex, "suggestion1", 1.0));
		redisearch.execute(c -> c.sugadd(suggestIndex, "suggestion2", 1.0));
		redisearch.execute(c -> c.sugadd(suggestIndex, "suggestion3", 1.0));

	}

	@Test
	public void info() throws Exception {
		this.mockMvc.perform(get(TESTINDEX)).andExpect(status().isOk()).andDo(document("get-index-info"));
	}

	@Test
	public void dropIndex() throws Exception {
		Drop drop = Drop.builder().keepDocs(false).build();
		this.mockMvc.perform(delete(TESTINDEX).contentType(MediaType.APPLICATION_JSON).content(toJson(drop)))
				.andExpect(status().isOk()).andDo(document("drop-index", requestFields(fieldWithPath("keepDocs")
						.type(JsonFieldType.BOOLEAN).description("Do not throw error if index does not exist"))));
	}

	private String toJson(Object object) throws JsonProcessingException {
		return this.objectMapper.writeValueAsString(object);
	}

	private Map<String, String> fields(String field1, Double field2) {
		Map<String, String> fields = new HashMap<>();
		fields.put("field1", field1);
		if (field2 != null) {
			fields.put("field2", String.valueOf(field2));
		}
		return fields;
	}

	@Test
	public void search() throws Exception {
		String query = "santa @field2:[120 124]";
		this.mockMvc
				.perform(post(SEARCH).contentType(MediaType.APPLICATION_JSON)
						.content(toJson(Search.builder().query(query).build())))
				.andExpect(status().isOk()).andDo(document("search", relaxedRequestFields(
						fieldWithPath("query").type(JsonFieldType.STRING).description("Query string"))));
	}

	@Test
	public void getSuggestions() throws Exception {
		this.mockMvc.perform(get(SUGGESTIONINDEX).param("prefix", "sug")).andExpect(status().isOk())
				.andDo(document("get-suggestions", requestParameters(parameterWithName("prefix").description("Prefix"),
						parameterWithName("fuzzy").optional().description("Fuzzy matching"),
						parameterWithName("max").optional().description("Max number of matches"),
						parameterWithName("withPayloads").optional().description("Include payloads with matches"),
						parameterWithName("withScores").optional().description("Include scores with matches"))));
	}
}