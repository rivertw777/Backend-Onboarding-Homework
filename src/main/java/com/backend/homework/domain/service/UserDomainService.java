package com.backend.homework.domain.service;

import com.backend.homework.common.exception.ApplicationException;
import com.backend.homework.common.exception.ExceptionCase;
import com.backend.homework.domain.model.entity.User;
import com.backend.homework.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserDomainService {

    private final UserRepository userRepository;

    public User createUser(String username, String encodedPassword, String nickname) {
        validateUsernameDuplication(username);
        return User.create(username, encodedPassword, nickname);
    }

    private void validateUsernameDuplication(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ApplicationException(ExceptionCase.USERNAME_ALREADY_TAKEN);
        }
    }

}