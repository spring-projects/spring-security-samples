/*
 * Copyright 2024 the original author or authors.
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
import java.util.Optional;

import org.springframework.security.authorization.method.AuthorizationProxy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

	private final MessageRepository messages;

	public MessageController(MessageRepository messages) {
		this.messages = messages;
	}

	@GetMapping
	List<Message> getMessages() {
		return this.messages.findAll();
	}

	@GetMapping("/{id}")
	Optional<Message> getMessages(Long id) {
		return this.messages.findById(id);
	}

	@PutMapping("/{id}")
	Optional<Message> updateMessage(@PathVariable("id") Long id, @RequestBody String text) {
		return this.messages.findById(id).map((message) -> {
			message.setText(text);
			// unwrap authorization proxy so Spring Data can persist
			if (message instanceof AuthorizationProxy proxy) {
				message = (Message) proxy.toAuthorizedTarget();
			}
			return this.messages.save(message);
		});
	}

}
