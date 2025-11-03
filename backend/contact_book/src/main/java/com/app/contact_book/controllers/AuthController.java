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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "Authentication", description = "Endpoints for managing and rotating session tokens.")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Operation(
        summary = "Refresh the Access Token",
        description = "Use the Refresh Token (stored in an HttpOnly cookie) to obtain a new Access Token. If successfully, it also rotates the Refresh Token."
    )
    @Parameter(
        name = "refresh_token",
        description = "Refresh Token JWT automatic sent in a cookie.",
        example = "eyJhbGciOiJIUzI1NiJ9...",
        in = io.swagger.v3.oas.annotations.enums.ParameterIn.COOKIE
    )
    @ApiResponse(
        responseCode = "200",
        description = "Access Token refreshed: Return a new Access Token in 'Authorization' header and a new Refresh Token in a cookie.",
        headers = @io.swagger.v3.oas.annotations.headers.Header(
            name = HEADER_AUTHORIZATION,
            description = "New Access Token (Bearer token)",
            schema = @Schema(type = "String", example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "Refresh Token invalid or null",
        content = @Content(schema = @Schema(implementation = Map.class))
    )
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

    @Operation(
        summary = "Log out",
        description = "Invalidates the session by expiring the Refresh Token."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Log out successfully."
    )
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response){
        Cookie refreshCookie = getCookieExpiration("refresh_token");
        response.addCookie(refreshCookie);
        return ResponseEntity.ok(Map.of("message", "Logout Success!"));
    }

}
