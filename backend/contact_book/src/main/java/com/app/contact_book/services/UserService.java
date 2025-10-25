package com.app.contact_book.services;

import java.util.List;
import java.util.Optional;

import com.app.contact_book.dtos.OAuthUserDTO;
import com.app.contact_book.dtos.UserDTO;
import com.app.contact_book.entities.User;

public interface UserService {
    List<User> findAll();
    Optional<User> findById(Long id);
    User save(UserDTO dto);
    Optional<User> update(Long id, UserDTO dto);
    void deleteById(Long id); 
    
    boolean existsById(Long id);
    boolean existsByUsername(String username);
    Optional<User> findByEmail(String email);
    
    User saveOAuth2(OAuthUserDTO u);
}
