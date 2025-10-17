package com.app.contact_book.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.contact_book.entities.Contact;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long>{

    List<Contact> findByUser_Id(Long userId);

    Page<Contact> findByUser_Id(Long userId, Pageable pageable);
}
