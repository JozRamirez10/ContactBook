package com.app.contact_book.services;

import java.util.List;
import java.util.Optional;

import com.app.contact_book.entities.User;

public interface UserService {
    List<User> findAll();
    Optional<User> findById(Long id);
    User save(User user);
    Optional<User> update(Long id, User user);
    void deleteById(Long id); 
    
    boolean existsById(Long id);
    boolean existsByUsername(String username);
}
