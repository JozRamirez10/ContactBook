package com.app.contact_book.controllers;

import static com.app.contact_book.utils.Utils.validation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.app.contact_book.services.ContactService;
import com.app.contact_book.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Contacts", description = "Endpoints for contact management.")
@CrossOrigin(originPatterns = {"http://localhost:4200"})
@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @Autowired
    private UserService userService;

    @Operation(
        summary = "List all contacts (Development only)",
        description = "Returns a list of all contacts registered on the system. This endpoint should not be in production."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Contact list obtained successfully.",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = (ContactDTO.class))))
    )
    @GetMapping
    public List<ContactDTO> list(){
        return contactService.findAll();
    }

    @Operation(
        summary = "Get contact list by user ID",
        description = "Authentication required. Users can only view their own contacts."
    )
    @ApiResponse(responseCode = "200", description = "Contacts found by user.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = (ContactDTO.class)))))
    @ApiResponse(responseCode = "404", description = "User not found.")
    @ApiResponse(responseCode = "403", description = "Access denied.")
    @GetMapping("/user/{id}") 
    public ResponseEntity<?> byUser(@PathVariable Long id, @AuthenticationPrincipal String userId){
        Long longUserId = Long.parseLong(userId);
        if(this.userService.existsById(longUserId)){
            if(!id.equals(longUserId)){
                return notPermission();
            }
            return ResponseEntity.status(HttpStatus.OK).body(this.contactService.findByUserId(id));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(
        summary = "Get a contact by ID",
        description = "Authentication required. Users can only view their own contacts."
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Contact found.", content = @Content(schema = @Schema(implementation = ContactDTO.class))
    )
    @ApiResponse(responseCode = "403", description = "Access denied.")
    @ApiResponse(responseCode = "404", description = "Contact not found.")
    @GetMapping("/{id}")
    public ResponseEntity<?> byId(@PathVariable Long id, @AuthenticationPrincipal String userId){
        Optional<ContactDTO> contactOptional = this.contactService.findById(id);
        if(contactOptional.isPresent()){
            if(!contactOptional.get().getUserId().equals(Long.parseLong(userId))){
                return notPermission();
            }
            return ResponseEntity.status(HttpStatus.OK).body(contactOptional.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("Error", "Contact not found"));
    }

    @Operation(
        summary = "New contact registration",
        description = "Allows a new contact to register in the system. Save contact by user authenticated."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "New contact data",
        required = true,
        content = @Content(schema = @Schema(implementation = ContactDTO.class))
    )
    @ApiResponse(responseCode = "201", description = "Contact created successfully.", content = @Content(schema = @Schema(implementation = ContactDTO.class)))
    @ApiResponse(responseCode = "400", description = "Validation errors")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ContactDTO contact, BindingResult result,
        @AuthenticationPrincipal String userId){
        if(result.hasErrors()){
            return validation(result);
        }
        contact.setUserId(Long.parseLong(userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(this.contactService.save(contact));
    }

    @Operation(
        summary = "Contact update by ID",
        description = "Authentication required. Only the user can update their own contacts."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Update contact data",
        required = true,
        content = @Content(schema = @Schema(implementation = ContactUpdateDTO.class))
    )
    @ApiResponse(responseCode = "200", description = "Contact updated successfully.", content = @Content(schema = @Schema(implementation = ContactDTO.class)))
    @ApiResponse(responseCode = "400", description = "Validation errors.")
    @ApiResponse(responseCode = "403", description = "Access denied.")
    @ApiResponse(responseCode = "404", description = "Contact to update not found.")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody ContactUpdateDTO contact, BindingResult result, @PathVariable Long id,
        @AuthenticationPrincipal String userId){
        if(result.hasErrors()){
            return validation(result);
        }
        Optional<ContactDTO> contactOp = this.contactService.findById(id);
        if(contactOp.isPresent()){
            if(!contactOp.get().getUserId().equals(Long.parseLong(userId))){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to modify this contact"));
            }
            Optional<ContactDTO> contactOptional = this.contactService.update(id, contact);
            if(contactOptional.isPresent()){
                return ResponseEntity.ok(contactOptional.get());
            }
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(
        summary = "Delete contact by ID", 
        description = "Authentication required. Only the user can delete their own contacts. Return '204 No Content' if is successfully."
    )
    @ApiResponse(responseCode = "204", description = "Contact delete successfully.")
    @ApiResponse(responseCode = "403", description = "Access denied.")
    @ApiResponse(responseCode = "404", description = "Contact to delete not found.")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @AuthenticationPrincipal String userId){
        Optional<ContactDTO> contactOptional = this.contactService.findById(id);
        if(contactOptional.isPresent()){
            if(!contactOptional.get().getUserId().equals(Long.parseLong(userId))){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to modify this contact"));
            }
            this.contactService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private ResponseEntity<?> notPermission(){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission"));
    }

}
