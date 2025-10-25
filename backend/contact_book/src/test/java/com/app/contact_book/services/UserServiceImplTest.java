package com.app.contact_book.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.app.contact_book.dtos.OAuthUserDTO;
import com.app.contact_book.dtos.UserDTO;
import com.app.contact_book.entities.User;
import com.app.contact_book.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User user1;
    private User user2;

    @BeforeEach()
    void setUp(){
        user1 = new User();
        user1.setId(1L);
        user1.setUsername("User 1");
        user1.setPassword("hashedPassword1");

        user2 = new User();
        user2.setId(2L);
        user2.setUsername("User 2");
        user2.setPassword("hashedPassword2");
    }

    @Test
    void testFindAll(){
        List<User> expectedUsers = Arrays.asList(user1, user2);
        when(this.userRepository.findAll()).thenReturn(expectedUsers);

        List<User> actualUsers = this.userService.findAll();

        assertNotNull(actualUsers);
        assertEquals(2, actualUsers.size());

        assertEquals(1L, actualUsers.get(0).getId());
        assertEquals("User 1", actualUsers.get(0).getUsername());
        assertEquals("hashedPassword1", actualUsers.get(0).getPassword());
        assertEquals(null, actualUsers.get(0).getEmail());
        
        assertEquals(2L, actualUsers.get(1).getId());
        assertEquals("User 2", actualUsers.get(1).getUsername());
        assertEquals("hashedPassword2", actualUsers.get(1).getPassword());
        assertEquals(null, actualUsers.get(1).getEmail());

        verify(this.userRepository).findAll();
    }

    @Test
    void testFindById(){
        when(this.userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(this.userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(this.userRepository.findById(3L)).thenReturn(Optional.empty());

        Optional<User> userOptional1 = this.userService.findById(1L);
        Optional<User> userOptional2 = this.userService.findById(2L);
        Optional<User> userOptional3 = this.userService.findById(3L);

        assertNotNull(userOptional1);
        assertEquals("User 1", userOptional1.get().getUsername());
        assertEquals("hashedPassword1", userOptional1.get().getPassword());
        assertEquals(null, userOptional1.get().getEmail());
        
        assertNotNull(userOptional2);
        assertEquals("User 2", userOptional2.get().getUsername());
        assertEquals("hashedPassword2", userOptional2.get().getPassword());
        assertEquals(null, userOptional2.get().getEmail());
        
        assertNotNull(userOptional3);
        assertEquals(Optional.empty(), userOptional3);

        verify(this.userRepository).findById(1L);
        verify(this.userRepository).findById(2L);
        verify(this.userRepository).findById(3L);
    }

    @Test
    void testSave(){
        UserDTO dto = new UserDTO();
        dto.setUsername("New User");
        dto.setPassword("1234");
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("hashed1234");

        User savedUser = new User();
        savedUser.setId(3L);
        savedUser.setUsername("New User");
        savedUser.setPassword("hashed1234");
        when(this.userRepository.save(any(User.class))).thenReturn(savedUser);

        User userActual = this.userService.save(dto);
        assertNotNull(userActual);
        assertEquals(3L, userActual.getId());
        assertEquals("New User", userActual.getUsername());
        assertEquals("hashed1234", userActual.getPassword());
        assertEquals(null, userActual.getEmail());

        verify(this.passwordEncoder).encode(dto.getPassword());
        verify(this.userRepository).save(any(User.class));
    }

    @Test
    void testUpdate(){
        UserDTO dto = new UserDTO();
        dto.setUsername("User updated");
        dto.setPassword("Password updated");
        
        when(this.userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(this.passwordEncoder.encode(dto.getPassword())).thenReturn("Password hashed");
        when(this.userRepository.save(any(User.class))).thenAnswer(u -> u.getArgument(0));

        Optional<User> userOptional = this.userService.update(2L, dto);
        assertTrue(userOptional.isPresent());
        
        User userUpdated = userOptional.get();
        assertEquals(2L, userUpdated.getId());
        assertEquals("User updated", userUpdated.getUsername());
        assertEquals("Password hashed", userUpdated.getPassword());

        verify(this.userRepository).findById(2L);
        verify(this.passwordEncoder).encode(dto.getPassword());
        verify(this.userRepository).save(userUpdated);
    }

    @Test
    void testNotUpdate(){
        UserDTO dto = new UserDTO();
        when(this.userRepository.findById(3L)).thenReturn(Optional.empty());
        
        Optional<User> userOptional = this.userService.update(3L, dto);
        assertFalse(userOptional.isPresent());

        verify(this.userRepository).findById(3L);
        verify(this.passwordEncoder, Mockito.never()).encode(ArgumentMatchers.anyString());
        verify(this.userRepository, Mockito.never()).save(any(User.class));
    }


    @Test
    void testDeleteById(){
        doNothing().when(this.userRepository).deleteById(2L);
        this.userService.deleteById(2L);
        verify(this.userRepository).deleteById(2L);
    }

    @Test
    void testExistsById(){
        when(this.userRepository.existsById(2L)).thenReturn(true);
        when(this.userRepository.existsById(3L)).thenReturn(false);

        Boolean result1 = this.userService.existsById(2L);
        assertTrue(result1);
        
        Boolean result2 = this.userService.existsById(3L);
        assertFalse(result2);

        verify(this.userRepository).existsById(2L);
        verify(this.userRepository).existsById(3L);
    }

    @Test
    void testExistsByUsername(){
        when(this.userRepository.existsByUsername("User 1")).thenReturn(true);
        when(this.userRepository.existsByUsername("User X")).thenReturn(false);

        Boolean result1 = this.userService.existsByUsername("User 1");
        assertTrue(result1);
        
        Boolean result2 = this.userService.existsByUsername("User X");
        assertFalse(result2);

        verify(this.userRepository).existsByUsername("User 1");
        verify(this.userRepository).existsByUsername("User X");
    }

    @Test
    void testFindByEmail(){
        User user = new User();
        user.setId(3L);
        user.setUsername("User");
        user.setEmail("user@email.com");
        user.setPassword("hashed1234");

        when(this.userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));
        when(this.userRepository.findByEmail("x@email.com")).thenReturn(Optional.empty());

        Optional<User> userOptional1 = this.userService.findByEmail("user@email.com");
        assertNotNull(userOptional1);
        assertEquals(3L, userOptional1.get().getId());
        assertEquals("User", userOptional1.get().getUsername());
        assertEquals("user@email.com", userOptional1.get().getEmail());
        assertEquals("hashed1234", userOptional1.get().getPassword());
        
        Optional<User> userOptional2 = this.userService.findByEmail("x@email.com");
        assertNotNull(userOptional2);
        assertEquals(Optional.empty(), userOptional2);

        verify(this.userRepository).findByEmail("user@email.com");
        verify(this.userRepository).findByEmail("x@email.com");
    }

    @Test
    void testSaveOAuth2(){
        OAuthUserDTO oauth = new OAuthUserDTO();
        oauth.setUsername("oauth user");
        oauth.setEmail("oauth@email.com");

        User savedUser = new User();
        savedUser.setId(3L);
        savedUser.setUsername("oauth user");
        savedUser.setEmail("oauth@email.com");
        when(this.userRepository.save(any(User.class))).thenReturn(savedUser);

        User userActual = this.userService.saveOAuth2(oauth);
        assertNotNull(userActual);
        assertEquals(3L, userActual.getId());
        assertEquals("oauth user", userActual.getUsername());
        assertEquals("oauth@email.com", userActual.getEmail());
        assertEquals(null, userActual.getPassword());

        verify(this.userRepository).save(any(User.class));
    }

}
