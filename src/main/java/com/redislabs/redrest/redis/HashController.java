package com.redislabs.redrest.redis;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;

@RestController
@RequestMapping("/hash")
public class HashController {

	@Data
	public static class PutRequest {

		private Object key;
		private Object value;
		private boolean ifAbsent;

	}

	@Autowired
	private StringRedisTemplate template;

	@GetMapping("/{key}")
	public Object get(@PathVariable(name = "key", required = true) String key,
			@RequestParam(name = "field", required = true) Object field) {
		return template.opsForHash().get(key, field);
	}

	@PutMapping("/{key}")
	public ResponseEntity<Object> put(@PathVariable(name = "key", required = true) String key,
			@RequestBody PutRequest put) {
		if (put.isIfAbsent()) {
			template.opsForHash().putIfAbsent(key, put.getKey(), put.getValue());
		} else {
			template.opsForHash().put(key, put.getKey(), put.getValue());
		}
		return ResponseEntity.ok().build();
	}

	@GetMapping("/all/{key}")
	public Map<Object, Object> entries(@PathVariable(name = "key", required = true) String key) {
		return template.opsForHash().entries(key);
	}

	@PutMapping("/all/{key}")
	public ResponseEntity<Object> putAll(@PathVariable(name = "key", required = true) String key,
			@RequestBody Map<Object, Object> fields) {
		template.opsForHash().putAll(key, fields);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/multi/{key}")
	public List<Object> multi(@PathVariable(name = "key", required = true) String key,
			@RequestParam(name = "fields", required = true) List<Object> fields) {
		return template.opsForHash().multiGet(key, fields);
	}

	@DeleteMapping("/multi/{key}")
	public Long delete(@PathVariable(name = "key", required = true) String key, @RequestBody List<Object> fields) {
		return template.opsForHash().delete(key, fields);
	}

	@GetMapping("/increment/{key}")
	public Double increment(@PathVariable(name = "key", required = true) String key,
			@RequestParam(name = "field", required = true) Object field,
			@RequestParam(name = "delta", required = true) double delta) {
		return template.opsForHash().increment(key, field, delta);
	}

	@GetMapping("/size/{key}")
	public Long size(@PathVariable(name = "key", required = true) String key) {
		return template.opsForHash().size(key);
	}

}
