package com.redislabs.redrest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.stereotype.Component;

import io.redisearch.client.Client;

@Component
public class RediSearchClientConfiguration {

	private static final int DEFAULT_TIMEOUT = 1000;
	private static final int DEFAULT_POOLSIZE = 1;

	@Autowired
	private RedisProperties redisProps;

	@Autowired
	private RedrestConfiguration config;

	private Map<String, Client> clients = new HashMap<>();

	public Client getClient(String index) {
		if (!clients.containsKey(index)) {
			clients.put(index, new Client(index, getHost(), getPort(), getTimeout(), getPoolSize()));
		}
		return clients.get(index);
	}

	private int getPort() {
		if (config.getRediSearchPort() == null) {
			return redisProps.getPort();
		}
		return config.getRediSearchPort();
	}

	private String getHost() {
		if (config.getRediSearchHost() == null) {
			return redisProps.getHost();
		}
		return config.getRediSearchHost();
	}

	private int getPoolSize() {
		if (redisProps.getJedis().getPool() == null) {
			return DEFAULT_POOLSIZE;
		}
		return redisProps.getJedis().getPool().getMaxActive();
	}

	private int getTimeout() {
		if (redisProps.getTimeout() == null) {
			return DEFAULT_TIMEOUT;
		}
		return (int) redisProps.getTimeout().getSeconds();
	}
}
