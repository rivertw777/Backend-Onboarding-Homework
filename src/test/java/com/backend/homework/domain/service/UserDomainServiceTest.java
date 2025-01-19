package com.backend.homework.domain.service;

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
class UserDomainServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDomainService userDomainService;

    @Test
    @DisplayName("사용자 생성 성공 테스트")
    void createUser_Success() {
        // given
        String username = "username";
        String password = "password";
        String nickname = "nickname";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // when
        User createdUser = userDomainService.createUser(username, password, nickname);

        // then
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getUsername()).isEqualTo(username);
        assertThat(createdUser.getPassword()).isEqualTo(password);
        assertThat(createdUser.getNickname()).isEqualTo(nickname);
        verify(userRepository).findByUsername(username);
    }

    @Test
    @DisplayName("중복된 사용자명으로 생성 시 예외 발생")
    void createUser_WithDuplicateUsername_ThrowsException() {
        // given
        String username = "username";
        String password = "password";
        String nickname = "nickname";
        User existingUser = User.create(username, password, nickname);

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(existingUser));

        // when & then
        assertThatThrownBy(() ->
                userDomainService.createUser(username, password, nickname))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("exceptionCase", ExceptionCase.USERNAME_ALREADY_TAKEN);
        verify(userRepository).findByUsername(username);
    }

}