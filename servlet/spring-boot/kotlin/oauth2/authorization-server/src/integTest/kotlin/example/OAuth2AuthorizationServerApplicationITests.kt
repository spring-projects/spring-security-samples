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

package example

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.RequestPostProcessor
import org.springframework.test.web.servlet.result.MockMvcResultMatchers


/**
 * Integration tests for [OAuth2AuthorizationServerApplication].
 *
 * @author Steve Riesenberg
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OAuth2AuthorizationServerApplicationITests {
    private val objectMapper = ObjectMapper()

    @Autowired
    private val mockMvc: MockMvc? = null

    @Test
    fun performTokenRequestWhenValidClientCredentialsThenOk() {
        // @formatter:off
        mockMvc!!.perform(MockMvcRequestBuilders.post("/oauth2/token")
            .param("grant_type", "client_credentials")
            .param("scope", "message:read")
            .with(basicAuth(CLIENT_ID, CLIENT_SECRET)))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.access_token").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.expires_in").isNumber)
            .andExpect(MockMvcResultMatchers.jsonPath("$.scope").value("message:read"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.token_type").value("Bearer"))
        // @formatter:on
    }

    @Test
    fun performTokenRequestWhenMissingScopeThenOk() {
        // @formatter:off
        mockMvc!!.perform(MockMvcRequestBuilders.post("/oauth2/token")
            .param("grant_type", "client_credentials")
            .with(basicAuth(CLIENT_ID, CLIENT_SECRET)))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.access_token").isString)
            .andExpect(MockMvcResultMatchers.jsonPath("$.expires_in").isNumber)
            .andExpect(MockMvcResultMatchers.jsonPath("$.scope").value("message:read message:write"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.token_type").value("Bearer"))
        // @formatter:on
    }

    @Test
    fun performTokenRequestWhenInvalidClientCredentialsThenUnauthorized() {
        // @formatter:off
        mockMvc!!.perform(MockMvcRequestBuilders.post("/oauth2/token")
            .param("grant_type", "client_credentials")
            .param("scope", "message:read")
            .with(basicAuth("bad", "password")))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("invalid_client"))
        // @formatter:on
    }

    @Test
    fun performTokenRequestWhenMissingGrantTypeThenUnauthorized() {
        // @formatter:off
        mockMvc!!.perform(MockMvcRequestBuilders.post("/oauth2/token")
            .with(basicAuth("bad", "password")))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("invalid_client"))
        // @formatter:on
    }

    @Test
    fun performTokenRequestWhenGrantTypeNotRegisteredThenBadRequest() {
        // @formatter:off
        mockMvc!!.perform(MockMvcRequestBuilders.post("/oauth2/token")
            .param("grant_type", "client_credentials")
            .with(basicAuth("login-client", "openid-connect")))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("unauthorized_client"))
        // @formatter:on
    }

    @Test
    fun performIntrospectionRequestWhenValidTokenThenOk() {
        // @formatter:off
        mockMvc!!.perform(MockMvcRequestBuilders.post("/oauth2/introspect")
            .param("token", accessToken)
            .with(basicAuth(CLIENT_ID, CLIENT_SECRET)))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.active").value("true"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.aud[0]").value(CLIENT_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$.client_id").value(CLIENT_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$.exp").isNumber)
            .andExpect(MockMvcResultMatchers.jsonPath("$.iat").isNumber)
            .andExpect(MockMvcResultMatchers.jsonPath("$.iss").value("http://localhost:9000"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.nbf").isNumber)
            .andExpect(MockMvcResultMatchers.jsonPath("$.scope").value("message:read"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.sub").value(CLIENT_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$.token_type").value("Bearer"))
        // @formatter:on
    }

    @Test
    fun performIntrospectionRequestWhenInvalidCredentialsThenUnauthorized() {
        // @formatter:off
        mockMvc!!.perform(MockMvcRequestBuilders.post("/oauth2/introspect")
            .param("token", accessToken)
            .with(basicAuth("bad", "password")))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("invalid_client"))
        // @formatter:on
    }

    // @formatter:off
    private val accessToken:
    // @formatter:on
            String
        get() {
            // @formatter:off
            val mvcResult = mockMvc!!.perform(MockMvcRequestBuilders.post("/oauth2/token")
                .param("grant_type", "client_credentials")
                .param("scope", "message:read")
                .with(basicAuth(CLIENT_ID, CLIENT_SECRET)))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.access_token").exists())
                .andReturn()
            // @formatter:on
            val tokenResponseJson = mvcResult.response.contentAsString
            val tokenResponse: Map<String, Any> =
                objectMapper.readValue(tokenResponseJson, object : TypeReference<Map<String, Any>>() {})
            return tokenResponse["access_token"].toString()
        }

    private class BasicAuthenticationRequestPostProcessor constructor(
        private val username: String,
        private val password: String,
    ) :
        RequestPostProcessor {
        override fun postProcessRequest(request: MockHttpServletRequest): MockHttpServletRequest {
            val headers = HttpHeaders()
            headers.setBasicAuth(username, password)
            request.addHeader("Authorization", headers.getFirst("Authorization")!!)
            return request
        }
    }

    companion object {
        private const val CLIENT_ID = "messaging-client"
        private const val CLIENT_SECRET = "secret"
        private fun basicAuth(username: String, password: String): BasicAuthenticationRequestPostProcessor {
            return BasicAuthenticationRequestPostProcessor(username, password)
        }
    }
}
