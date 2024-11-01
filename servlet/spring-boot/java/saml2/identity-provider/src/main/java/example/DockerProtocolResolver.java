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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class DockerProtocolResolver implements ProtocolResolver {

	private static final String PREFIX = "docker:";

	static Environment environment;

	@Override
	public Resource resolve(String location, ResourceLoader resourceLoader) {
		if (!location.startsWith(PREFIX)) {
			return null;
		}
		Resource resource = resourceLoader.getResource(location.replace(PREFIX, "classpath:"));
		try {
			String content = resource.getContentAsString(StandardCharsets.UTF_8);
			content = environment.resolvePlaceholders(content);
			File file = resource.getFile();
			File tmp = new File(file.getAbsolutePath() + ".tmp");
			tmp.createNewFile();
			Files.write(tmp.toPath(), content.getBytes(StandardCharsets.UTF_8));
			tmp.deleteOnExit();
			return new FileSystemResource(tmp);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
