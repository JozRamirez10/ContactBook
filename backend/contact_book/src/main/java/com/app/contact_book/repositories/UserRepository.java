package com.app.contact_book.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.app.contact_book.entities.User;

public interface UserRepository extends CrudRepository<User, Long>{

    Optional<User> findByUsername(String username);

    boolean existsById(Long id);
    
    boolean existsByUsername(String username);
}
