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

package example;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import jakarta.servlet.http.HttpServletRequest;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.impl.ResponseUnmarshaller;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.springframework.security.saml2.core.OpenSamlInitializationService;
import org.springframework.security.saml2.core.Saml2Error;
import org.springframework.security.saml2.core.Saml2ErrorCodes;
import org.springframework.security.saml2.core.Saml2ParameterNames;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationException;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;

public class EntityIdRelyingPartyRegistrationResolver implements RelyingPartyRegistrationResolver {
	static {
		OpenSamlInitializationService.initialize();
	}

	private final ResponseUnmarshaller responseUnmarshaller;
	private final ParserPool parserPool;

	private final RelyingPartyRegistrationResolver delegate;

	public EntityIdRelyingPartyRegistrationResolver(RelyingPartyRegistrationRepository registrations) {
		XMLObjectProviderRegistry registry = ConfigurationService.get(XMLObjectProviderRegistry.class);
		this.responseUnmarshaller = (ResponseUnmarshaller) registry.getUnmarshallerFactory()
				.getUnmarshaller(Response.DEFAULT_ELEMENT_NAME);
		this.parserPool = registry.getParserPool();
		this.delegate = new DefaultRelyingPartyRegistrationResolver(registrations);
	}

	@Override
	public RelyingPartyRegistration resolve(HttpServletRequest request, String relyingPartyRegistrationId) {
		if (relyingPartyRegistrationId != null) {
			return this.delegate.resolve(request, relyingPartyRegistrationId);
		}
		return this.delegate.resolve(request, resolveRegistrationId(request));
	}

	private String resolveRegistrationId(HttpServletRequest request) {
		String saml2Response = request.getParameter(Saml2ParameterNames.SAML_RESPONSE);
		if (saml2Response == null) {
			return null;
		}
		byte[] decoded = Base64.getMimeDecoder().decode(saml2Response);
		String serialized = new String(decoded, StandardCharsets.UTF_8);
		return parseResponse(serialized).getIssuer().getValue();
	}

	private Response parseResponse(String serialized) {
		try {
			Document document = this.parserPool
					.parse(new ByteArrayInputStream(serialized.getBytes(StandardCharsets.UTF_8)));
			Element element = document.getDocumentElement();
			return (Response) this.responseUnmarshaller.unmarshall(element);
		}
		catch (Exception ex) {
			Saml2Error error = new Saml2Error(Saml2ErrorCodes.MALFORMED_RESPONSE_DATA, ex.getMessage());
			throw new Saml2AuthenticationException(error, ex.getMessage());
		}
	}
}
