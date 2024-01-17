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

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests.
 *
 * @author Rob Winch
 * @since 5.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HelloMethodApplicationITests {

	@Autowired
	WebTestClient rest;

	// --- /message ---

	@Test
	void messageWhenNotAuthenticated() {
		// @formatter:off
		this.rest.get()
				.uri("/message")
				.exchange()
				.expectStatus().isUnauthorized();
		// @formatter:on
	}

	@Test
	void messageWhenUserThenOk() {
		// @formatter:off
		this.rest.get()
			.uri("/message")
			.headers(userCredentials())
			.exchange()
			.expectStatus().isOk();
		// @formatter:on
	}

	// --- /secret ---

	@Test
	void secretWhenNotAuthenticated() {
		// @formatter:off
		this.rest.get()
			.uri("/secret")
			.exchange()
			.expectStatus().isUnauthorized();
		// @formatter:on
	}

	@Test
	void secretWhenUserThenForbidden() {
		// @formatter:off
		this.rest.get()
			.uri("/secret")
			.headers(userCredentials())
			.exchange()
			.expectStatus().isForbidden();
		// @formatter:on
	}

	@Test
	void secretWhenAdminThenOk() {
		// @formatter:off
		this.rest.get()
			.uri("/secret")
			.headers(adminCredentials())
			.exchange()
			.expectStatus().isOk()
			.expectBody(String.class).isEqualTo("Hello Admin!");
		// @formatter:on
	}

	private Consumer<HttpHeaders> userCredentials() {
		return (httpHeaders) -> httpHeaders.setBasicAuth("user", "password");
	}

	private Consumer<HttpHeaders> adminCredentials() {
		return (httpHeaders) -> httpHeaders.setBasicAuth("admin", "password");
	}

}
