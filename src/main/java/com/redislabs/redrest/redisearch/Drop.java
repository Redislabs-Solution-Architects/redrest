package com.redislabs.redrest.redisearch;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Drop {

	private boolean keepDocs;

}
