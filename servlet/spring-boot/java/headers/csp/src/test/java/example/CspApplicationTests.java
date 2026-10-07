/*
 * Copyright 2004-present the original author or authors.
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Josh Cummings
 */
@SpringBootTest
@AutoConfigureMockMvc
class CspApplicationTests {

	private static final Pattern NONCE_IN_HEADER = Pattern.compile("script-src 'self' 'nonce-([^']+)'");

	@Autowired
	private MockMvc mvc;

	@Test
	void loginThenInlineScriptNonceMatchesHeader() throws Exception {
		MvcResult result = this.mvc.perform(get("/login")).andExpect(status().isOk()).andReturn();

		String header = result.getResponse().getHeader("Content-Security-Policy");
		Matcher matcher = NONCE_IN_HEADER.matcher(header);
		assertThat(matcher.find()).isTrue();
		assertThat(result.getResponse().getContentAsString()).contains("nonce=\"" + matcher.group(1) + "\"");
	}

	@Test
	void loginWhenRequestedTwiceThenNonceIsDifferent() throws Exception {
		String first = this.mvc.perform(get("/login")).andReturn().getResponse().getHeader("Content-Security-Policy");
		String second = this.mvc.perform(get("/login")).andReturn().getResponse().getHeader("Content-Security-Policy");

		assertThat(first).isNotEqualTo(second);
	}

}
