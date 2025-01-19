package com.backend.homework.application.service;

import com.backend.homework.application.dto.TokenPair;
import com.backend.homework.application.dto.TokenResponse;
import com.backend.homework.application.dto.UserResponse;
import com.backend.homework.common.exception.ApplicationException;
import com.backend.homework.common.exception.ExceptionCase;
import com.backend.homework.domain.model.entity.User;
import com.backend.homework.domain.repository.UserRepository;
import com.backend.homework.domain.service.UserDomainService;
import com.backend.homework.infrastructure.security.JwtManager;
import com.backend.homework.infrastructure.security.UserDetailsImpl;
import com.backend.homework.infrastructure.security.UserDetailsServiceImpl;
import com.backend.homework.presentation.request.LoginRequest;
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
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtManager jwtManager;

    @Transactional
    public UserResponse signUp(SignUpRequest request) {
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = userDomainService.createUser(request.username(), encodedPassword, request.nickname());
        userRepository.save(user);
        return UserResponse.from(user);
    }

    public TokenResponse login(LoginRequest request) {
        UserDetailsImpl userDetails = userDetailsService.loadUserByUsername(request.username());
        validatePassword(request.password(), userDetails.getUser().getPassword());
        TokenPair tokenPair = jwtManager.generateTokenPair(userDetails);
        // TODO: refresh 토큰 DB 저장 (ex. redis)
        return new TokenResponse(tokenPair.accessToken());
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new ApplicationException(ExceptionCase.PASSWORD_NOT_MATCH);
        }
    }

}
