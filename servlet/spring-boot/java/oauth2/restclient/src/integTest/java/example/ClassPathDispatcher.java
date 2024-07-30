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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Dispatcher for {@link okhttp3.mockwebserver.MockWebServer} that returns resources on
 * the classpath.
 *
 * @author Steve Riesenberg
 */
final class ClassPathDispatcher extends Dispatcher {

	@NonNull
	@Override
	public MockResponse dispatch(RecordedRequest recordedRequest) {
		if (recordedRequest.getPath() != null && recordedRequest.getRequestUrl() != null) {
			try {
				String requestUrl = recordedRequest.getRequestUrl().toString();
				String baseUrl = UriComponentsBuilder.fromUriString(requestUrl).replacePath("").toUriString();
				String responseBody = readResource(recordedRequest.getPath()).replace("{baseUrl}", baseUrl);
				return new MockResponse().setResponseCode(200)
					.setHeader("Content-Type", "application/json")
					.setBody(responseBody);
			}
			catch (IOException ignored) {
			}
		}
		return new MockResponse().setResponseCode(404);
	}

	private static String readResource(String path) throws IOException {
		if (path.startsWith("/")) {
			path = StringUtils.trimLeadingCharacter(path, '/');
		}

		return new ClassPathResource("responses/" + path + ".json").getContentAsString(StandardCharsets.UTF_8);
	}

}
