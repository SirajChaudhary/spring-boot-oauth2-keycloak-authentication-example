package com.example.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public AuthController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    /**
     * Called after Keycloak login, returns Access Token + ID Token claims
     */
    @GetMapping("/api/v1/auth/success")
    public Map<String, Object> loginSuccess(
            @AuthenticationPrincipal OAuth2User principal,
            OAuth2AuthenticationToken authentication) {

        // Load the authorized client (Keycloak)
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        String accessToken = client.getAccessToken().getTokenValue();

        return Map.of(
                "message", "Login successful",
                "access_token", accessToken,         // Keycloak access token
                "user", principal.getAttributes()    // ID token claims
        );
    }
}
