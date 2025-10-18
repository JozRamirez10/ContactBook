package com.app.contact_book.auth;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import io.jsonwebtoken.Jwts;

@Configuration
public class TokenJwtConfig {

    public static final String CONTENT_TYPE = "application/json";

    public static final String PREFIX_TOKEN = "Bearer ";

    public static final String HEADER_AUTHORIZATION = "Authorization";

    public static final Integer MAX_AGE_REFRESH_TOKEN = 1 * 24 * 60 * 1000; // Days * hours * minutes * millis

    /*
     * Every time the server restarts a new key is created
     */
    //public static final SecretKey SECRET_KEY = Jwts.SIG.HS256.key().build();

    private static String secretKey;

    /*
     * It gets the key from a settings file
     */
    @Value("${jwt.secret}")
    public void setSecretKey(String secretKey){
        TokenJwtConfig.secretKey = secretKey;
    }

    public static SecretKey getSecretKey(){
        return new SecretKeySpec(
            secretKey.getBytes(StandardCharsets.UTF_8)
            , "HmacSHA256");
    }

    public static String getAccessToken(String subject){
        return Jwts.builder()
            .subject(subject)
            .expiration(expirationAccessToken())
            .signWith(getSecretKey())
            .compact();
    }

    public static String getRefreshToken(String subject){
        return Jwts.builder()
            .subject(subject)
            .signWith(getSecretKey())
            .expiration(expirationRefreshToken())
            .compact();
    }

    public static Date expirationAccessToken(){
        return new Date(System.currentTimeMillis() + 3 * 60 * 1000); // minutes * seconds * millis
    }

    public static Date expirationRefreshToken(){
        return new Date(System.currentTimeMillis() + MAX_AGE_REFRESH_TOKEN); 
    }

}
