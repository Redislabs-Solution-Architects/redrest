package com.redislabs.redrest.redisearch;

import lombok.Data;

@Data
public class Field {

	private String name;
	private boolean sortable;
	private boolean noIndex;
	private FieldType type;

}