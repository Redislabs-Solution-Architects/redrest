package com.redislabs.redrest.redisearch;

import java.util.List;

import lombok.Data;

@Data
public class AddDocuments {

	private List<Document> documents;
	private AddDocumentOptions options = new AddDocumentOptions();

}
