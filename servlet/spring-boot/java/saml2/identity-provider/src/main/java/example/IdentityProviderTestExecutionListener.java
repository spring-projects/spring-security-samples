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
import java.net.ServerSocket;

import org.testcontainers.containers.ComposeContainer;

import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class IdentityProviderTestExecutionListener extends AbstractTestExecutionListener {

	private ComposeContainer composed;

	@Override
	public void beforeTestClass(TestContext testContext) throws Exception {
		String port = Integer.toString(getRandomPort());
		System.setProperty("SERVER_PORT", port);
		System.setProperty("server.port", port);
		this.composed = new ComposeContainer("saml2", new ClassPathResource("docker/compose.yml").getFile())
			.withEnv("SERVER_PORT", port);
		this.composed.start();
	}

	@Override
	public void afterTestClass(TestContext testContext) throws Exception {
		this.composed.stop();
	}

	static int getRandomPort() throws IOException {
		try (ServerSocket serverSocket = new ServerSocket(0)) {
			return serverSocket.getLocalPort();
		}
	}

}
