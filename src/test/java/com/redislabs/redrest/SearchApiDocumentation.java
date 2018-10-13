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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redislabs.redrest.redisearch.AddDocuments;
import com.redislabs.redrest.redisearch.AddSuggestions;
import com.redislabs.redrest.redisearch.CreateIndex;
import com.redislabs.redrest.redisearch.Document;
import com.redislabs.redrest.redisearch.DropIndex;
import com.redislabs.redrest.redisearch.Field;
import com.redislabs.redrest.redisearch.FieldType;
import com.redislabs.redrest.redisearch.Search;
import com.redislabs.redrest.redisearch.Suggestion;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SearchApiDocumentation {

	private final static String INDEXES = "/indexes";
	private final static String TESTINDEX = INDEXES + "/testIndex";
	private final static String SUGGESTIONS = "/suggestions";
	private final static String SUGGESTIONINDEX = SUGGESTIONS + "/suggestionIndex";
	private final static String DOCUMENTS = TESTINDEX + "/documents";
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

	@Before
	public void setUp() {
		this.template.getConnectionFactory().getConnection().flushAll();
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
				.apply(documentationConfiguration(this.restDocumentation)).build();
	}

	@Test
	public void createIndex() throws Exception {
		doCreateIndex().andExpect(status().isOk()).andDo(document("create-index", requestFields(
				fieldWithPath("index").type(JsonFieldType.STRING).description("index name"),
				fieldWithPath("maxTextFields").type(JsonFieldType.BOOLEAN)
						.description("see https://oss.redislabs.com/redisearch/Commands/#ftcreate[MAXTEXTFIELDS]"),
				fieldWithPath("noOffsets").type(JsonFieldType.BOOLEAN)
						.description("see https://oss.redislabs.com/redisearch/Commands/#ftcreate[NOOFFSETS]"),
				fieldWithPath("noHL").type(JsonFieldType.BOOLEAN)
						.description("see https://oss.redislabs.com/redisearch/Commands/#ftcreate[NOHL]"),
				fieldWithPath("noFields").type(JsonFieldType.BOOLEAN)
						.description("see https://oss.redislabs.com/redisearch/Commands/#ftcreate[NOFIELDS]"),
				fieldWithPath("noFreqs").type(JsonFieldType.BOOLEAN)
						.description("see https://oss.redislabs.com/redisearch/Commands/#ftcreate[NOFREQS]"),
				fieldWithPath("stopWords").type(JsonFieldType.ARRAY)
						.description("see https://oss.redislabs.com/redisearch/Commands/#ftcreate[STOPWORDS]"),
				fieldWithPath("fields").type(JsonFieldType.ARRAY)
						.description("see https://oss.redislabs.com/redisearch/Commands/#ftcreate[SCHEMA]"),
				fieldWithPath("fields[].name").type(JsonFieldType.STRING).description("field name"),
				fieldWithPath("fields[].sortable").type(JsonFieldType.BOOLEAN)
						.description("allows the user to later sort the results by the value of this field"),
				fieldWithPath("fields[].noIndex").type(JsonFieldType.BOOLEAN).description("field will not be indexed"),
				fieldWithPath("fields[].type").type(JsonFieldType.STRING).description("text, numeric, or geo"))));
	}

	private ResultActions doCreateIndex() throws Exception {
		CreateIndex index = new CreateIndex();
		index.setIndex("testIndex");
		index.setStopWords(Arrays.asList("a", "i", "the"));
		Field field1 = new Field();
		field1.setName("field1");
		field1.setType(FieldType.text);
		Field field2 = new Field();
		field2.setName("field2");
		field2.setType(FieldType.numeric);
		field2.setSortable(true);
		index.setFields(Arrays.asList(field1, field2));
		return this.mockMvc.perform(post(INDEXES).contentType(MediaType.APPLICATION_JSON).content(toJson(index)));
	}

	@Test
	public void info() throws Exception {
		doCreateIndex();
		this.mockMvc.perform(get(TESTINDEX)).andExpect(status().isOk()).andDo(document("get-index-info"));
	}

	@Test
	public void dropIndex() throws Exception {
		doCreateIndex();
		DropIndex options = new DropIndex();
		options.setMissingOk(true);
		String json = toJson(options);
		this.mockMvc.perform(delete(TESTINDEX).contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andDo(document("drop-index", requestFields(fieldWithPath("missingOk")
						.type(JsonFieldType.BOOLEAN).description("Do not throw error if index does not exist"))));
	}

	private String toJson(Object object) throws JsonProcessingException {
		return this.objectMapper.writeValueAsString(object);
	}

	@Test
	public void addDocuments() throws Exception {
		doCreateIndex();
		doAddDocuments().andExpect(status().isOk()).andDo(document("add-documents", relaxedRequestFields(
				fieldWithPath("documents").type(JsonFieldType.ARRAY).description("An array of documents to add"),
				fieldWithPath("documents[].fields").description("Document fields"),
				fieldWithPath("documents[].id").type(JsonFieldType.STRING).description("Document id"),
				fieldWithPath("documents[].score").type(JsonFieldType.NUMBER).optional()
						.description("Document score, between 0.0 and 1.0"),
				fieldWithPath("documents[].payload").type(JsonFieldType.STRING).optional()
						.description("Document payload"),
				fieldWithPath("documents[].fields").type(JsonFieldType.OBJECT).description("Document fields"),
				fieldWithPath("options").type(JsonFieldType.OBJECT).description("Add options"),
				fieldWithPath("options.noSave").type(JsonFieldType.BOOLEAN).description("Do not save document fields"),
				fieldWithPath("options.language").type(JsonFieldType.STRING).description("Language of the documents"),
				fieldWithPath("options.replacementPolicy").type(JsonFieldType.STRING)
						.description("NONE, FULL, or PARTIAL"))));
	}

	@Test
	public void deleteDocuments() throws Exception {
		doCreateIndex();
		doAddDocuments();
		String json = toJson(new String[] { "doc1", "doc2" });
		this.mockMvc.perform(delete(DOCUMENTS).contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andDo(document("delete-documents"));
	}

	private ResultActions doAddDocuments() throws Exception {
		AddDocuments add = new AddDocuments();
		add.getOptions().setLanguage("english");
		add.setDocuments(Arrays.asList(newDoc("doc1", "payload1", 1.0, "malibu", null),
				newDoc("doc2", "payload2", null, "santa monica", 123.321),
				newDoc("doc3", null, null, "santa cruz", 122.0)));
		String json = toJson(add);
		return this.mockMvc.perform(post(DOCUMENTS).contentType(MediaType.APPLICATION_JSON).content(json));
	}

	private Document newDoc(String id, String payload, Double score, String field1, Double field2) {
		Document doc = new Document();
		doc.setId(id);
		if (payload != null) {
			doc.setPayload(payload.getBytes());
		}
		if (score != null) {
			doc.setScore(score);
		}
		Map<String, Object> fields = new HashMap<>();
		fields.put("field1", field1);
		if (field2 != null) {
			fields.put("field2", field2);
		}
		doc.setFields(fields);
		return doc;
	}

	@Test
	public void search() throws Exception {
		doCreateIndex();
		doAddDocuments();
		Search search = new Search();
		search.setWithPayloads(true);
		search.setWithScores(true);
		search.setLanguage("english");
		search.setSortBy("field2");
		search.setSortAscending(true);
		search.setVerbatim(true);
		search.setNoStopWords(true);
		search.setLimit(30);
		search.setQuery("santa @field2:[120 124]");
		this.mockMvc.perform(post(SEARCH).contentType(MediaType.APPLICATION_JSON).content(toJson(search)))
				.andExpect(status().isOk())
				.andDo(document("search",
						relaxedRequestFields(
								fieldWithPath("query").type(JsonFieldType.STRING).description("Query string"),
								fieldWithPath("withScores").type(JsonFieldType.BOOLEAN)
										.description("Include document scores in search results"),
								fieldWithPath("withPayloads").type(JsonFieldType.BOOLEAN)
										.description("Include document payloads in search results"),
								fieldWithPath("language").type(JsonFieldType.STRING)
										.description("Language to be used for search results"),
								fieldWithPath("sortBy").type(JsonFieldType.STRING)
										.description("Name of field to be used for sorting search results"),
								fieldWithPath("sortAscending").type(JsonFieldType.BOOLEAN)
										.description("Sort results in ascending order"),
								fieldWithPath("verbatim").type(JsonFieldType.BOOLEAN).description("Verbatim"),
								fieldWithPath("noStopWords").type(JsonFieldType.BOOLEAN).description("No stop words"),
								fieldWithPath("noContent").type(JsonFieldType.BOOLEAN).description("No content"),
								fieldWithPath("limit").type(JsonFieldType.NUMBER)
										.description("Number of search results"),
								fieldWithPath("offset").type(JsonFieldType.NUMBER)
										.description("Start index for search results"))));
	}

	private ResultActions doAddSuggestions() throws Exception {
		AddSuggestions add = new AddSuggestions();
		Suggestion suggestion1 = new Suggestion();
		suggestion1.setString("suggestion1");
		Suggestion suggestion2 = new Suggestion();
		suggestion2.setString("suggestion2");
		Suggestion suggestion3 = new Suggestion();
		suggestion3.setString("suggestion3");
		add.setSuggestions(Arrays.asList(suggestion1, suggestion2, suggestion3));
		return this.mockMvc.perform(post(SUGGESTIONINDEX).contentType(MediaType.APPLICATION_JSON).content(toJson(add)));
	}

	@Test
	public void addSuggestions() throws Exception {
		doAddSuggestions().andExpect(status().isOk()).andDo(document("add-suggestions", requestFields(
				fieldWithPath("suggestions").type(JsonFieldType.ARRAY).description("list of suggestions to add"),
				fieldWithPath("suggestions[].string").type(JsonFieldType.STRING).description("suggestion string"),
				fieldWithPath("suggestions[].payload").type(JsonFieldType.STRING).optional()
						.description("suggestion payload"),
				fieldWithPath("suggestions[].score").type(JsonFieldType.NUMBER).optional()
						.description("suggestion score"),
				fieldWithPath("increment").type(JsonFieldType.BOOLEAN).optional()
						.description("increment suggestion score"))));
	}

	@Test
	public void getSuggestions() throws Exception {
		doAddSuggestions();
		this.mockMvc.perform(get(SUGGESTIONINDEX).param("prefix", "sug")).andExpect(status().isOk())
				.andDo(document("get-suggestions", requestParameters(parameterWithName("prefix").description("Prefix"),
						parameterWithName("fuzzy").optional().description("Fuzzy matching"),
						parameterWithName("max").optional().description("Max number of matches"),
						parameterWithName("withPayloads").optional().description("Include payloads with matches"),
						parameterWithName("withScores").optional().description("Include scores with matches"))));
	}
}