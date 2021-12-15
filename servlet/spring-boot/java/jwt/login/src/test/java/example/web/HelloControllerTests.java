/*
 * Copyright 2002-2020 the original author or authors.
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

package example.web;

import example.RestConfig;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link HelloController}
 *
 * @author Josh Cummings
 */
@WebMvcTest({ HelloController.class, TokenController.class })
@Import(RestConfig.class)
public class HelloControllerTests {

	@Autowired
	MockMvc mvc;

	@Test
	void rootWhenAuthenticatedThenSaysHelloUser() throws Exception {
		// @formatter:off
		MvcResult result = this.mvc.perform(post("/token")
			.with(httpBasic("user", "password")))
			.andExpect(status().isOk())
			.andReturn();

		String token = result.getResponse().getContentAsString();

		this.mvc.perform(get("/")
			.header("Authorization", "Bearer " + token))
			.andExpect(content().string("Hello, user!"));
		// @formatter:on
	}

	@Test
	void rootWhenUnauthenticatedThen401() throws Exception {
		// @formatter:off
		this.mvc.perform(get("/"))
				.andExpect(status().isUnauthorized());
		// @formatter:on
	}

	@Test
	void tokenWhenBadCredentialsThen401() throws Exception {
		// @formatter:off
		this.mvc.perform(post("/token"))
				.andExpect(status().isUnauthorized());
		// @formatter:on
	}

}
