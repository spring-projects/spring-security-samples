/*
 * Copyright 2020 the original author or authors.
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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rob Winch
 */
@SpringBootTest
public class DataApplicationTests {

	@Autowired
	MessageRepository repository;

	@Test
	@WithMockUser("rob")
	void findAllOnlyToCurrentUserCantReadMessage() {
		List<Message> messages = this.repository.findAll();
		assertThat(messages).hasSize(3);
		for (Message message : messages) {
			assertThat(message.getSummary()).isNull();
			assertThat(message.getText()).isNull();
		}
	}

	@Test
	@WithMockUser(username = "rob", authorities = "message:read")
	void findAllOnlyToCurrentUserCanReadMessage() {
		List<Message> messages = this.repository.findAll();
		assertThat(messages).hasSize(3);
		for (Message message : messages) {
			assertThat(message.getSummary()).isNotNull();
			assertThat(message.getText()).isNotNull();
		}
	}

	@Test
	@WithMockUser(username = "rob", authorities = "message:read")
	void findAllOnlyToCurrentUserCantReadUserDetails() {
		List<Message> messages = this.repository.findAll();
		assertThat(messages).hasSize(3);
		for (Message message : messages) {
			User user = message.getTo();
			assertThat(user.getFirstName()).isNull();
			assertThat(user.getLastName()).isNull();
		}
	}

	@Test
	@WithMockUser(username = "rob", authorities = { "message:read", "user:read" })
	void findAllOnlyToCurrentUserCanReadUserDetails() {
		List<Message> messages = this.repository.findAll();
		assertThat(messages).hasSize(3);
		for (Message message : messages) {
			User user = message.getTo();
			assertThat(user.getFirstName()).isNotNull();
			assertThat(user.getLastName()).isNotNull();
		}
	}

}
