package com.redislabs.redrest;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "")
@EnableAutoConfiguration
@Data
public class RedrestConfiguration {

	private String rediSearchHost;
	private Integer rediSearchPort;

}