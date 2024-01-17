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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link OAuth2ResourceServerApplication}.
 *
 * @author Josh Cummings
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OAuth2ResourceServerApplicationITests {

	String noScopesToken = "00ed5855-1869-47a0-b0c9-0f3ce520aee7";

	String messageReadToken = "b43d1500-c405-4dc9-b9c9-6cfd966c34c9";

	@Autowired
	MockMvc mvc;

	@Test
	void performWhenValidBearerTokenThenAllows() throws Exception {

		// @formatter:off
		this.mvc.perform(get("/").with(bearerToken(this.noScopesToken)))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Hello, subject!")));
		// @formatter:on
	}

	// -- tests with scopes

	@Test
	void performWhenValidBearerTokenThenScopedRequestsAlsoWork() throws Exception {

		// @formatter:off
		this.mvc.perform(get("/message").with(bearerToken(this.messageReadToken)))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("secret message")));
		// @formatter:on
	}

	@Test
	void performWhenInsufficientlyScopedBearerTokenThenDeniesScopedMethodAccess() throws Exception {

		// @formatter:off
		this.mvc.perform(get("/message").with(bearerToken(this.noScopesToken)))
				.andExpect(status().isForbidden())
				.andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE,
						containsString("Bearer error=\"insufficient_scope\"")));
		// @formatter:on
	}

	private static BearerTokenRequestPostProcessor bearerToken(String token) {
		return new BearerTokenRequestPostProcessor(token);
	}

	private static class BearerTokenRequestPostProcessor implements RequestPostProcessor {

		private String token;

		BearerTokenRequestPostProcessor(String token) {
			this.token = token;
		}

		@Override
		public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
			request.addHeader("Authorization", "Bearer " + this.token);
			return request;
		}

	}

}
