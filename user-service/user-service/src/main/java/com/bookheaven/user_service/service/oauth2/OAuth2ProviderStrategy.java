package com.bookheaven.user_service.service.oauth2;

import com.bookheaven.user_service.dto.OAuth2UserInfo;
import com.bookheaven.user_service.entity.User;

public interface OAuth2ProviderStrategy {

    /** The provider name this strategy handles (e.g., "google", "github") */
    String getProviderName();

    /** The corresponding AuthProvider enum value */
    User.AuthProvider getAuthProvider();

    /** Exchange the authorization code for user profile info */
    OAuth2UserInfo exchangeCodeForUserInfo(String authorizationCode);
}
