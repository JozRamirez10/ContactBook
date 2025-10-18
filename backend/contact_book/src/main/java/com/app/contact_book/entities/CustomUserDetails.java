package com.app.contact_book.entities;

import io.jsonwebtoken.lang.Collections;

public class CustomUserDetails extends org.springframework.security.core.userdetails.User {

    private Long id;

    public CustomUserDetails(Long id, String username, String password){
        super(username, password, Collections.emptyList());
        this.id = id;
    }

    public Long getId(){
        return id;
    }

}
