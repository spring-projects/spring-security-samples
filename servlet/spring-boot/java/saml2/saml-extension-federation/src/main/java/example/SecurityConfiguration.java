/*
 * Copyright 2002-2022 the original author or authors.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties.Registration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
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
	InMemoryRelyingPartyRegistrationRepository repository(Saml2RelyingPartyProperties properties,
			@Value("classpath:credentials/rp-private.key") RSAPrivateKey key,
			@Value("classpath:credentials/rp-certificate.crt") File cert) {
		Saml2X509Credential signing = Saml2X509Credential.signing(key, x509Certificate(cert));
		Registration registration = properties.getRegistration().values().iterator().next();
		return new InMemoryRelyingPartyRegistrationRepository(RelyingPartyRegistrations
			.collectionFromMetadataLocation(registration.getAssertingparty().getMetadataUri())
			.stream()
			.map((builder) -> builder.registrationId(UUID.randomUUID().toString())
				.entityId(registration.getEntityId())
				.assertionConsumerServiceLocation(registration.getAcs().getLocation())
				.singleLogoutServiceLocation(registration.getSinglelogout().getUrl())
				.singleLogoutServiceResponseLocation(registration.getSinglelogout().getResponseUrl())
				.signingX509Credentials((credentials) -> credentials.add(signing))
				.build())
			.collect(Collectors.toList()));
	}

	X509Certificate x509Certificate(File location) {
		try (InputStream source = new FileInputStream(location)) {
			return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(source);
		}
		catch (CertificateException | IOException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

}
