/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example;

import java.util.List;

import io.micrometer.observation.Observation.Context;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.observation.ClientRequestObservationContext;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.security.authentication.AuthenticationObservationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests.
 *
 * @author Rob Winch
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelloMethodApplicationITests {

	@Autowired
	TestRestTemplate rest;

	@Autowired
	ObservationCollector collector;

	// --- /message ---

	@Test
	void messageWhenNotAuthenticated() {
		// @formatter:off
		assertThat(this.rest.getForEntity("/message", String.class).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		List<Context> starts = this.collector.getStarts();
		assertThat(starts).extracting((context) -> context.getClass())
				.contains((Class) AuthenticationObservationContext.class)
				.contains((Class) AuthenticationObservationContext.class)
				.contains((Class) ClientRequestObservationContext.class)
				.contains((Class) ServerRequestObservationContext.class);
	}

	@Test
	void messageWhenUserThenOk() {
		ResponseEntity<?> response = this.rest.exchange("/message", HttpMethod.GET, userCredentials(), String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().toString()).isEqualTo("Hello User!");
		List<Context> starts = this.collector.getStarts();
		assertThat(starts).extracting((context) -> context.getClass())
				.contains((Class) AuthenticationObservationContext.class)
				.contains((Class) AuthenticationObservationContext.class)
				.contains((Class) ClientRequestObservationContext.class)
				.contains((Class) ServerRequestObservationContext.class);
	}

	// --- /secret ---

	@Test
	void secretWhenNotAuthenticated() {
		assertThat(this.rest.getForEntity("/secret", String.class).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		List<Context> starts = this.collector.getStarts();
		assertThat(starts).extracting((context) -> context.getClass())
				.contains((Class) AuthenticationObservationContext.class)
				.contains((Class) AuthenticationObservationContext.class)
				.contains((Class) ClientRequestObservationContext.class)
				.contains((Class) ServerRequestObservationContext.class);
	}

	@Test
	void secretWhenUserThenForbidden() {
		assertThat(this.rest.exchange("/secret", HttpMethod.GET, userCredentials(), String.class).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		List<Context> starts = this.collector.getStarts();
		assertThat(starts).extracting((context) -> context.getClass())
				.contains((Class) AuthenticationObservationContext.class)
				.contains((Class) AuthenticationObservationContext.class)
				.contains((Class) ClientRequestObservationContext.class)
				.contains((Class) ServerRequestObservationContext.class);
	}

	@Test
	void secretWhenAdminThenOk() {
		ResponseEntity<?> response = this.rest.exchange("/secret", HttpMethod.GET, adminCredentials(), String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().toString()).isEqualTo("Hello Admin!");
		List<Context> starts = this.collector.getStarts();
		assertThat(starts).extracting((context) -> context.getClass())
				.contains((Class) AuthenticationObservationContext.class)
				.contains((Class) AuthenticationObservationContext.class)
				.contains((Class) ClientRequestObservationContext.class)
				.contains((Class) ServerRequestObservationContext.class);
	}

	private HttpEntity<?> userCredentials() {
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth("user", "password");
		return new HttpEntity<>(headers);
	}

	private HttpEntity<?> adminCredentials() {
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth("admin", "password");
		return new HttpEntity<>(headers);
	}

}
