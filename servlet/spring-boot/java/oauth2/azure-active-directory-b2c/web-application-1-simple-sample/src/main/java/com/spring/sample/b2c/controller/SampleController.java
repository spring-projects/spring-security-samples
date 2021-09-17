package com.spring.sample.b2c.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@Controller
public class SampleController {

    @Autowired
    private WebClient webClient;

    @GetMapping(value = { "/", "/home" })
    public String index() {
        return "home";
    }

    @GetMapping(value = { "/resourceServer" })
    @ResponseBody
    public String getResourceServer(@RegisteredOAuth2AuthorizedClient("sign-up-or-sign-in") OAuth2AuthorizedClient signUpOrSignIn) {
        return canVisitUri(signUpOrSignIn, "http://localhost:8091/hello");
    }

    @GetMapping(value = { "/resourceServerValidateAudience" })
    @ResponseBody
    public String getResourceServerValidateAudience(@RegisteredOAuth2AuthorizedClient("sign-up-or-sign-in") OAuth2AuthorizedClient signUpOrSignIn) {
        return canVisitUri(signUpOrSignIn, "http://localhost:8092/hello");
    }

    /**
     * Check whether uri is accessible by provided client.
     *
     * @param client Authorized client.
     * @param uri The request uri.
     * @return "Get http response successfully." or "Get http response failed."
     */
    private String canVisitUri(OAuth2AuthorizedClient client, String uri) {
        if (null == client) {
            return "Get response failed.";
        }
        String body = this.webClient
                .get()
                .uri(uri)
                .attributes(oauth2AuthorizedClient(client))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return "Get response from " + uri + (null != body ? " successfully" : " failed\n");
    }

}
