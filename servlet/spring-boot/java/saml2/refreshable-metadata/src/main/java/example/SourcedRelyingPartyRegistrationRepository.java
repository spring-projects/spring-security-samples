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

import java.util.Iterator;

import org.springframework.security.saml2.provider.service.registration.AssertingPartyMetadata;
import org.springframework.security.saml2.provider.service.registration.AssertingPartyMetadataRepository;
import org.springframework.security.saml2.provider.service.registration.IterableRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.stereotype.Component;

@Component
public class SourcedRelyingPartyRegistrationRepository implements IterableRelyingPartyRegistrationRepository {

	private final AssertingPartyMetadataRepository assertingParties;

	private final RelyingPartyMetadata metadata;

	public SourcedRelyingPartyRegistrationRepository(AssertingPartyMetadataRepository assertingParties,
			RelyingPartyMetadata metadata) {
		this.assertingParties = assertingParties;
		this.metadata = metadata;
	}

	@Override
	public RelyingPartyRegistration findByRegistrationId(String registrationId) {
		AssertingPartyMetadata metadata = this.assertingParties.findByEntityId(registrationId);
		return this.metadata.apply(RelyingPartyRegistration.withAssertingPartyMetadata(metadata));
	}

	@Override
	public Iterator<RelyingPartyRegistration> iterator() {
		Iterator<AssertingPartyMetadata> assertingParties = this.assertingParties.iterator();
		RelyingPartyMetadata metadata = this.metadata;
		return new Iterator<>() {
			@Override
			public boolean hasNext() {
				return assertingParties.hasNext();
			}

			@Override
			public RelyingPartyRegistration next() {
				return metadata.apply(RelyingPartyRegistration.withAssertingPartyMetadata(assertingParties.next()));
			}
		};
	}

}
