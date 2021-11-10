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

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.saml2.provider.service.metadata.OpenSamlMetadataResolver;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.Saml2AuthenticationTokenConverter;
import org.springframework.security.saml2.provider.service.web.Saml2MetadataFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

	@Bean
	SecurityFilterChain app(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.authorizeRequests((authorize) -> authorize
				.anyRequest().authenticated()
			)
			.saml2Login((saml2) -> saml2.loginProcessingUrl("/login/saml2/sso"))
			.saml2Logout(Customizer.withDefaults());
		// @formatter:on

		return http.build();
	}

	@Bean
	RelyingPartyRegistrationResolver relyingPartyRegistrationResolver(
			RelyingPartyRegistrationRepository registrations) {
		return new DefaultRelyingPartyRegistrationResolver((id) -> registrations.findByRegistrationId("metadata"));
	}

	@Bean
	Saml2AuthenticationTokenConverter authentication(RelyingPartyRegistrationResolver registrations) {
		return new Saml2AuthenticationTokenConverter(registrations);
	}

	@Bean
	FilterRegistrationBean<Saml2MetadataFilter> metadata(RelyingPartyRegistrationResolver registrations) {
		Saml2MetadataFilter metadata = new Saml2MetadataFilter(registrations, new OpenSamlMetadataResolver());
		FilterRegistrationBean<Saml2MetadataFilter> filter = new FilterRegistrationBean<>(metadata);
		filter.setOrder(-101);
		return filter;
	}

}
