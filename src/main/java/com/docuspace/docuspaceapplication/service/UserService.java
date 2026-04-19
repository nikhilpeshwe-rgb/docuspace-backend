package com.docuspace.docuspaceapplication.service;

import com.docuspace.docuspaceapplication.entity.User;
import com.docuspace.docuspaceapplication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Optional<User> findByEmail(String emailId)
    {
        return userRepository.findByEmail(emailId);
    }
}
