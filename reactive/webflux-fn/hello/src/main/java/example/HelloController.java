/*
 * Copyright 2002-2017 the original author or authors.
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

import java.util.Collections;

import reactor.core.publisher.Mono;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * Index Controller.
 *
 * @author Rob Winch
 * @since 5.0
 */
@Component
public class HelloController {

	// @formatter:off
	public Mono<ServerResponse> hello(ServerRequest serverRequest) {
		return ServerResponse.ok()
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(Collections.singletonMap("message", "Hello world!"));
	}
	// @formatter:on

}
