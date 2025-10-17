package com.app.contact_book.repositories;

import org.springframework.data.repository.CrudRepository;

import com.app.contact_book.entities.User;

public interface UserRepository extends CrudRepository<User, Long>{

    boolean existsById(Long id);

}
