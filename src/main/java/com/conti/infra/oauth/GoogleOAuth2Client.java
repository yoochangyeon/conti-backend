package com.conti.infra.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class GoogleOAuth2Client {

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    @Value("${oauth2.google.client-id}")
    private String clientId;

    @Value("${oauth2.google.client-secret}")
    private String clientSecret;

    @Value("${oauth2.google.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate;

    public GoogleOAuth2Client() {
        this.restTemplate = new RestTemplate();
    }

    public OAuthUserInfo getUserInfo(String authorizationCode) {
        String accessToken = getAccessToken(authorizationCode);
        return fetchUserInfo(accessToken);
    }

    private String getAccessToken(String authorizationCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", authorizationCode);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(TOKEN_URL, request, JsonNode.class);
        JsonNode body = response.getBody();

        if (body == null) {
            throw new RuntimeException("Failed to get access token from Google");
        }

        return body.get("access_token").asText();
    }

    private OAuthUserInfo fetchUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                USER_INFO_URL, HttpMethod.GET, request, JsonNode.class);
        JsonNode body = response.getBody();

        if (body == null) {
            throw new RuntimeException("Failed to get user info from Google");
        }

        String providerId = body.get("id").asText();
        String email = body.has("email") ? body.get("email").asText() : null;
        String name = body.has("name") ? body.get("name").asText() : null;
        String profileImage = body.has("picture") ? body.get("picture").asText() : null;

        return new OAuthUserInfo(email, name, profileImage, "GOOGLE", providerId);
    }
}
