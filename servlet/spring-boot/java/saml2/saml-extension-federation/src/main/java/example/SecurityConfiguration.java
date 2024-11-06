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

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.authorizeHttpRequests((authorize) -> authorize
				.requestMatchers("/error").permitAll()
				.anyRequest().authenticated()
			)
			.saml2Login((saml2) -> saml2.loginProcessingUrl("/saml/SSO"))
			.saml2Logout((saml2) -> saml2.logoutRequest((request) -> request.logoutUrl("/saml/logout")))
			.saml2Logout((saml2) -> saml2.logoutResponse((response) -> response.logoutUrl("/saml/SingleLogout")))
			.saml2Metadata((saml2) -> saml2.metadataUrl("/saml/metadata"));
		// @formatter:on
		return http.build();
	}

	@Bean
	RelyingPartyRegistrationRepository registrations(RelyingPartyMetadata rp,
			@Value("${saml2.ap.metadata}") String ap) {
		List<RelyingPartyRegistration> registrations = RelyingPartyRegistrations.collectionFromMetadataLocation(ap)
			.stream()
			.map(rp::apply)
			.toList();
		return new InMemoryRelyingPartyRegistrationRepository(registrations);
	}

}
