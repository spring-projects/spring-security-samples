/*
 * Copyright 2002-2019 the original author or authors.
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Jérôme Wacongne &lt;ch4mp@c4-soft.com&gt;
 * @author Josh Cummings
 * @since 5.2.0
 *
 */
@WebMvcTest(OAuth2ResourceServerController.class)
@Import(OAuth2ResourceServerSecurityConfiguration.class)
public class OAuth2ResourceServerControllerTests {

	@Autowired
	MockMvc mockMvc;

	@Test
	void indexGreetsAuthenticatedUser() throws Exception {
		// @formatter:off
		this.mockMvc.perform(get("/").with(jwt().jwt((jwt) -> jwt.subject("ch4mpy"))))
				.andExpect(content().string(is("Hello, ch4mpy!")));
		// @formatter:on
	}

	@Test
	void messageCanBeReadWithScopeMessageReadAuthority() throws Exception {
		// @formatter:off
		this.mockMvc.perform(get("/message").with(jwt().jwt((jwt) -> jwt.claim("scope", "message:read"))))
				.andExpect(content().string(is("secret message")));

		this.mockMvc.perform(get("/message").with(jwt().authorities(new SimpleGrantedAuthority(("SCOPE_message:read")))))
				.andExpect(content().string(is("secret message")));
		// @formatter:on
	}

	@Test
	void messageCanNotBeReadWithoutScopeMessageReadAuthority() throws Exception {
		// @formatter:off
		this.mockMvc.perform(get("/message").with(jwt()))
				.andExpect(status().isForbidden());
		// @formatter:on
	}

	@Test
	void messageCanNotBeCreatedWithoutAnyScope() throws Exception {
		// @formatter:off
		this.mockMvc.perform(post("/message")
				.content("Hello message")
				.with(jwt()))
				.andExpect(status().isForbidden());
		// @formatter:on
	}

	@Test
	void messageCanNotBeCreatedWithScopeMessageReadAuthority() throws Exception {
		// @formatter:off
		this.mockMvc.perform(post("/message")
				.content("Hello message")
				.with(jwt().jwt((jwt) -> jwt.claim("scope", "message:read"))))
				.andExpect(status().isForbidden());
		// @formatter:on
	}

	@Test
	void messageCanBeCreatedWithScopeMessageWriteAuthority() throws Exception {
		// @formatter:off
		this.mockMvc.perform(post("/message")
				.content("Hello message")
				.with(jwt().jwt((jwt) -> jwt.claim("scope", "message:write"))))
				.andExpect(status().isOk())
				.andExpect(content().string(is("Message was created. Content: Hello message")));
		// @formatter:on
	}

}
