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

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/**
 * Message service that has method security on it.
 *
 * @author Rob Winch
 * @since 5.0
 */
@Component
public class MessageService {

	/**
	 * Gets a message if authenticated.
	 * @return the message
	 */
	@PreAuthorize("isAuthenticated()")
	public String findMessage() {
		return "Hello User!";
	}

	/**
	 * Gets a message if admin.
	 * @return the message
	 */
	@PreAuthorize("hasRole('ADMIN')")
	public String findSecretMessage() {
		return "Hello Admin!";
	}

}
