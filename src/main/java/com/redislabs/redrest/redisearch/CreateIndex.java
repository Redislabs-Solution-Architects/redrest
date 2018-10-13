package com.redislabs.redrest.redisearch;

import java.util.List;

import lombok.Data;

@Data
public class CreateIndex {

	private String index;
	private boolean maxTextFields;
	private boolean noOffsets;
	private boolean noHL;
	private boolean noFields;
	private boolean noFreqs;
	private List<String> stopWords;
	private List<Field> fields;

}
