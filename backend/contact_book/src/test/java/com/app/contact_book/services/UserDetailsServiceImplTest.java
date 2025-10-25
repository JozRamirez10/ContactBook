package com.app.contact_book.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.app.contact_book.entities.CustomUserDetails;
import com.app.contact_book.entities.User;
import com.app.contact_book.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private UserRepository userRepository;
    
    private User user;
    private Long id = 1L;
    private String username = "User 1";
    private String password = "hashedPassword";

    @BeforeEach
    void setUp(){
        this.user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
    }

    @Test
    void testLoadUserByUsername(){
        when(this.userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        
        UserDetails userActual = this.userDetailsService.loadUserByUsername(username);
        assertEquals(username, userActual.getUsername());
        assertEquals(password, userActual.getPassword());

        assertEquals(id, ((CustomUserDetails)userActual).getId());

        verify(this.userRepository).findByUsername(username);
    }

    @Test
    void testNotLoadUserByUsername(){
        String userNotExists = "I not exists";
        when(this.userRepository.findByUsername(userNotExists)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            this.userDetailsService.loadUserByUsername(userNotExists);
        });

        verify(this.userRepository).findByUsername(userNotExists);
    }

}
