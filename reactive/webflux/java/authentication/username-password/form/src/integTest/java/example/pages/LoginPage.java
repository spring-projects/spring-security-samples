/*
 * Copyright 2020 the original author or authors.
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

package example.pages;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The custom log in page.
 *
 * @author Rob Winch
 * @since 5.0
 */
public class LoginPage {

	private WebDriver driver;

	@FindBy(css = "div[role=alert]")
	private List<WebElement> alert;

	private LoginForm loginForm;

	public LoginPage(WebDriver webDriver) {
		this.driver = webDriver;
		this.loginForm = PageFactory.initElements(webDriver, LoginForm.class);
	}

	static LoginPage create(WebDriver driver) {
		return PageFactory.initElements(driver, LoginPage.class);
	}

	public LoginPage assertAt() {
		assertThat(this.driver.getTitle()).isEqualTo("Please Log In");
		return this;
	}

	public LoginPage assertError() {
		assertThat(this.alert).extracting(WebElement::getText).containsOnly("Invalid username and password.");
		return this;
	}

	public LoginPage assertLogout() {
		assertThat(this.alert).extracting(WebElement::getText).containsOnly("You have been logged out.");
		return this;
	}

	public LoginForm loginForm() {
		return this.loginForm;
	}

	public static class LoginForm {

		private WebDriver driver;

		@FindBy(id = "username")
		private WebElement username;

		@FindBy(id = "password")
		private WebElement password;

		@FindBy(css = "button[type=submit]")
		private WebElement submit;

		public LoginForm(WebDriver driver) {
			this.driver = driver;
		}

		public LoginForm username(String username) {
			this.username.sendKeys(username);
			return this;
		}

		public LoginForm password(String password) {
			this.password.sendKeys(password);
			return this;
		}

		public <T> T submit(Class<T> page) {
			this.submit.click();
			return PageFactory.initElements(this.driver, page);
		}

	}

}
