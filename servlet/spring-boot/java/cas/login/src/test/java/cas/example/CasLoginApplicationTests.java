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

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
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

	@DynamicPropertySource
	static void casProperties(DynamicPropertyRegistry registry) {
		String casUrl = String.format("http://%s:%s/cas", casServer.getHost(), casServer.getMappedPort(8080));
		registry.add("cas.base.url", () -> casUrl);
		registry.add("cas.login.url", () -> casUrl + "/login");
		registry.add("cas.logout.url", () -> casUrl + "/logout");
	}

	@BeforeAll
	static void setUp() {
		WebDriverManager.chromedriver()
			.clearDriverCache()
			.clearResolutionCache()
			.browserInDocker()
			.browserVersion("114")
			.setup();
		Configuration.headless = true;
	}

	@AfterEach
	void setup() {
		Selenide.closeWindow();
	}

	@Test
	void login() {
		doLogin();
		String lead = Selenide.$(By.className("lead")).text();
		assertThat(lead).isEqualTo("You are successfully logged in as casuser");
	}

	private void doLogin() {
		Selenide.open("http://localhost:" + this.port);
		Selenide.$(By.name("username")).setValue("casuser");
		Selenide.$(By.name("password")).setValue("Mellon");
		Selenide.$(By.name("submitBtn")).click();
	}

	@Test
	void loginAndLogout() {
		doLogin();
		Selenide.$(By.id("rp_logout_button")).click();
		String logoutMsg = Selenide.$(By.id("logout-msg")).text();
		assertThat(logoutMsg).isEqualTo("You are successfully logged out of the app, but not CAS");
	}

	@Test
	void publicPageWhenCasGatewayAuthenticationThenAuthenticated() {
		doCasLogin();
		Selenide.open("http://localhost:" + this.port + "/public");
		String lead = Selenide.$(By.className("lead")).text();
		assertThat(lead).isEqualTo("You are successfully logged in as casuser");
	}

	private void doCasLogin() {
		Selenide.open(this.environment.getProperty("cas.login.url"));
		Selenide.$(By.name("username")).setValue("casuser");
		Selenide.$(By.name("password")).setValue("Mellon");
		Selenide.$(By.name("submitBtn")).click();
	}

}
