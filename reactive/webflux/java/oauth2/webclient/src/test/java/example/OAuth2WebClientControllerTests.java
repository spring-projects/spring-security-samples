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

package example;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOAuth2Client;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOAuth2Login;

@WebFluxTest
@Import({ SecurityConfiguration.class, OAuth2WebClientController.class })
@AutoConfigureWebTestClient(timeout = "36000")
public class OAuth2WebClientControllerTests {

	private static MockWebServer web = new MockWebServer();

	@Autowired
	private WebTestClient client;

	@MockBean
	ReactiveClientRegistrationRepository clientRegistrationRepository;

	@AfterAll
	static void shutdown() throws Exception {
		web.shutdown();
	}

	@Test
	void explicitWhenAuthenticatedThenUsesClientIdRegistration() throws Exception {
		web.enqueue(new MockResponse().setBody("body").setResponseCode(200));
		// @formatter:off
		this.client.mutateWith(mockOAuth2Login())
			.mutateWith(mockOAuth2Client("client-id"))
			.get()
			.uri("/webclient/explicit")
			.exchange()
			.expectStatus().isOk();
		// @formatter:on
	}

	@Test
	void implicitWhenAuthenticatedThenUsesDefaultRegistration() throws Exception {
		web.enqueue(new MockResponse().setBody("body").setResponseCode(200));
		// @formatter:off
		this.client.mutateWith(mockOAuth2Login())
			.get()
			.uri("/webclient/implicit")
			.exchange()
			.expectStatus().isOk();
		// @formatter:on
	}

	@Test
	void publicExplicitWhenAuthenticatedThenUsesClientIdRegistration() throws Exception {
		web.enqueue(new MockResponse().setBody("body").setResponseCode(200));
		// @formatter:off
		this.client.mutateWith(mockOAuth2Client("client-id"))
			.get()
			.uri("/public/webclient/explicit")
			.exchange()
			.expectStatus().isOk();
		// @formatter:on
	}

	@Test
	void publicImplicitWhenAuthenticatedThenUsesDefaultRegistration() throws Exception {
		web.enqueue(new MockResponse().setBody("body").setResponseCode(200));
		// @formatter:off
		this.client.mutateWith(mockOAuth2Login())
			.get()
			.uri("/public/webclient/implicit")
			.exchange()
			.expectStatus().isOk();
		// @formatter:on
	}

	@Configuration
	static class WebClientConfig {

		@Bean
		WebClient web() {
			return WebClient.create(web.url("/").toString());
		}

		@Bean
		ServerOAuth2AuthorizedClientRepository authorizedClientRepository() {
			return new WebSessionServerOAuth2AuthorizedClientRepository();
		}

	}

}
