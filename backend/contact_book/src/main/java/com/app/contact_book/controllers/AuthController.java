package com.app.contact_book.controllers;

import static com.app.contact_book.auth.TokenJwtConfig.HEADER_AUTHORIZATION;
import static com.app.contact_book.auth.TokenJwtConfig.PREFIX_TOKEN;
import static com.app.contact_book.auth.TokenJwtConfig.getAccessToken;
import static com.app.contact_book.auth.TokenJwtConfig.getRefreshToken;
import static com.app.contact_book.auth.CookieJwtConfig.getRefreshCookie;
import static com.app.contact_book.auth.CookieJwtConfig.getCookieExpiration;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.contact_book.auth.TokenJwtConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(value = "refresh_token", required = false) String refreshToken,
        HttpServletResponse response){

        if(refreshToken == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Refresh token is missing"));
        }

        try {
            Claims claims = Jwts.parser()
                .verifyWith(TokenJwtConfig.getSecretKey())
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();

            String userId = claims.getSubject();

            String newAccessToken = getAccessToken(userId);
            
            String newRefreshToken = getRefreshToken(userId);
            Cookie newRefreshCookie = getRefreshCookie("refresh_token", newRefreshToken);
            response.addCookie(newRefreshCookie);
            
            return ResponseEntity.ok()
                .header(HEADER_AUTHORIZATION, PREFIX_TOKEN + newAccessToken)
                .build();

        } catch (Exception e) {
            Cookie cookieRevocked = getCookieExpiration("refresh_token");
            response.addCookie(cookieRevocked);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid refresh token"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response){
        Cookie refreshCookie = getCookieExpiration("refresh_token");
        response.addCookie(refreshCookie);
        return ResponseEntity.ok(Map.of("message", "Logout Success!"));
    }

}
