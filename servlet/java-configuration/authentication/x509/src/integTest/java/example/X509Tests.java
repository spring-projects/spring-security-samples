/*
 * Copyright 2002-2024 the original author or authors.
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

import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.HttpsSupport;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Test the Hello World application.
 *
 * @author Michael Simons
 */
// @Disabled
public class X509Tests {

	@Test
	void notCertificateThenSslHandshakeException() {
		RestTemplate rest = new RestTemplate();
		assertThatCode(() -> rest.getForEntity(getServerUrl(), String.class))
			.hasCauseInstanceOf(SSLHandshakeException.class);
	}

	@Test
	@Disabled("Figure out how to make certs work")
	void certificateThenStatusOk() throws Exception {
		ClassPathResource serverKeystore = new ClassPathResource("certs/server.p12");
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(serverKeystore.getInputStream(), "password".toCharArray());
		// @formatter:off
		SSLContext sslContext = SSLContexts.custom()
				.loadKeyMaterial(keyStore, "password".toCharArray(), (aliases, socket) -> "client")
				.loadTrustMaterial(keyStore, new TrustAllStrategy())
				.build();

		SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext,
				HttpsSupport.getDefaultHostnameVerifier());

		final Registry<ConnectionSocketFactory> socketFactoryRegistry =
				RegistryBuilder.<ConnectionSocketFactory>create()
						.register("https", socketFactory)
						.register("http", new PlainConnectionSocketFactory())
						.build();

		final BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager(socketFactoryRegistry);
		// @formatter:on

		try (CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).build()) {
			ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
			RestTemplate rest = new RestTemplate(requestFactory);
			ResponseEntity<String> responseEntity = rest.getForEntity(getServerUrl() + "/me", String.class);
			assertThat(responseEntity).extracting((result) -> result.getStatusCode().is2xxSuccessful()).isEqualTo(true);
		}

	}

	private String getServerUrl() {
		return "https://localhost:" + System.getProperty("app.httpsPort");
	}

}
