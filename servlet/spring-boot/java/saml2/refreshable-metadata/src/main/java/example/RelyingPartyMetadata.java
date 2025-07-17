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

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.io.ApplicationResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("saml2")
public class RelyingPartyMetadata {

	private final ResourceLoader resourceLoader = ApplicationResourceLoader.get();

	private String entityId = "{baseUrl}/saml2/metadata";

	private String sso = "{baseUrl}/login/saml2/sso";

	private SingleLogout slo = new SingleLogout();

	private X509Certificate certificate;

	private RSAPrivateKey key;

	public RelyingPartyRegistration apply(RelyingPartyRegistration.Builder builder) {
		Saml2X509Credential signing = Saml2X509Credential.signing(this.key, this.certificate);
		return builder.entityId(this.entityId)
			.assertionConsumerServiceLocation(this.sso)
			.singleLogoutServiceBinding(this.slo.getBinding())
			.singleLogoutServiceLocation(this.slo.getLocation())
			.singleLogoutServiceResponseLocation(this.slo.getResponseLocation())
			.signingX509Credentials((c) -> c.add(signing))
			.build();
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public void setSso(String sso) {
		this.sso = sso;
	}

	public void setSlo(SingleLogout slo) {
		this.slo = slo;
	}

	public void setCertificate(String certificate) {
		Resource source = this.resourceLoader.getResource(certificate);
		try (InputStream in = source.getInputStream()) {
			CertificateFactory certificates = CertificateFactory.getInstance("X.509");
			this.certificate = (X509Certificate) certificates.generateCertificate(in);
		}
		catch (CertificateException | IOException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	public void setKey(RSAPrivateKey key) {
		this.key = key;
	}

	public static class SingleLogout {

		private Saml2MessageBinding binding = Saml2MessageBinding.REDIRECT;

		private String location = "{baseUrl}/logout/saml2/slo";

		private String responseLocation = "{baseUrl}/logout/saml2/slo";

		public Saml2MessageBinding getBinding() {
			return this.binding;
		}

		public void setBinding(Saml2MessageBinding binding) {
			this.binding = binding;
		}

		public String getLocation() {
			return this.location;
		}

		public void setLocation(String location) {
			this.location = location;
		}

		public String getResponseLocation() {
			if (this.responseLocation == null) {
				return this.location;
			}
			return this.responseLocation;
		}

		public void setResponseLocation(String responseLocation) {
			this.responseLocation = responseLocation;
		}

	}

}
