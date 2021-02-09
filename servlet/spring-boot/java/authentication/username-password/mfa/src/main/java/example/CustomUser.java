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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A custom user representation.
 *
 * @author Rob Winch
 */
public class CustomUser {

	private final long id;

	private final String email;

	@JsonIgnore
	private final String password;

	@JsonIgnore
	private final String secret;

	@JsonIgnore
	private final String answer;

	@JsonCreator
	public CustomUser(long id, String email, String password, String secret, String answer) {
		this.id = id;
		this.email = email;
		this.password = password;
		this.secret = secret;
		this.answer = answer;
	}

	public CustomUser(CustomUser user) {
		this(user.id, user.email, user.password, user.secret, user.answer);
	}

	public long getId() {
		return this.id;
	}

	public String getEmail() {
		return this.email;
	}

	public String getPassword() {
		return this.password;
	}

	public String getSecret() {
		return this.secret;
	}

	public String getAnswer() {
		return this.answer;
	}

}
