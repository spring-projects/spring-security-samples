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
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Josh Cummings
 */
@SpringBootTest
@AutoConfigureMockMvc
public class HelloMethodApplicationTests {

	@Autowired
	MockMvc mvc;

	// --- /message ---

	@Test
	@WithAnonymousUser
	void messageWhenNotAuthenticatedThenUnAuthorized() throws Exception {
		this.mvc.perform(get("/message")).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser
	void messageWhenAuthenticatedThenOk() throws Exception {
		this.mvc.perform(get("/message")).andExpect(status().isOk());
	}

	// --- /secret ---

	@Test
	void secretWhenNotAuthenticatedThenUnAuthorized() throws Exception {
		this.mvc.perform(get("/secret")).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser
	void secretWhenNotAuthorizedThenForbidden() throws Exception {
		this.mvc.perform(get("/secret")).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void secretWhenAuthorizedThenOk() throws Exception {
		this.mvc.perform(get("/secret")).andExpect(status().isOk());
	}

}
