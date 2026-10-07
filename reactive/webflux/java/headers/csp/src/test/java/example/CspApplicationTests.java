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
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Josh Cummings
 */
@SpringBootTest
@AutoConfigureWebTestClient
class CspApplicationTests {

	private static final Pattern NONCE_IN_HEADER = Pattern.compile("script-src 'self' 'nonce-([^']+)'");

	@Autowired
	WebTestClient rest;

	@Test
	void indexThenInlineScriptNonceMatchesHeader() {
		EntityExchangeResult<String> result = index();

		String header = result.getResponseHeaders().getFirst("Content-Security-Policy");
		Matcher matcher = NONCE_IN_HEADER.matcher(header);
		assertThat(matcher.find()).isTrue();
		assertThat(result.getResponseBody()).contains("nonce=\"" + matcher.group(1) + "\"");
	}

	@Test
	void indexWhenRequestedTwiceThenNonceIsDifferent() {
		String first = index().getResponseHeaders().getFirst("Content-Security-Policy");
		String second = index().getResponseHeaders().getFirst("Content-Security-Policy");

		assertThat(first).isNotEqualTo(second);
	}

	private EntityExchangeResult<String> index() {
		// @formatter:off
		return this.rest.get()
			.uri("/")
			.exchange()
			.expectStatus().isOk()
			.expectBody(String.class)
			.returnResult();
		// @formatter:on
	}

}
