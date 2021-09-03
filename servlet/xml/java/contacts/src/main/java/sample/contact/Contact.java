/*
 * Copyright 2002-2021 the original author or authors.
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

package sample.contact;

import java.io.Serializable;

/**
 * Represents a contact.
 *
 * @author Ben Alex
 */
public class Contact implements Serializable {

	private Long id;

	private String email;

	private String name;

	public Contact(String name, String email) {
		this.name = name;
		this.email = email;
	}

	public Contact() {
	}

	public String getEmail() {
		return this.email;
	}

	public Long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString() + ": ");
		sb.append("Id: " + this.getId() + "; ");
		sb.append("Name: " + this.getName() + "; ");
		sb.append("Email: " + this.getEmail());

		return sb.toString();
	}

}
