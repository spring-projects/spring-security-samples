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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * The log out confirmation page.
 *
 * @author Rob Winch
 */
public class LogoutConfirmPage {

	private final WebDriver webDriver;

	private final LogoutConfirmPage.LogoutForm logoutForm;

	public LogoutConfirmPage(WebDriver webDriver) {
		this.webDriver = webDriver;
		this.logoutForm = PageFactory.initElements(this.webDriver, LogoutForm.class);
	}

	public LoginPage logout() {
		return this.logoutForm.logout();
	}

	public static class LogoutForm {

		private WebDriver webDriver;

		@FindBy(css = "button[type=submit]")
		private WebElement submit;

		public LogoutForm(WebDriver webDriver) {
			this.webDriver = webDriver;
		}

		public LoginPage logout() {
			this.submit.click();
			return PageFactory.initElements(this.webDriver, LoginPage.class);
		}

	}

}
