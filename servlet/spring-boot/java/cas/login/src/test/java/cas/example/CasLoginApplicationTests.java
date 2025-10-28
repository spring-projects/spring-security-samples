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

package cas.example;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class CasLoginApplicationTests {

	@LocalServerPort
	int port;

	@Autowired
	Environment environment;

	@Container
	static GenericContainer<?> casServer = new GenericContainer<>(DockerImageName.parse("apereo/cas:6.6.6"))
		.withCommand("--cas.standalone.configuration-directory=/etc/cas/config", "--server.ssl.enabled=false",
				"--server.port=8080", "--cas.service-registry.core.init-from-json=true",
				"--cas.service-registry.json.location=file:/etc/cas/services", "--cas.tgc.secure=false",
				"--cas.tgc.sameSitePolicy=Lax")
		.withExposedPorts(8080)
		.withClasspathResourceMapping("cas/services/https-1.json", "/etc/cas/services/https-1.json",
				BindMode.READ_WRITE)
		.waitingFor(Wait.forLogMessage(".*Ready to process requests.*", 1));

	Playwright playwright;

	Browser browser;

	@DynamicPropertySource
	static void casProperties(DynamicPropertyRegistry registry) {
		String casUrl = String.format("http://%s:%s/cas", casServer.getHost(), casServer.getMappedPort(8080));
		registry.add("cas.base.url", () -> casUrl);
		registry.add("cas.login.url", () -> casUrl + "/login");
		registry.add("cas.logout.url", () -> casUrl + "/logout");
	}

	@BeforeEach
	void setUp() {
		this.playwright = Playwright.create();
		this.browser = this.playwright.chromium().launch();
	}

	@AfterEach
	void setup() {
		this.browser.close();
		this.playwright.close();
	}

	@Test
	void login() {
		try (Page page = doLogin()) {
			String lead = page.locator(".lead").textContent();
			assertThat(lead).isEqualTo("You are successfully logged in as casuser");
		}
	}

	@Test
	void loginAndLogout() {
		try (Page page = doLogin()) {
			page.click("#rp_logout_button");
			String logoutMsg = page.locator("#logout-msg").textContent();
			assertThat(logoutMsg).isEqualTo("You are successfully logged out of the app, but not CAS");
		}
	}

	@Test
	void publicPageWhenCasGatewayAuthenticationThenAuthenticated() {
		try (Page page = doLogin()) {
			page.navigate("http://localhost:" + this.port + "/public");
			String lead = page.locator(".lead").textContent();
			assertThat(lead).isEqualTo("You are successfully logged in as casuser");
		}
	}

	private Page doLogin() {
		Page page = this.browser.newPage();
		page.navigate("http://localhost:" + this.port);
		page.fill("//input[@name='username']", "casuser");
		page.fill("//input[@name='password']", "Mellon");
		page.click("//button[@name='submitBtn']");
		return page;
	}

}
