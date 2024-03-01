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

import java.time.Instant;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import org.springframework.security.authorization.method.AuthorizeReturnObject;

@Entity
@JsonSerialize(as = Message.class)
@AuthorizeReturnObject
public class Message {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String text;

	private String summary;

	private Instant created = Instant.now();

	@ManyToOne
	private User to;

	public User getTo() {
		return this.to;
	}

	public void setTo(User to) {
		this.to = to;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Instant getCreated() {
		return this.created;
	}

	public void setCreated(Instant created) {
		this.created = created;
	}

	@AuthorizeRead("message")
	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@AuthorizeRead("message")
	public String getSummary() {
		return this.summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

}
