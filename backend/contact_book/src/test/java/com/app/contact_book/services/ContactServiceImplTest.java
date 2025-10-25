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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.app.contact_book.dtos.ContactDTO;
import com.app.contact_book.dtos.ContactUpdateDTO;
import com.app.contact_book.entities.Contact;
import com.app.contact_book.entities.User;
import com.app.contact_book.repositories.ContactRepository;
import com.app.contact_book.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
public class ContactServiceImplTest {

    @InjectMocks
    private ContactServiceImpl contactService;  

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private UserRepository userRepository;

    private User user1;
    private Contact contact1;
    private Contact contact2;

    @BeforeEach
    void setUp(){
        user1 = new User();
        user1.setId(1L);
        user1.setUsername("User 1");
        user1.setPassword("hashedPassword1");

        contact1 = new Contact();
        contact1.setId(1L);
        contact1.setName("Contact1");
        contact1.setPhone("5566778899");
        contact1.setEmail("contact1@email.com");
        contact1.setUser(user1);

        contact2 = new Contact();
        contact2.setId(2L);
        contact2.setName("Contact2");
        contact2.setPhone("5566778899");
        contact2.setEmail("contact2@email.com");
        contact2.setUser(user1);
    }

    @Test
    void testFindAll(){
        List<Contact> expectedContacts = Arrays.asList(contact1, contact2);
        when(this.contactRepository.findAll()).thenReturn(expectedContacts);

        List<Contact> actualContacts = this.contactRepository.findAll();

        assertNotNull(actualContacts);
        assertEquals(2, actualContacts.size());

        assertEquals(1L, actualContacts.get(0).getId());
        assertEquals("Contact1", actualContacts.get(0).getName());
        assertEquals("5566778899", actualContacts.get(0).getPhone());
        assertEquals("contact1@email.com", actualContacts.get(0).getEmail());

        assertEquals(1L, actualContacts.get(0).getUser().getId());
        assertEquals("User 1", actualContacts.get(0).getUser().getUsername());
        assertEquals("hashedPassword1", actualContacts.get(0).getUser().getPassword());

        assertEquals(2L, actualContacts.get(1).getId());
        assertEquals("Contact2", actualContacts.get(1).getName());
        assertEquals("5566778899", actualContacts.get(1).getPhone());
        assertEquals("contact2@email.com", actualContacts.get(1).getEmail());

        assertEquals(1L, actualContacts.get(1).getUser().getId());
        assertEquals("User 1", actualContacts.get(1).getUser().getUsername());
        assertEquals("hashedPassword1", actualContacts.get(1).getUser().getPassword());

        verify(this.contactRepository).findAll();
    }

    @Test
    void testFindById(){
        when(this.contactRepository.findById(2L)).thenReturn(Optional.of(contact2));
        when(this.contactRepository.findById(3L)).thenReturn(Optional.empty());

        Optional<ContactDTO> contactOptional1 = this.contactService.findById(2L);

        assertNotNull(contactOptional1);
        assertEquals(2L, contactOptional1.get().getId());
        assertEquals("Contact2", contactOptional1.get().getName());
        assertEquals("5566778899", contactOptional1.get().getPhone());
        assertEquals("contact2@email.com", contactOptional1.get().getEmail());
        assertEquals(1L, contactOptional1.get().getUserId());

        Optional<ContactDTO> contactOptional2 = this.contactService.findById(3L);

        assertNotNull(contactOptional2);
        assertEquals(Optional.empty(), contactOptional2);

        verify(this.contactRepository).findById(2L);
        verify(this.contactRepository).findById(3L);
    }

    @Test
    void testSave(){
        ContactDTO dto = new ContactDTO();
        dto.setName("New contact");
        dto.setPhone("5511223344");
        dto.setEmail("contact@email.com");
        dto.setUserId(1L);
        when(this.contactRepository.save(any(Contact.class))).thenAnswer( c -> {
            Contact contactToSave = c.getArgument(0);
            contactToSave.setId(3L);
            return contactToSave;
        });
        when(this.userRepository.findById(1L)).thenReturn(Optional.of(user1));

        ContactDTO contactActual = this.contactService.save(dto);

        assertNotNull(contactActual);
        assertNotNull(contactActual.getId());

        assertEquals(3L, contactActual.getId());
        assertEquals("New contact", contactActual.getName());
        assertEquals("5511223344", contactActual.getPhone());
        assertEquals("contact@email.com", contactActual.getEmail());
        assertEquals(1L, contactActual.getUserId());

        verify(this.contactRepository).save(any(Contact.class));
    }

    @Test
    void testUpdate(){
        when(this.contactRepository.findById(2L)).thenReturn(Optional.of(contact2));
        when(this.contactRepository.save(any(Contact.class))).thenAnswer(c -> c.getArgument(0));

        ContactUpdateDTO dto = new ContactUpdateDTO();
        dto.setName("Contact updated");
        dto.setPhone("5500998877");
        dto.setEmail("updated@email.com");

        Optional<ContactDTO> contactOptional = this.contactService.update(2L, dto);

        assertNotNull(contactOptional);
        assertTrue(contactOptional.isPresent());
        assertNotNull(contactOptional.get().getId());

        assertEquals(2L, contactOptional.get().getId());
        assertEquals("Contact updated", contactOptional.get().getName());
        assertEquals("5500998877", contactOptional.get().getPhone());
        assertEquals("updated@email.com", contactOptional.get().getEmail());
        assertEquals(user1.getId(), contactOptional.get().getUserId());

        verify(this.contactRepository).findById(2L);
        verify(this.contactRepository).save(any(Contact.class));
    }

    @Test
    void testNotUpdate(){
        ContactUpdateDTO dto = new ContactUpdateDTO();
        when(this.contactRepository.findById(3L)).thenReturn(Optional.empty());

        Optional<ContactDTO> contactOptional = this.contactService.update(3L, dto);
        assertFalse(contactOptional.isPresent());
        assertEquals(Optional.empty(), contactOptional);

        verify(this.contactRepository).findById(3L);
        verify(this.contactRepository, Mockito.never()).save(any(Contact.class));

    }

    @Test
    void testDeleteById(){
        doNothing().when(this.contactRepository).deleteById(2L);
        this.contactService.deleteById(2L);
        verify(this.contactRepository).deleteById(2L);
    }

}
