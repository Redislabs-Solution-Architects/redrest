package com.redislabs.redrest.redisearch;

import io.redisearch.client.AddOptions.ReplacementPolicy;
import lombok.Data;

@Data
public class AddDocumentOptions {

	private boolean noSave;
	private String language;
	private ReplacementPolicy replacementPolicy = ReplacementPolicy.NONE;
}
