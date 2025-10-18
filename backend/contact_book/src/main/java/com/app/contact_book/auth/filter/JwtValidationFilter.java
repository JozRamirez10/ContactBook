package com.app.contact_book.auth.filter;

import static com.app.contact_book.auth.TokenJwtConfig.CONTENT_TYPE;
import static com.app.contact_book.auth.TokenJwtConfig.HEADER_AUTHORIZATION;
import static com.app.contact_book.auth.TokenJwtConfig.PREFIX_TOKEN;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.app.contact_book.auth.TokenJwtConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.lang.Collections;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtValidationFilter extends BasicAuthenticationFilter{

    public JwtValidationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
                
        String header = request.getHeader(HEADER_AUTHORIZATION);

        if(header == null || !header.startsWith(PREFIX_TOKEN)){
            chain.doFilter(request, response);
            return;
        }

        String token = header.replace(PREFIX_TOKEN, "");

        try{
            Claims claims = Jwts.parser()
                .verifyWith(TokenJwtConfig.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

            String userId = claims.getSubject();

            UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
            
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            chain.doFilter(request, response);
        }catch(JwtException e){
            Map<String, String> body = new HashMap<>();
            body.put("message", "Token has expired or is invalid");

            response.getWriter().write(new ObjectMapper().writeValueAsString(body));
            response.setContentType(CONTENT_TYPE);
            response.setStatus(401);
        }
    }

    

}
