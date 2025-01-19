package com.backend.homework.infrastructure.security;

import com.backend.homework.common.exception.ApplicationException;
import com.backend.homework.common.exception.ExceptionCase;
import com.backend.homework.domain.model.entity.User;
import com.backend.homework.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final JwtManager jwtManager;

    @Override
    public UserDetailsImpl loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApplicationException(ExceptionCase.USER_NOT_FOUND));
        return new UserDetailsImpl(user);
    }

    public Authentication extractAuthentication(String token) {
        String username = jwtManager.parseClaims(token).getSubject();
        UserDetailsImpl userDetails = loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

}
