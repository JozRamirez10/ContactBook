package com.app.contact_book.dtos;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class ContactDTO {

    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Must be a correctly formatted email address")    
    private String email;

    @Pattern(regexp = "^$|^[0-9]{10}$", message = "The phone only must has numbers and must be 10 characters long")
    private String phone;

    private LocalDateTime createdAt;

    @NotNull(message = "User ID is required")
    private Long userId;

}
