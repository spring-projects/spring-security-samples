/*
 * Copyright 2024 the original author or authors.
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class FormLoginOAuth2ApplicationTests {

	@Autowired
	MockMvc mvc;

	@Test
	@WithMockUser
	void indexWhenAuthenticatedThenAllows() throws Exception {
		this.mvc.perform(get("/"))
			.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	void profileWhenAuthenticatedThenRedirectsToAuthorizationServer() throws Exception {
		this.mvc.perform(get("/profile"))
			.andExpect(status().is3xxRedirection())
			.andExpect(header().string("Location", startsWith("https://accounts.google.com/o/oauth2/v2/auth?response_type=code&client_id=id&scope=https://www.googleapis.com/auth/gmail.readonly")));
	}

	@Test
	@WithMockUser(authorities = "SCOPE_" + SecurityConfig.SCOPE)
	void profileWhenAuthenticatedWithScopeThenAllows() throws Exception {
		this.mvc.perform(get("/profile"))
			.andExpect(status().isOk());
	}

}
