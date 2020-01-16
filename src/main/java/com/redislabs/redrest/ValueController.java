package com.redislabs.redrest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/value")
public class ValueController {

	@Autowired
	private StringRedisTemplate template;

	@GetMapping("/{key}")
	public String get(@PathVariable(name = "key", required = true) String key) {
		return template.opsForValue().get(key);
	}

	@PutMapping("/{key}")
	public ResponseEntity<Object> set(@PathVariable(name = "key", required = true) String key,
			@RequestBody String value) {
		template.opsForValue().set(key, value);
		return ResponseEntity.ok().build();
	}

	@GetMapping()
	public List<String> multi(@RequestParam(name = "keys", required = true) List<String> keys) {
		return template.opsForValue().multiGet(keys);
	}

	@PutMapping("/increment/{key}")
	public Double increment(@PathVariable(name = "key", required = true) String key, @RequestBody double delta) {
		return template.opsForValue().increment(key, delta);
	}

}
