package com.bookheaven.user_service.service.oauth2;

import com.bookheaven.user_service.dto.OAuth2UserInfo;
import com.bookheaven.user_service.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@Slf4j
public class GoogleOAuth2Strategy implements OAuth2ProviderStrategy {

    private final RestTemplate externalRestTemplate;

    @Value("${oauth2.google.client-id}")
    private String googleClientId;

    @Value("${oauth2.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth2.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${oauth2.google.token-url}")
    private String googleTokenUrl;

    @Value("${oauth2.google.userinfo-url}")
    private String googleUserInfoUrl;

    public GoogleOAuth2Strategy(@Qualifier("externalRestTemplate") RestTemplate externalRestTemplate) {
        this.externalRestTemplate = externalRestTemplate;
    }

    @Override
    public String getProviderName() {
        return "google";
    }

    @Override
    public User.AuthProvider getAuthProvider() {
        return User.AuthProvider.GOOGLE;
    }

    @Override
    public OAuth2UserInfo exchangeCodeForUserInfo(String authorizationCode) {
        // Step 1: Exchange code for access token
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", authorizationCode);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        @SuppressWarnings("unchecked")
        ResponseEntity<Map> tokenResponse = externalRestTemplate.exchange(
                googleTokenUrl, HttpMethod.POST, entity, Map.class);
        assert tokenResponse.getBody() != null;
        String accessToken = (String) tokenResponse.getBody().get("access_token");

        // Step 2: Fetch user info
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> userEntity = new HttpEntity<>(userHeaders);

        @SuppressWarnings("unchecked")
        ResponseEntity<Map> userResponse = externalRestTemplate.exchange(
                googleUserInfoUrl, HttpMethod.GET, userEntity, Map.class);
        Map<String, Object> profile = userResponse.getBody();

        return OAuth2UserInfo.builder()
                .providerId((String) profile.get("id"))
                .email((String) profile.get("email"))
                .name((String) profile.get("name"))
                .picture((String) profile.get("picture"))
                .build();
    }
}
