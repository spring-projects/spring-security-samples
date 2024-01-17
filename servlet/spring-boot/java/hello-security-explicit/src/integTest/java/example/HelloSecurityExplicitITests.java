/*
 * Copyright 2002-2024 the original author or authors.
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

import example.pages.HomePage;
import example.pages.LoginPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Integration tests.
 *
 * @author Michael Simons
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HelloSecurityExplicitITests {

	private WebDriver driver;

	@LocalServerPort
	private int port;

	@BeforeEach
	void setup() {
		this.driver = new HtmlUnitDriver();
	}

	@AfterEach
	void tearDown() {
		this.driver.quit();
	}

	@Test
	void login() {
		final LoginPage loginPage = HomePage.to(this.driver, this.port);
		loginPage.assertAt();

		HomePage homePage = loginPage.loginForm().username("user").password("password").submit();
		homePage.assertAt();

		LoginPage logoutSuccess = homePage.logout();
		logoutSuccess.assertAt();
	}

}
