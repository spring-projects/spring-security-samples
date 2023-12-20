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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

	@Value("${cas.logout.url}")
	private String casLogoutUrl;

	@GetMapping("/")
	public String index() {
		return "index";
	}

	@GetMapping("/loggedout")
	public String loggedout(Model model) {
		model.addAttribute("casLogout", this.casLogoutUrl);
		return "loggedout";
	}

	@GetMapping("/public")
	String publicPage() {
		return "public";
	}

}
