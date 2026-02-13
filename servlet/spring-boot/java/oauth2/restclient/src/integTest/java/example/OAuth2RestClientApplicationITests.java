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

import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Client;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link OAuth2RestClientApplication}.
 *
 * @author Steve Riesenberg
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OAuth2RestClientApplicationITests {

	private static final String TOKEN_VALUE = "123abc";

	private static MockWebServer mockWebServer;

	@Autowired
	private MockMvc mockMvc;

	private OAuth2AccessToken accessToken;

	@BeforeAll
	static void initialize() throws Exception {
		mockWebServer = new MockWebServer();
		mockWebServer.setDispatcher(new ClassPathDispatcher());
		mockWebServer.start();
	}

	@BeforeEach
	void setUp() {
		this.accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, TOKEN_VALUE, null, null,
				Set.of("message:read", "message:write"));
	}

	@AfterAll
	static void destroy() throws Exception {
		mockWebServer.shutdown();
	}

	@DynamicPropertySource
	static void mockwebserver(DynamicPropertyRegistry registry) {
		String issuer = StringUtils.trimTrailingCharacter(mockWebServer.url("/").toString(), '/');
		registry.add("mockwebserver.url", () -> issuer);
	}

	@Test
	void messagesWhenAnonymousThenRedirectsToLogin() throws Exception {
		// @formatter:off
		this.mockMvc.perform(get("/messages"))
			.andExpect(status().is3xxRedirection())
			.andExpect(header().string(HttpHeaders.LOCATION, endsWith("/oauth2/authorization/login-client")));
		// @formatter:on
	}

	@Test
	void messagesWhenAuthenticatedAndUnauthorizedThenRedirectsToAuthorizationEndpoint() throws Exception {
		// @formatter:off
		this.mockMvc.perform(get("/messages").with(user("user")))
			.andExpect(status().is3xxRedirection())
			.andExpect(header().string(HttpHeaders.LOCATION, containsString("/oauth2/authorize")));
		// @formatter:on
	}

	@Test
	void publicMessagesWhenAnonymousAndUnauthorizedThenRedirectsToAuthorizationEndpoint() throws Exception {
		// @formatter:off
		this.mockMvc.perform(get("/public/messages"))
			.andExpect(status().is3xxRedirection())
			.andExpect(header().string(HttpHeaders.LOCATION, containsString("/oauth2/authorize")));
		// @formatter:on
	}

	@Test
	void publicMessagesWhenAnonymousAndAuthorizedThenRequestContainsBearerToken() throws Exception {
		String expectedResponse = """
				<h1>Messages</h1>
				<ol>
					<li>Hello</li>
					<li>Goodbye</li>
				</ol>
				""".replaceAll("\t", "    ");
		// @formatter:off
		this.mockMvc.perform(get("/public/messages")
				.with(oauth2Client("messaging-client").accessToken(this.accessToken)))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(expectedResponse)));
		// @formatter:on

		RecordedRequest recordedRequest = takeRequest("/messages");
		assertThat(recordedRequest).isNotNull();
		assertThat(recordedRequest.getHeaders().get(HttpHeaders.AUTHORIZATION))
			.isEqualTo("Bearer %s".formatted(TOKEN_VALUE));
	}

	@Test
	void messagesWhenAuthenticatedAndAuthorizedThenRequestContainsBearerToken() throws Exception {
		String expectedResponse = """
				<h1>Messages</h1>
				<ol>
					<li>Hello</li>
					<li>Goodbye</li>
				</ol>
				""".replaceAll("\t", "    ");
		// @formatter:off
		this.mockMvc.perform(get("/messages")
				.with(user("user"))
				.with(oauth2Client("messaging-client").accessToken(this.accessToken)))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(expectedResponse)));
		// @formatter:on

		RecordedRequest recordedRequest = takeRequest("/messages");
		assertThat(recordedRequest).isNotNull();
		assertThat(recordedRequest.getHeaders().get(HttpHeaders.AUTHORIZATION))
			.isEqualTo("Bearer %s".formatted(TOKEN_VALUE));
	}

	/**
	 * Take a request, ignoring startup requests (e.g.
	 * "/.well-known/openid-configuration").
	 * @param path the request path
	 * @return the {@link RecordedRequest} from the server
	 */
	private RecordedRequest takeRequest(String path) throws InterruptedException {
		RecordedRequest recordedRequest;
		do {
			recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
		}
		while (recordedRequest != null && !path.equals(recordedRequest.getPath()));

		return recordedRequest;
	}

}
