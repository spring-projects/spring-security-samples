/*
 * Copyright 2002-2022 the original author or authors.
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

import org.htmlunit.ElementNotFoundException;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlPasswordInput;
import org.htmlunit.html.HtmlSubmitInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
public class Saml2LoginApplicationITests {

	@Autowired
	MockMvc mvc;

	@Autowired
	WebClient webClient;

	@BeforeEach
	void setup() {
		this.webClient.getCookieManager().clearCookies();
	}

	@Test
	void authenticationAttemptWhenValidThenShowsUserEmailAddress() throws Exception {
		performLogin();
		HtmlPage home = (HtmlPage) this.webClient.getCurrentWindow().getEnclosedPage();
		assertThat(home.asNormalizedText()).contains("You're email address is testuser2@spring.security.saml");
	}

	private void performLogin() throws Exception {
		HtmlPage login = this.webClient.getPage("/");
		this.webClient.waitForBackgroundJavaScript(10000);
		HtmlForm form = findForm(login);
		HtmlInput username = form.getInputByName("username");
		HtmlPasswordInput password = form.getInputByName("password");
		HtmlSubmitInput submit = login.getHtmlElementById("okta-signin-submit");
		username.type("testuser2@spring.security.saml");
		password.type("12345678");
		submit.click();
		this.webClient.waitForBackgroundJavaScript(10000);
	}

	private HtmlForm findForm(HtmlPage login) {
		for (HtmlForm form : login.getForms()) {
			try {
				if (form.getId().equals("form19")) {
					return form;
				}
			}
			catch (ElementNotFoundException ex) {
				// Continue
			}
		}
		throw new IllegalStateException("Could not resolve login form");
	}

}
