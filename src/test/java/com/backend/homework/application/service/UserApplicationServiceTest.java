package com.backend.homework.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private JwtManager jwtManager;

    @InjectMocks
    private UserApplicationService userApplicationService;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signUp_Success() {
        // given
        String username = "username";
        String password = "password";
        String nickname = "nickname";
        String encodedPassword = "encodedPassword";

        SignUpRequest request = new SignUpRequest(username, password, nickname);
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

    @Test
    @DisplayName("로그인 성공 테스트")
    void login_Success() {
        // given
        String username = "username";
        String password = "password";
        String nickname = "nickname";
        String encodedPassword = "encodedPassword";
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        LoginRequest request = new LoginRequest(username, password);
        User user = User.create(username, encodedPassword, nickname);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        TokenPair tokenPair = new TokenPair(accessToken, refreshToken);

        when(userDetailsService.loadUserByUsername(request.username())).thenReturn(userDetails);
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
        when(jwtManager.generateTokenPair(userDetails)).thenReturn(tokenPair);

        // when
        TokenResponse response = userApplicationService.login(request);

        // then
        assertThat(response).isNotNull();
        verify(userDetailsService).loadUserByUsername(request.username());
        verify(passwordEncoder).matches(request.password(), user.getPassword());
        verify(jwtManager).generateTokenPair(userDetails);
    }

    @Test
    @DisplayName("비밀번호 다를 시 예외 발생")
    void login_WithDifferentPassword_ThrowsException() {
        // given
        String username = "username";
        String password = "password";
        String nickname = "nickname";
        String encodedPassword = "encodedPassword";

        LoginRequest request = new LoginRequest(username, password);
        User user = User.create(username, encodedPassword, nickname);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        when(userDetailsService.loadUserByUsername(request.username())).thenReturn(userDetails);
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

        // when & then
        assertThatThrownBy(() ->
                userApplicationService.login(request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("exceptionCase", ExceptionCase.PASSWORD_NOT_MATCH);
        verify(userDetailsService).loadUserByUsername(request.username());
        verify(passwordEncoder).matches(request.password(), user.getPassword());
    }

}