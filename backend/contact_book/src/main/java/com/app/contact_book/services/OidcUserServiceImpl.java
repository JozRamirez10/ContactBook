package com.app.contact_book.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import com.app.contact_book.dtos.OAuthUserDTO;
import com.app.contact_book.entities.User;

@Service
public class OidcUserServiceImpl extends OidcUserService{

    private final UserService userService;

    public OidcUserServiceImpl(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        Map<String, Object> attributes = new HashMap<>(oidcUser.getClaims());
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        Optional<User> opt = this.userService.findByEmail(email);
        User user = opt.orElseGet(() ->{
            OAuthUserDTO u = new OAuthUserDTO();
            u.setEmail(email);
            u.setUsername(name);
            return this.userService.saveOAuth2(u);
        });

        attributes.put("userId", user.getId());

        return new DefaultOidcUser(Collections.emptyList(), oidcUser.getIdToken(), oidcUser.getUserInfo(), "email"){
            @Override
            public Map<String, Object> getClaims(){
                return attributes;
            }
            @Override
            public Map<String, Object> getAttributes(){
                return attributes;
            }
        };
    }  
}
