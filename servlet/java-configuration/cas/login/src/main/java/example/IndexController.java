/*
 * Copyright 2002-2021 the original author or authors.
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

import org.apereo.cas.client.authentication.AttributePrincipal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for "/".
 *
 * @author Rob WInch
 */
@Controller
@PropertySource(value = "classpath:security.properties")
public class IndexController {

	@Value("${cas.base.url}")
	private String casBaseUrl;

	@GetMapping("/")
	public String index(Model model, @AuthenticationPrincipal AttributePrincipal principal) {
		if (principal != null) {
			String emailAddress = (String) principal.getAttributes().get("email");
			model.addAttribute("emailAddress", emailAddress);
			model.addAttribute("userAttributes", principal.getAttributes());
		}
		return "index";
	}

	@GetMapping("/loggedout")
	public String loggedout(Model model) {
		model.addAttribute("casLogout", this.casBaseUrl + "/logout");
		return "loggedout";
	}

}
