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

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringJUnitWebConfig(classes = SecurityConfiguration.class)
public class RememberMeTests {

	@Test
	void loginWhenRemembermeThenAuthenticated(WebApplicationContext context) throws Exception {
		// @formatter:off
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context)
				.apply(springSecurity())
				.build();

		MockHttpServletRequestBuilder login = post("/login")
				.with(csrf())
				.param("username", "user")
				.param("password", "password")
				.param("remember-me", "true");
		MvcResult mvcResult = mockMvc.perform(login)
				.andExpect(authenticated())
				.andReturn();
		// @formatter:on

		Cookie rememberMe = mvcResult.getResponse().getCookie("remember-me");

		// @formatter:off
		mockMvc.perform(get("/").cookie(rememberMe))
				.andExpect(authenticated());
		// @formatter:on
	}

	@Test
	void loginWhenNoRemembermeThenUnauthenticated(WebApplicationContext context) throws Exception {
		// @formatter:off
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context)
				.apply(springSecurity())
				.build();

		MockHttpServletRequestBuilder login = post("/login")
				.with(csrf())
				.param("username", "user")
				.param("password", "password")
				.param("remember-me", "true");
		// @formatter:on

		// @formatter:off
		mockMvc.perform(get("/"))
				.andExpect(unauthenticated());
		// @formatter:on
	}

	@Test
	void loginWhenNoRemembermeThenNoCookie(WebApplicationContext context) throws Exception {
		// @formatter:off
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context)
				.apply(springSecurity())
				.build();

		MockHttpServletRequestBuilder login = post("/login")
				.with(csrf())
				.param("username", "user")
				.param("password", "password");
		MvcResult mvcResult = mockMvc.perform(login)
				.andExpect(authenticated())
				.andReturn();
		// @formatter:on

		Cookie rememberMe = mvcResult.getResponse().getCookie("remember-me");

		assertThat(rememberMe).isNull();
	}

}
