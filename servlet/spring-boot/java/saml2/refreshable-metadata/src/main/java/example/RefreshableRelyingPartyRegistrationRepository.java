/*
 * Copyright 2021 the original author or authors.
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
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.stereotype.Component;

@Component
public class RefreshableRelyingPartyRegistrationRepository
		implements RelyingPartyRegistrationRepository, Iterable<RelyingPartyRegistration> {

	private final Map<String, RelyingPartyRegistration> relyingPartyRegistrations = new ConcurrentHashMap<>();

	private final Saml2RelyingPartyProperties relyingPartyProperties;

	public RefreshableRelyingPartyRegistrationRepository(Saml2RelyingPartyProperties relyingPartyProperties) {
		this.relyingPartyProperties = relyingPartyProperties;
		refreshMetadata();
	}

	@Override
	public RelyingPartyRegistration findByRegistrationId(String registrationId) {
		return this.relyingPartyRegistrations.get(registrationId);
	}

	@Override
	public Iterator<RelyingPartyRegistration> iterator() {
		return this.relyingPartyRegistrations.values().iterator();
	}

	@Scheduled(fixedDelay = 30, timeUnit = TimeUnit.MINUTES)
	public void refreshMetadata() {
		for (Map.Entry<String, Saml2RelyingPartyProperties.Registration> byRegistrationId : this.relyingPartyProperties
			.getRegistration()
			.entrySet()) {
			fetchMetadata(byRegistrationId.getKey(), byRegistrationId.getValue());
		}
	}

	private void fetchMetadata(String registrationId, Saml2RelyingPartyProperties.Registration registration) {
		RelyingPartyRegistration relyingPartyRegistration = RelyingPartyRegistrations
			.fromMetadataLocation(registration.getAssertingparty().getMetadataUri())
			.signingX509Credentials((credentials) -> registration.getSigning()
				.getCredentials()
				.stream()
				.map(this::asSigningCredential)
				.forEach(credentials::add))
			.registrationId(registrationId)
			.build();
		this.relyingPartyRegistrations.put(relyingPartyRegistration.getRegistrationId(), relyingPartyRegistration);
	}

	private Saml2X509Credential asSigningCredential(
			Saml2RelyingPartyProperties.Registration.Signing.Credential properties) {
		RSAPrivateKey privateKey = readPrivateKey(properties.getPrivateKeyLocation());
		X509Certificate certificate = readCertificate(properties.getCertificateLocation());
		return new Saml2X509Credential(privateKey, certificate, Saml2X509Credential.Saml2X509CredentialType.SIGNING);
	}

	private RSAPrivateKey readPrivateKey(Resource location) {
		try (InputStream inputStream = location.getInputStream()) {
			return RsaKeyConverters.pkcs8().convert(inputStream);
		}
		catch (Exception ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	private X509Certificate readCertificate(Resource location) {
		try (InputStream inputStream = location.getInputStream()) {
			return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(inputStream);
		}
		catch (Exception ex) {
			throw new IllegalArgumentException(ex);
		}
	}

}
