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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * @author Rob Winch
 * @since 5.0
 */
@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
public class HelloMethodApplicationTests {

	@Autowired
	WebTestClient rest;

	// --- /message ---

	@Test
	void messageWhenNotAuthenticatedThenUnAuthorized() {
		// @formatter:off
		this.rest.get()
				.uri("/message")
				.exchange().
				expectStatus().isUnauthorized();
		// @formatter:on
	}

	@Test
	@WithMockUser
	void messageWhenAuthenticatedThenOk() {
		// @formatter:off
		this.rest.get()
				.uri("/message")
				.exchange()
				.expectStatus().isOk();
		// @formatter:on
	}

	// --- /secret ---

	@Test
	void secretWhenNotAuthenticatedThenUnAuthorized() {
		// @formatter:off
		this.rest.get()
				.uri("/secret")
				.exchange()
				.expectStatus().isUnauthorized();
		// @formatter:on
	}

	@Test
	@WithMockUser
	void secretWhenNotAuthorizedThenForbidden() {
		// @formatter:off
		this.rest.get()
				.uri("/secret")
				.exchange()
				.expectStatus().isForbidden();
		// @formatter:on
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void secretWhenAuthorizedThenOk() {
		// @formatter:off
		this.rest.get()
				.uri("/secret")
				.exchange()
				.expectStatus().isOk();
		// @formatter:on
	}

}
