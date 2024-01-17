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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.server.UnboundIdContainer;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	@Bean
	UnboundIdContainer ldapContainer() {
		UnboundIdContainer result = new UnboundIdContainer("dc=springframework,dc=org", "classpath:users.ldif");
		result.setPort(0);
		return result;
	}

	@Bean
	DefaultSpringSecurityContextSource contextSource(UnboundIdContainer container) {
		return new DefaultSpringSecurityContextSource(
				"ldap://localhost:" + container.getPort() + "/dc=springframework,dc=org");
	}

	@Bean
	BindAuthenticator authenticator(BaseLdapPathContextSource contextSource) {
		BindAuthenticator authenticator = new BindAuthenticator(contextSource);
		authenticator.setUserDnPatterns(new String[] { "uid={0},ou=people" });
		return authenticator;
	}

	@Bean
	LdapAuthenticationProvider authenticationProvider(LdapAuthenticator authenticator) {
		return new LdapAuthenticationProvider(authenticator);
	}

}
