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

import java.io.IOException;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.assertj.core.api.Assertions;
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

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ApplicationConfiguration.class)
@WebAppConfiguration
public class Saml2JavaConfigurationITests {

	private MockMvc mvc;

	private WebClient webClient;

	@Autowired
	WebApplicationContext webApplicationContext;

	@Autowired
	Environment environment;

	@BeforeEach
	void setup() {
		this.mvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
				.apply(SecurityMockMvcConfigurers.springSecurity()).build();
		this.webClient = MockMvcWebClientBuilder.mockMvcSetup(this.mvc)
				.withDelegate(new LocalHostWebClient(this.environment)).build();
		this.webClient.getCookieManager().clearCookies();
	}

	@Test
	void authenticationAttemptWhenValidThenShowsUserEmailAddress() throws Exception {
		HtmlPage relyingParty = performLogin();
		Assertions.assertThat(relyingParty.asText()).contains("You're email address is testuser@spring.security.saml");
	}

	@Test
	void logoutWhenRelyingPartyInitiatedLogoutThenLoginPageWithLogoutParam() throws Exception {
		HtmlPage relyingParty = performLogin();
		HtmlElement rpLogoutButton = relyingParty.getHtmlElementById("rp_logout_button");
		HtmlPage loginPage = rpLogoutButton.click();
		Assertions.assertThat(loginPage.getUrl().getFile()).isEqualTo("/login?logout");
	}

	@Test
	void logoutWhenAssertingPartyInitiatedLogoutThenLoginPageWithLogoutParam() throws Exception {
		HtmlPage relyingParty = performLogin();
		HtmlElement apLogoutButton = relyingParty.getHtmlElementById("ap_logout_button");
		HtmlPage loginPage = apLogoutButton.click();
		Assertions.assertThat(loginPage.getUrl().getFile()).isEqualTo("/login?logout");
	}

	private HtmlPage performLogin() throws IOException {
		HtmlPage login = this.webClient.getPage("/");
		HtmlForm form = login.getFormByName("f");
		HtmlInput username = form.getInputByName("username");
		HtmlInput password = form.getInputByName("password");
		HtmlSubmitInput submit = login.getHtmlElementById("submit_button");
		username.setValueAttribute("user");
		password.setValueAttribute("password");
		return submit.click();
	}

}
