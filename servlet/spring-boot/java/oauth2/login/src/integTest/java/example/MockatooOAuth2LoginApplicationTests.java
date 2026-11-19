/*
 * Copyright 2002-2026 the original author or authors.
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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlButton;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlPasswordInput;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, properties = "spring.profiles.active=mockatoo")
class MockatooOAuth2LoginApplicationTests {

	private static final String MOCKATOO_ORIGIN = "https://app.mockatoo.globalunderdog.com";

	@LocalServerPort
	int port;

	@Test
	void requestAuthorizeGoogleClientWhenMockatooProfileActiveThenRedirectsToMockatoo() throws Exception {
		assertAuthorizationRedirect("google", "/google/o/oauth2/v2/auth");
	}

	@Test
	void requestAuthorizeGitHubClientWhenMockatooProfileActiveThenRedirectsToMockatoo() throws Exception {
		assertAuthorizationRedirect("github", "/github/login/oauth/authorize");
	}

	@Test
	void signInWithGitHubWhenMockatooProfileActiveThenShowsAuthenticatedPage() throws Exception {
		String email = System.getenv("MOCKATOO_TEST_EMAIL");
		String password = System.getenv("MOCKATOO_TEST_PASSWORD");
		assumeTrue(email != null && !email.isBlank() && password != null && !password.isBlank(),
				"MOCKATOO_TEST_EMAIL and MOCKATOO_TEST_PASSWORD are required for the hosted sign-in test");

		try (WebClient webClient = new WebClient()) {
			HtmlPage login = webClient.getPage("http://127.0.0.1:" + this.port + "/oauth2/authorization/github");
			HtmlForm loginForm = login.getForms().get(0);
			HtmlInput emailInput = loginForm.getInputByName("email");
			HtmlPasswordInput passwordInput = (HtmlPasswordInput) loginForm.getInputByName("password");
			emailInput.setValueAttribute(email);
			passwordInput.setValueAttribute(password);
			HtmlButton loginButton = (HtmlButton) loginForm.getElementsByTagName("button").get(0);
			HtmlPage consent = loginButton.click();

			HtmlButton authorizeButton = consent.getFirstByXPath("//button[normalize-space(.)='Authorize']");
			HtmlPage home = authorizeButton.click();

			assertThat(home.asNormalizedText()).contains("You are successfully logged in",
					"via the OAuth 2.0 Client GitHub");
		}
	}

	private void assertAuthorizationRedirect(String registrationId, String expectedPath) throws Exception {
		HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create("http://127.0.0.1:" + this.port + "/oauth2/authorization/" + registrationId))
			.GET()
			.build();
		HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

		assertThat(response.statusCode()).isEqualTo(302);
		URI location = URI.create(response.headers().firstValue("Location").orElseThrow());
		assertThat(location.getScheme() + "://" + location.getAuthority() + location.getPath())
			.isEqualTo(MOCKATOO_ORIGIN + expectedPath);
		assertThat(location.getQuery()).contains("client_id=mockatoo");
	}

}
