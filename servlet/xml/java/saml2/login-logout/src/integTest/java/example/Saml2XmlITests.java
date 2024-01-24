/*
 * Copyright 2021 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebClientBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = { "file:src/main/webapp/WEB-INF/spring-servlet.xml",
		"file:src/main/webapp/WEB-INF/spring/security.xml" })
@WebAppConfiguration
public class Saml2XmlITests {

	private MockMvc mvc;

	private WebClient webClient;

	@Autowired
	WebApplicationContext webApplicationContext;

	@Autowired
	Environment environment;

	@BeforeEach
	void setup() {
		this.mvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
			.apply(SecurityMockMvcConfigurers.springSecurity())
			.build();
		this.webClient = MockMvcWebClientBuilder.mockMvcSetup(this.mvc)
			.withDelegate(new LocalHostWebClient(this.environment))
			.build();
		this.webClient.getCookieManager().clearCookies();
	}

	@Test
	void authenticationAttemptWhenValidThenShowsUserEmailAddress() throws Exception {
		performLogin();
		HtmlPage home = (HtmlPage) this.webClient.getCurrentWindow().getEnclosedPage();
		assertThat(home.asNormalizedText()).contains("You're email address is testuser2@spring.security.saml");
	}

	@Test
	void logoutWhenRelyingPartyInitiatedLogoutThenLoginPageWithLogoutParam() throws Exception {
		performLogin();
		HtmlPage home = (HtmlPage) this.webClient.getCurrentWindow().getEnclosedPage();
		HtmlElement rpLogoutButton = home.getHtmlElementById("rp_logout_button");
		HtmlPage loginPage = rpLogoutButton.click();
		this.webClient.waitForBackgroundJavaScript(10000);
		List<String> urls = new ArrayList<>();
		urls.add(loginPage.getUrl().getFile());
		urls.add(((HtmlPage) this.webClient.getCurrentWindow().getEnclosedPage()).getUrl().getFile());
		assertThat(urls).withFailMessage(() -> {
			// @formatter:off
			String builder = loginPage.asXml()
					+ "\n\n\n"
					+ "Enclosing Page"
					+ "\n\n\n"
					+ ((HtmlPage) this.webClient.getCurrentWindow().getEnclosedPage()).asXml();
			// @formatter:on
			return builder;
		}).contains("/login?logout");
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
