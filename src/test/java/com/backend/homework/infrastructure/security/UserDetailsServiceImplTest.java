package com.backend.homework.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backend.homework.common.exception.ApplicationException;
import com.backend.homework.common.exception.ExceptionCase;
import com.backend.homework.domain.model.entity.User;
import com.backend.homework.domain.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("사용자명으로 UserDetails 조회 성공 테스트")
    void loadUserByUsername_Success() {
        // given
        String username = "username";
        String password = "password";
        String nickname = "nickname";

        User user = User.create(username, password, nickname);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // when
        UserDetailsImpl userDetails = userDetailsService.loadUserByUsername(username);

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(username);
        assertThat(userDetails.getPassword()).isEqualTo(password);
        verify(userRepository).findByUsername(username);
    }

    @Test
    @DisplayName("존재하지 않는 사용자명으로 조회 시 예외 발생 테스트")
    void loadUserByUsername_UserNotFound_ThrowsException() {
        // given
        String username = "username";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(username))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("exceptionCase", ExceptionCase.USER_NOT_FOUND);
        verify(userRepository).findByUsername(username);
    }

}