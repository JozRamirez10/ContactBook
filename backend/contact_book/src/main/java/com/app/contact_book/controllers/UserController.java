package com.app.contact_book.controllers;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.contact_book.entities.CustomUserDetails;
import com.app.contact_book.entities.User;
import com.app.contact_book.services.UserService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.app.contact_book.utils.Utils.validation;;

@CrossOrigin(originPatterns = {"http://localhost:4200"})
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> list() {
        return this.userService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> byId(@PathVariable Long id){
        Optional<User> userOptional = this.userService.findById(id);
        if(userOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.OK).body(userOptional.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("Error", "User not found"));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody User user, BindingResult result){
        if(result.hasErrors()){
            return validation(result);
        }

        if(this.userService.existsByUsername(user.getUsername())){
            return ResponseEntity.badRequest().body(Map.of("username", "Username already exists"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.save(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody User user, BindingResult result, @PathVariable Long id, 
        @AuthenticationPrincipal String userId){
        if(result.hasErrors()){
            return validation(result);
        }
        
        if(!id.equals(Long.parseLong(userId))){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to modify this user"));
        }

        Optional<User> userOptional = this.userService.update(id, user);
        if(userOptional.isPresent()){
            return ResponseEntity.ok(userOptional.get());
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @AuthenticationPrincipal String userId){
        if(!id.equals(Long.parseLong(userId))){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to modify this user"));
        }
        Optional<User> userOptional = this.userService.findById(id);
        if(userOptional.isPresent()){
            this.userService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
