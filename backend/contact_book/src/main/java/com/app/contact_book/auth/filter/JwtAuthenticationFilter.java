package com.app.contact_book.auth.filter;

import static com.app.contact_book.auth.TokenJwtConfig.CONTENT_TYPE;
import static com.app.contact_book.auth.TokenJwtConfig.HEADER_AUTHORIZATION;
import static com.app.contact_book.auth.TokenJwtConfig.PREFIX_TOKEN;
import static com.app.contact_book.auth.TokenJwtConfig.getAccessToken;
import static com.app.contact_book.auth.TokenJwtConfig.getRefreshToken;
import static com.app.contact_book.auth.CookieJwtConfig.getRefreshCookie;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.app.contact_book.entities.CustomUserDetails;
import com.app.contact_book.entities.User;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager){
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        
        User user = null;
        String username = null;
        String password = null;

        try {
            user = new ObjectMapper().readValue(request.getInputStream(), User.class);
            username = user.getUsername();
            password = user.getPassword();
        } catch (StreamReadException e) {
            e.printStackTrace();
        } catch (DatabindException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        UsernamePasswordAuthenticationToken authenticationToken = 
            new UsernamePasswordAuthenticationToken(username, password);

        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {

        CustomUserDetails user = (CustomUserDetails) authResult.getPrincipal();

        Long userId = user.getId();

        String accessToken = getAccessToken(userId.toString());
        response.addHeader(HEADER_AUTHORIZATION, PREFIX_TOKEN + accessToken);
        
        String refreshToken = getRefreshToken(userId.toString());
        Cookie refreshCookie = getRefreshCookie("refresh_token", refreshToken);
        response.addCookie(refreshCookie);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) throws IOException, ServletException {
        
        Map<String, String> body = new HashMap<>();
        body.put("message", "Email or password incorrect");
        body.put("error", failed.getMessage());

        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.setContentType(CONTENT_TYPE);
        response.setStatus(401);
    }

}
