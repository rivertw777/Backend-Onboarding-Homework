package com.backend.homework.application.service;

import com.backend.homework.application.dto.UserResponse;
import com.backend.homework.domain.model.entity.User;
import com.backend.homework.domain.repository.UserRepository;
import com.backend.homework.domain.service.UserDomainService;
import com.backend.homework.presentation.request.SignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserApplicationService {

    private final PasswordEncoder passwordEncoder;
    private final UserDomainService userDomainService;
    private final UserRepository userRepository;

    @Transactional
    public UserResponse signUp(SignUpRequest request) {
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = userDomainService.createUser(request.username(), encodedPassword, request.nickname());
        userRepository.save(user);
        return UserResponse.from(user);
    }

}
