package com.app.contact_book.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.contact_book.dtos.ContactDTO;
import com.app.contact_book.dtos.ContactUpdateDTO;
import com.app.contact_book.entities.Contact;
import com.app.contact_book.entities.User;
import com.app.contact_book.repositories.ContactRepository;
import com.app.contact_book.repositories.UserRepository;

@Service
public class ContactServiceImpl implements ContactService{

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ContactDTO> findAll() {
        List<Contact> contacts = new ArrayList<>();
        this.contactRepository.findAll(Sort.unsorted()).forEach(contacts::add);
        return listContactToDto(contacts);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ContactDTO> findById(Long id) {
        return this.contactRepository.findById(id)
            .map(this::contactToDTO);
    }

    @Override
    @Transactional
    public ContactDTO save(ContactDTO dto) {
        Contact contact = dtoToContact(dto);
        return contactToDTO(this.contactRepository.save(contact));
    }

    @Override
    @Transactional
    public Optional<ContactDTO> update(Long id, ContactUpdateDTO update) {
        Optional<Contact> contactOptional = this.contactRepository.findById(id);
        if(contactOptional.isPresent()){
            Contact contactDB = contactOptional.get();
            contactDB.setName(update.getName());
            contactDB.setPhone(update.getPhone());
            contactDB.setEmail(update.getEmail());
            return Optional.of(this.contactRepository.save(contactDB))
                .map(this::contactToDTO);
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        this.contactRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactDTO> findByUserId(Long id) {
        List<Contact> contacts = new ArrayList<>();
        this.contactRepository.findByUser_Id(id).forEach(contacts::add);
        return listContactToDto(contacts);
    }

    private ContactDTO contactToDTO(Contact contact){
        ContactDTO dto = new ContactDTO();
        dto.setId(contact.getId());
        dto.setName(contact.getName());
        dto.setEmail(contact.getEmail());
        dto.setPhone(contact.getPhone());
        dto.setCreatedAt(contact.getCreatedAt());
        dto.setUserId(contact.getUser().getId());
        return dto;
    }

    private Contact dtoToContact(ContactDTO dto){
        Contact contact = new Contact();
        
        contact.setId(dto.getId());
        contact.setName(dto.getName());
        contact.setEmail(dto.getEmail());
        contact.setPhone(dto.getPhone());
        contact.setCreatedAt(dto.getCreatedAt());

        if(dto.getUserId() != null){
            User user = getUserFromDto(dto);
            contact.setUser(user);
        }

        return contact;
    }

    @Transactional(readOnly = true)
    private User getUserFromDto(ContactDTO dto){
        return this.userRepository.findById(dto.getUserId()).orElseThrow(
            () -> new RuntimeException("User not found with id: " + dto.getUserId())
        );
    }

    private List<ContactDTO> listContactToDto(List<Contact> contacts){
        return contacts.stream()
            .map(this::contactToDTO)
            .collect(Collectors.toList());
    }

}
