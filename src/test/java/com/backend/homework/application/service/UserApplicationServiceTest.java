package com.backend.homework.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backend.homework.application.dto.UserResponse;
import com.backend.homework.domain.model.entity.User;
import com.backend.homework.domain.repository.UserRepository;
import com.backend.homework.domain.service.UserDomainService;
import com.backend.homework.presentation.request.SignUpRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserApplicationServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserDomainService userDomainService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserApplicationService userApplicationService;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signUp_Success() {
        // given
        SignUpRequest request = new SignUpRequest("username", "1234", "nickname");
        String encodedPassword = "encodedPassword";
        User user = User.create(request.username(), encodedPassword, request.nickname());
        UserResponse userResponse = UserResponse.from(user);

        when(passwordEncoder.encode(request.password())).thenReturn(encodedPassword);
        when(userDomainService.createUser(request.username(), encodedPassword, request.nickname())).thenReturn(user);

        // when
        UserResponse response = userApplicationService.signUp(request);

        // then
        assertThat(response).isEqualTo(userResponse);
        verify(userRepository).save(user);
        verify(passwordEncoder).encode(request.password());
        verify(userDomainService).createUser(request.username(), encodedPassword, request.nickname());
    }

}