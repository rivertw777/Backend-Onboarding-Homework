package com.backend.homework.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.backend.homework.application.dto.TokenPair;
import com.backend.homework.common.exception.ApplicationException;
import com.backend.homework.common.exception.ExceptionCase;
import com.backend.homework.domain.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtManagerTest {

    private final String secretKey = "VlwEyVBsYt9V7zq57TejMnVUyzblYcfPQfsdkjfhsdlkfdsjlfkjfye08f7MGVA9XkHa";
    private final String accessTokenExpiration = "3600000";
    private final String refreshTokenExpiration = "86400000";

    private JwtManager jwtManager;

    @BeforeEach
    void setUp() {
        jwtManager = new JwtManager(secretKey, accessTokenExpiration, refreshTokenExpiration);
    }

    @Test
    @DisplayName("토큰 페어 생성 성공 테스트")
    void generateTokenPair_Success() {
        // given
        String username = "username";
        String password = "password";
        String nickname = "nickname";
        User user = User.create(username, password, nickname);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        // when
        TokenPair tokenPair = jwtManager.generateTokenPair(userDetails);

        // then
        assertThat(tokenPair).isNotNull();
        assertThat(tokenPair.accessToken()).isNotNull();
        assertThat(tokenPair.refreshToken()).isNotNull();
    }

    @Test
    @DisplayName("토큰 파싱 성공 테스트")
    void parseClaims_Success() {
        // given
        String username = "username";
        String password = "password";
        String nickname = "nickname";
        User user = User.create(username, password, nickname);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        TokenPair tokenPair = jwtManager.generateTokenPair(userDetails);

        // when
        Claims claims = jwtManager.parseClaims(tokenPair.accessToken());

        // then
        assertThat(claims.getSubject()).isEqualTo("username"); // 수정된 부분
    }

    @Test
    @DisplayName("토큰 검증 성공 테스트")
    void validateToken_Success() {
        // given
        String username = "username";
        String password = "password";
        String nickname = "nickname";
        User user = User.create(username, password, nickname);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        TokenPair tokenPair = jwtManager.generateTokenPair(userDetails);

        // when & then
        assertThatCode(() -> jwtManager.validateToken(tokenPair.accessToken()))
                .doesNotThrowAnyException();
        assertThatCode(() -> jwtManager.validateToken(tokenPair.refreshToken()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패 테스트")
    void validateToken_WithExpiredToken_ThrowsException() {
        // given
        String expiredToken = Jwts.builder()
                .setSubject("username")
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS256)
                .compact();

        // when & then
        assertThatThrownBy(() -> jwtManager.validateToken(expiredToken))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("exceptionCase", ExceptionCase.EXPIRED_TOKEN);
    }

    @Test
    @DisplayName("유효하지 않은 토큰 검증 실패 테스트")
    void validateToken_WithInvalidToken_ThrowsException() {
        // given
        String invalidToken = "invalidToken";

        // when & then
        assertThatThrownBy(() -> jwtManager.validateToken(invalidToken))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("exceptionCase", ExceptionCase.INVALID_TOKEN);
    }

}