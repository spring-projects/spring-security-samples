/*
 * Copyright 2023 the original author or authors.
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

package com.example.maximumsessionspreventlogin;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
public class MaximumSessionsPreventLoginTests {

	@Autowired
	private MockMvc mvc;

	@Test
	void loginOnSecondLoginThenPreventLogin() throws Exception {
		// @formatter:off
		MvcResult mvcResult = this.mvc.perform(formLogin())
				.andExpect(authenticated())
				.andReturn();

		MockHttpSession firstLoginSession = (MockHttpSession) mvcResult.getRequest().getSession();

		this.mvc.perform(get("/").session(firstLoginSession))
				.andExpect(authenticated());

		// second login is prevented
		this.mvc.perform(formLogin()).andExpect(unauthenticated());

		// first session is still valid
		this.mvc.perform(get("/").session(firstLoginSession))
				.andExpect(authenticated());
		// @formatter:on
	}

}
