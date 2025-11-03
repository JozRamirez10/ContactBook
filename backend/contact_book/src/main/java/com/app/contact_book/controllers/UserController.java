package com.app.contact_book.controllers;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.contact_book.dtos.UserDTO;
import com.app.contact_book.entities.User;
import com.app.contact_book.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.app.contact_book.utils.Utils.validation;;

@Tag(name = "Users", description = "Endpoints for user management.")
@CrossOrigin(originPatterns = {"http://localhost:4200"})
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(
        summary = "List all users (Development only)",
        description = "Returns a list of all users registered on the system. This endpoint should not be in production."
    )
    @ApiResponse(
        responseCode = "200",
        description =  "User list obtained successfully.",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = (User.class))))
    )
    @GetMapping
    public List<User> list() {
        return this.userService.findAll();
    }

    @Operation(
        summary = "Get a user by ID",
        description = "Authentication required. Users can only view their own profile."
    )
    @ApiResponse(responseCode = "200", description = "User found.", content = @Content(schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "404", description = "User not found.")
    @ApiResponse(responseCode = "403", description = "Access denied.")
    @GetMapping("/{id}")
    public ResponseEntity<?> byId(@PathVariable Long id, @AuthenticationPrincipal String userId){
        Optional<User> userOptional = this.userService.findById(id);
        if(userOptional.isPresent()){

            if(!userOptional.get().getId().equals(Long.parseLong(userId))){
                return notPermission();
            }

            return ResponseEntity.status(HttpStatus.OK).body(userOptional.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("Error", "User not found"));
    }

    @Operation(
        summary = "New user registration",
        description = "Allows a new user to register in the system. Authentication isn't required."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "New user data (username & password)",
        required = true,
        content = @Content(schema = @Schema(implementation = UserDTO.class))
    )
    @ApiResponse(responseCode = "201", description = "User created successfully", content = @Content(schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "400", description = "Validation errors: Empty fields or user exists.")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody UserDTO user, BindingResult result){
        if(result.hasErrors()){
            return validation(result);
        }

        if(this.userService.existsByUsername(user.getUsername())){
            return ResponseEntity.badRequest().body(Map.of("username", "Username already exists"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.save(user));
    }

    @Operation(
        summary = "User update by ID",
        description = "Authentication required. Only the user can update your own account."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Update user data (username & password)",
        required = true,
        content = @Content(schema = @Schema(implementation = UserDTO.class))
    )
    @ApiResponse(responseCode = "200", description = "User updated successfully.", content = @Content(schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "400", description = "Validation errors.")
    @ApiResponse(responseCode = "403", description = "Access denied.")
    @ApiResponse(responseCode = "404", description = "User to update not found.")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody UserDTO user, BindingResult result, @PathVariable Long id, 
        @AuthenticationPrincipal String userId){
        if(result.hasErrors()){
            return validation(result);
        }
        
        if(!id.equals(Long.parseLong(userId))){
            return notPermission();
        }

        Optional<User> userOptional = this.userService.update(id, user);
        if(userOptional.isPresent()){
            return ResponseEntity.ok(userOptional.get());
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(
        summary = "Delete user by ID",
        description = "Authentication required. Only the user can delete your own account. Return '204 No Content' if is successfully."
    )
    @ApiResponse(responseCode = "204", description = "User delete successfully.")
    @ApiResponse(responseCode = "403", description = "Access denied.")
    @ApiResponse(responseCode = "404", description = "User to delete not found.")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @AuthenticationPrincipal String userId){
        if(!id.equals(Long.parseLong(userId))){
            return notPermission();
        }
        Optional<User> userOptional = this.userService.findById(id);
        if(userOptional.isPresent()){
            this.userService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private ResponseEntity<?> notPermission(){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to modify this user"));
    }
}