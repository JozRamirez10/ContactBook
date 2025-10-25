package com.app.contact_book.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class OAuthUserDTO {

    private Long id;

    @NotBlank(message = "Username is required")
    private String username;

    @Email(message = "Must be a correctly formatted email address")
    private String email;

}
