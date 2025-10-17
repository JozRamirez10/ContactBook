package com.app.contact_book.controllers;

import static com.app.contact_book.utils.Utils.validation;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.contact_book.dtos.ContactDTO;
import com.app.contact_book.dtos.ContactUpdateDTO;
import com.app.contact_book.entities.User;
import com.app.contact_book.services.ContactService;
import com.app.contact_book.services.UserService;

import jakarta.validation.Valid;

@CrossOrigin(originPatterns = {"http://localhost:4200"})
@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @Autowired
    private UserService userService;

    @GetMapping
    public List<ContactDTO> list(){
        return contactService.findAll();
    }

    @GetMapping("/user/{id}") 
    public ResponseEntity<?> byUser(@PathVariable Long id){
        if(this.userService.existsById(id)){
            return ResponseEntity.status(HttpStatus.OK).body(this.contactService.findByUserId(id));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> byId(@PathVariable Long id){
        Optional<ContactDTO> contactOptional = this.contactService.findById(id);
        if(contactOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.OK).body(contactOptional.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("Error", "Contact not found"));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ContactDTO contact, BindingResult result){
        if(result.hasErrors()){
            return validation(result);
        }
        Optional<User> userOptional = this.userService.findById(contact.getUserId());
        if(userOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.CREATED).body(this.contactService.save(contact));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("Error", "User ID not found"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody ContactUpdateDTO contact, BindingResult result, @PathVariable Long id){
        if(result.hasErrors()){
            return validation(result);
        }
        Optional<ContactDTO> contactOptional = this.contactService.update(id, contact);
        if(contactOptional.isPresent()){
            return ResponseEntity.ok(contactOptional.get());
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        Optional<ContactDTO> contactOptional = this.contactService.findById(id);
        if(contactOptional.isPresent()){
            this.contactService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

}
