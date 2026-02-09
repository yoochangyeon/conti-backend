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
public class KakaoOAuth2Client {

    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    @Value("${oauth2.kakao.client-id}")
    private String clientId;

    @Value("${oauth2.kakao.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate;

    public KakaoOAuth2Client() {
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
        params.add("redirect_uri", redirectUri);
        params.add("code", authorizationCode);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(TOKEN_URL, request, JsonNode.class);
        JsonNode body = response.getBody();

        if (body == null) {
            throw new RuntimeException("Failed to get access token from Kakao");
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
            throw new RuntimeException("Failed to get user info from Kakao");
        }

        String providerId = body.get("id").asText();

        JsonNode kakaoAccount = body.get("kakao_account");
        String email = kakaoAccount != null && kakaoAccount.has("email")
                ? kakaoAccount.get("email").asText() : null;

        JsonNode profile = kakaoAccount != null ? kakaoAccount.get("profile") : null;
        String name = profile != null && profile.has("nickname")
                ? profile.get("nickname").asText() : null;
        String profileImage = profile != null && profile.has("profile_image_url")
                ? profile.get("profile_image_url").asText() : null;

        return new OAuthUserInfo(email, name, profileImage, "KAKAO", providerId);
    }
}
