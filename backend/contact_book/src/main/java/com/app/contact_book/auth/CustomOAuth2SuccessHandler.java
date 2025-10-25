package com.app.contact_book.auth;

import static com.app.contact_book.auth.CookieJwtConfig.getRefreshCookie;
import static com.app.contact_book.auth.TokenJwtConfig.HEADER_AUTHORIZATION;
import static com.app.contact_book.auth.TokenJwtConfig.PREFIX_TOKEN;
import static com.app.contact_book.auth.TokenJwtConfig.getAccessToken;
import static com.app.contact_book.auth.TokenJwtConfig.getRefreshToken;

import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler{

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OidcUser oidcUser = (OidcUser) oauthToken.getPrincipal();
        Object userIdAttr = oidcUser.getAttributes().get("userId");
        Long userId = Long.valueOf(userIdAttr.toString());
        
        String accessToken = getAccessToken(userId.toString());
        response.addHeader(HEADER_AUTHORIZATION, PREFIX_TOKEN + accessToken);
        
        String refreshToken = getRefreshToken(userId.toString());
        Cookie refreshCookie = getRefreshCookie("refresh_token", refreshToken);
        response.addCookie(refreshCookie);

        response.sendRedirect("http://localhost:8080/api/users");
    }

}
