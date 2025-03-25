/*
 * Copyright 2002-2025 the original author or authors.
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

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
class AclInitializer implements SmartInitializingSingleton {

	private final JdbcMutableAclService acls;

	private final MessageRepository messages;

	private final TransactionTemplate transactions;

	AclInitializer(JdbcMutableAclService acls, MessageRepository messages, TransactionTemplate transactions) {
		this.acls = acls;
		this.messages = messages;
		this.transactions = transactions;
	}

	@Override
	public void afterSingletonsInstantiated() {
		Iterable<Message> messages = this.messages.findAll();
		for (Message message : messages) {
			String to = message.getTo();
			ObjectIdentity id = new ObjectIdentityImpl(Message.class, message.getId());
			asUser(to, () -> this.transactions.execute((status) -> {
				MutableAcl acl = AclInitializer.this.acls.createAcl(id);
				acl.insertAce(0, BasePermission.READ, new PrincipalSid(to), true);
				return this.acls.updateAcl(acl);
			}));
		}
	}

	private void asUser(String user, Runnable runnable) {
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(user, null, "app"));
		runnable.run();
		SecurityContextHolder.clearContext();
	}

}
