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
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(SpringExtension.class)
@SpringJUnitWebConfig(classes = SecurityConfiguration.class)
public class MaxSessionTests {

	@Test
	void run(WebApplicationContext context) throws Exception {
		// @formatter:off
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context)
				.apply(springSecurity())
				.build();

		MvcResult mvcResult = mockMvc.perform(formLogin())
				.andExpect(authenticated())
				.andReturn();
		// @formatter:on

		MockHttpSession user1Session = (MockHttpSession) mvcResult.getRequest().getSession();

		// @formatter:off
		mockMvc.perform(get("/").session(user1Session))
				.andExpect(authenticated());
		// @formatter:on

		mockMvc.perform(formLogin()).andExpect(authenticated());

		// @formatter:off
		// session is terminated by user2
		mockMvc.perform(get("/").session(user1Session))
				.andExpect(unauthenticated());
		// @formatter:on
	}

}
