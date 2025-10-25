package com.app.contact_book.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.contact_book.dtos.OAuthUserDTO;
import com.app.contact_book.dtos.UserDTO;
import com.app.contact_book.entities.User;
import com.app.contact_book.repositories.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return (List<User>) this.userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        Optional<User> userOptional = this.userRepository.findById(id);
        if(userOptional.isPresent()){
            return userOptional;
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public User save(UserDTO dto) {
        User user = this.dtoToUser(dto);
        if(user.getPassword() != null && !user.getPassword().isEmpty()){
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return this.userRepository.save(user);
    }

    @Override
    @Transactional
    public Optional<User> update(Long id, UserDTO dto) {
        Optional<User> userOptional = this.userRepository.findById(id);
        if(userOptional.isPresent()){
            User userDB = userOptional.get();
            userDB.setUsername(dto.getUsername());
            userDB.setPassword(passwordEncoder.encode(dto.getPassword()));
            return Optional.of(this.userRepository.save(userDB));
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        this.userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return this.userRepository.existsById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return this.userRepository.existsByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public User saveOAuth2(OAuthUserDTO dto) {
        User user = oAuthToUser(dto);
        return this.userRepository.save(user);
    }

    private User dtoToUser(UserDTO dto){
        User user = new User();
        if(dto.getId() != null){
            user.setId(dto.getId());
        }
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        return user;
    }

    private User oAuthToUser(OAuthUserDTO dto){
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        return user;
    }

}
