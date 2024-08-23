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

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for the messages.
 *
 * @author Rob Winch
 * @since 5.0
 */
@RestController
public class MessageController {

	private final MessageService messages;

	public MessageController(MessageService messages) {
		this.messages = messages;
	}

	@GetMapping("/message")
	public String message() {
		return this.messages.findMessage();
	}

	@GetMapping("/secret")
	public String secretMessage() {
		return this.messages.findSecretMessage();
	}

}
