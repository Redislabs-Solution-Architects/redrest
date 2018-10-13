package com.redislabs.redrest.redis;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/set")
public class SetController {

	@Autowired
	private StringRedisTemplate template;

	@PutMapping("/{key}")
	public Long add(@PathVariable(name = "key", required = true) String key, @RequestBody String value) {
		return template.opsForSet().add(key, value);
	}

	@PutMapping("/multi/{key}")
	public Long addMulti(@PathVariable(name = "key", required = true) String key, @RequestBody String[] values) {
		return template.opsForSet().add(key, values);
	}

	@DeleteMapping("/multi/{key}")
	public Long remove(@PathVariable(name = "key", required = true) String key, @RequestBody Object[] values) {
		return template.opsForSet().remove(key, values);
	}

	@GetMapping("/multi/{key}")
	public Set<String> members(@PathVariable(name = "key", required = true) String key) {
		return template.opsForSet().members(key);
	}

	@GetMapping("/intersect/{key}")
	public Set<String> intersect(@PathVariable(name = "key", required = true) String key,
			@RequestParam(name = "keys", required = true) Collection<String> otherKeys) {
		return template.opsForSet().intersect(key, otherKeys);
	}

	@GetMapping("/union/{key}")
	public Set<String> union(@PathVariable(name = "key", required = true) String key,
			@RequestParam(name = "keys", required = true) Collection<String> otherKeys) {
		return template.opsForSet().union(key, otherKeys);
	}

	@GetMapping("/{key}/is-member")
	public boolean isMember(@PathVariable(name = "key", required = true) String key,
			@RequestParam(name = "object", required = true) Object object) {
		return template.opsForSet().isMember(key, object);
	}

	@GetMapping("/pop/{key}")
	public List<String> pop(@PathVariable(name = "key", required = true) String key,
			@RequestParam(name = "count", required = true) long count) {
		return template.opsForSet().pop(key, count);
	}

}
