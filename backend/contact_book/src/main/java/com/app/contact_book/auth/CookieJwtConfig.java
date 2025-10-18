package com.app.contact_book.auth;

import static com.app.contact_book.auth.TokenJwtConfig.MAX_AGE_REFRESH_TOKEN;

import jakarta.servlet.http.Cookie;

public class CookieJwtConfig {

    public static Cookie getRefreshCookie(String name, String value){
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(MAX_AGE_REFRESH_TOKEN);
        cookie.setAttribute("SameSite", "Strict");
        return cookie;
    }

    public static Cookie getCookieExpiration(String name){
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }

}
