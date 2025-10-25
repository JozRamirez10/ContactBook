package com.app.contact_book.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.app.contact_book.dtos.OAuthUserDTO;
import com.app.contact_book.entities.User;

@ExtendWith(MockitoExtension.class)
public class OidcUserServiceImplTest {

    @InjectMocks
    private OidcUserServiceImpl oidcUserService;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private OidcUserRequest oidcUserRequest;

    @Mock
    private ClientRegistration clientRegistration;

    @Mock
    private OAuth2AccessToken accessToken;

    private final String TEST_EMAIL = "test@google.com";
    private final String TEST_NAME = "Test User Google";
    private final String TEST_SUB = "google-sub-12345";
    private final Long ID = 1L;
    
    private OidcIdToken idToken;
    
    @BeforeEach
    void setUp(){
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", TEST_EMAIL);
        claims.put("name", TEST_NAME);
        claims.put("sub", TEST_SUB);

        idToken = new OidcIdToken(
            "token value", 
            Instant.now(), 
            Instant.now().plusSeconds(3600), 
            claims
        );

        when(this.oidcUserRequest.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getScopes()).thenReturn(Set.of("openid", "profile", "email"));

        ClientRegistration.ProviderDetails providerDetails = Mockito.mock(ClientRegistration.ProviderDetails.class);
        ClientRegistration.ProviderDetails.UserInfoEndpoint userInfoEndpoint = 
            Mockito.mock(ClientRegistration.ProviderDetails.UserInfoEndpoint.class);

        when(this.oidcUserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(this.clientRegistration.getProviderDetails()).thenReturn(providerDetails);
        when(providerDetails.getUserInfoEndpoint()).thenReturn(userInfoEndpoint);
        when(userInfoEndpoint.getUri()).thenReturn("https://fake.uri/userinfo");

        when(oidcUserRequest.getIdToken()).thenReturn(idToken);
    }

    @Test
    void testLoadUser(){
        when(this.userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        User newUser = new User();
        newUser.setId(ID);
        newUser.setEmail(TEST_EMAIL);
        newUser.setUsername(TEST_NAME);

        when(this.userService.saveOAuth2(any(OAuthUserDTO.class))).thenReturn(newUser);

        OidcUser resultUser = oidcUserService.loadUser(oidcUserRequest);

        assertNotNull(resultUser);
        assertEquals(ID, resultUser.getAttributes().get("userId"));

        verify(this.userService).findByEmail(TEST_EMAIL);
        verify(this.userService).saveOAuth2(any(OAuthUserDTO.class));
    }

    @Test
    void testLoadUserExists(){
        User user = new User();
        user.setId(ID);
        user.setUsername(TEST_NAME);
        user.setEmail(TEST_EMAIL);
        when(this.userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));

        OidcUser resultUser = this.oidcUserService.loadUser(oidcUserRequest);

        assertNotNull(resultUser);
        assertEquals(ID, resultUser.getAttributes().get("userId"));

        verify(this.userService).findByEmail(TEST_EMAIL);
        verify(this.userService, Mockito.never()).saveOAuth2(any(OAuthUserDTO.class));
    }

}
