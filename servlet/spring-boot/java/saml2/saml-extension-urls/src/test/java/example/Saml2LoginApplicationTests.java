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

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.saml2.provider.service.authentication.Saml2AssertionAuthentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2ResponseAssertion;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Saml2LoginApplicationTests {

	@Autowired
	MockMvc mvc;

	@Test
	void authenticationAttemptWhenValidThenShowsUserEmailAddress() throws Exception {
		Saml2ResponseAssertion assertion = Saml2ResponseAssertion.withResponseValue("response")
			.nameId("user1")
			.attributes(Map.of("email", List.of("user1@example.org")))
			.build();
		Saml2Authentication saml2 = new Saml2AssertionAuthentication((AuthenticatedPrincipal) () -> "user1", assertion,
				AuthorityUtils.createAuthorityList("app"), "one");
		String result = this.mvc.perform(get("/").with(authentication(saml2)))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		assertThat(result).contains("Your email address is <span>user1@example.org</span>");
	}

	@Test
	void logoutWhenRelyingPartyInitiatedLogoutThenLoginPageWithLogoutParam() throws Exception {
		Saml2ResponseAssertion assertion = Saml2ResponseAssertion.withResponseValue("response")
			.nameId("user1")
			.attributes(Map.of("email", List.of("user1@example.org")))
			.build();
		Saml2Authentication saml2 = new Saml2AssertionAuthentication((AuthenticatedPrincipal) () -> "user1", assertion,
				AuthorityUtils.createAuthorityList("app"), "one");
		this.mvc.perform(post("/logout").with(authentication(saml2)).with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrlPattern(
					"http://idp-one.127-0-0-1.nip.io/simplesaml/saml2/idp/SingleLogoutService.php*"));
	}

}
