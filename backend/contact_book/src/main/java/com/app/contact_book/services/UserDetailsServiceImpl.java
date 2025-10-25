package com.app.contact_book.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.contact_book.entities.CustomUserDetails;
import com.app.contact_book.entities.User;
import com.app.contact_book.repositories.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService{

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = this.userRepository.findByUsername(username);
        if(userOptional.isEmpty()){
            throw new UsernameNotFoundException(String.format("Username %s doesn't exist", username));
        }
        
        User user = userOptional.get();

        return new CustomUserDetails(user.getId(), user.getUsername(), user.getPassword());
    }

}
