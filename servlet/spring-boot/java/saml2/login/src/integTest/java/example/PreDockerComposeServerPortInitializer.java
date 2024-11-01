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

package example;

import java.io.IOException;
import java.net.ServerSocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

/**
 * Spring Boot doesn't determine the port before the docker containers are loaded, so
 * we'll decide the test port here and override the associated properties.
 *
 * @author Josh Cummings
 */
public class PreDockerComposeServerPortInitializer implements EnvironmentPostProcessor {

	private static final Integer port = getPort();

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		environment.getPropertySources().addFirst(new ServerPortPropertySource(port));
	}

	private static Integer getPort() {
		try (ServerSocket serverSocket = new ServerSocket(0)) {
			return serverSocket.getLocalPort();
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static class ServerPortPropertySource extends PropertySource<Integer> {

		ServerPortPropertySource(Integer port) {
			super("server.port.override", port);
		}

		@Override
		public Object getProperty(String name) {
			if ("server.port".equals(name)) {
				return getSource();
			}
			if ("SERVER_PORT".equals(name)) {
				return getSource();
			}
			return null;
		}

	}

}
