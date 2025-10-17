package com.app.contact_book.services;

import java.util.List;
import java.util.Optional;

import com.app.contact_book.dtos.ContactDTO;
import com.app.contact_book.dtos.ContactUpdateDTO;

public interface ContactService {
    List<ContactDTO> findAll();
    Optional<ContactDTO> findById(Long id);
    ContactDTO save(ContactDTO contact);
    Optional<ContactDTO> update(Long id, ContactUpdateDTO contact);
    void deleteById(Long id);

    List<ContactDTO> findByUserId(Long id);
}
