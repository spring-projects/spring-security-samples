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

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.metadata.OpenSamlMetadataResolver;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.Saml2MetadataFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

	@Value("classpath:credentials/rp-private.key")
	RSAPrivateKey privateKey;

	@Bean
	SecurityFilterChain app(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.authorizeHttpRequests((authorize) -> authorize
				.anyRequest().authenticated()
			)
			.saml2Login(Customizer.withDefaults())
			.saml2Logout(Customizer.withDefaults());
		// @formatter:on

		return http.build();
	}

	@Bean
	RelyingPartyRegistrationResolver relyingPartyRegistrationResolver(
			RelyingPartyRegistrationRepository registrations) {
		return new DefaultRelyingPartyRegistrationResolver(registrations);
	}

	@Bean
	FilterRegistrationBean<Saml2MetadataFilter> metadata(RelyingPartyRegistrationResolver registrations) {
		Saml2MetadataFilter metadata = new Saml2MetadataFilter(registrations, new OpenSamlMetadataResolver());
		FilterRegistrationBean<Saml2MetadataFilter> filter = new FilterRegistrationBean<>(metadata);
		filter.setOrder(-101);
		return filter;
	}

	@Bean
	RelyingPartyRegistrationRepository repository() {
		RelyingPartyRegistration one = addRelyingPartyDetails(RelyingPartyRegistrations
				.fromMetadataLocation("https://dev-05937739.okta.com/app/exk46xofd8NZvFCpS5d7/sso/saml/metadata")
				.registrationId("one")).build();
		RelyingPartyRegistration two = addRelyingPartyDetails(RelyingPartyRegistrations
				.fromMetadataLocation("https://dev-05937739.okta.com/app/exk4842vmapcMkohr5d7/sso/saml/metadata")
				.registrationId("two")).build();
		return new InMemoryRelyingPartyRegistrationRepository(one, two);
	}

	RelyingPartyRegistration.Builder addRelyingPartyDetails(RelyingPartyRegistration.Builder builder) {
		Saml2X509Credential signing = Saml2X509Credential.signing(this.privateKey, relyingPartyCertificate());
		return builder.signingX509Credentials((c) -> c.add(signing))
				.singleLogoutServiceLocation("http://localhost:8080/logout/saml2/slo");
	}

	X509Certificate relyingPartyCertificate() {
		Resource resource = new ClassPathResource("credentials/rp-certificate.crt");
		try (InputStream is = resource.getInputStream()) {
			return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(is);
		}
		catch (Exception ex) {
			throw new UnsupportedOperationException(ex);
		}
	}

}
