package com.backend.homework.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backend.homework.common.exception.ApplicationException;
import com.backend.homework.common.exception.ExceptionCase;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthorizationFilterTest {

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private JwtManager jwtManager;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthorizationFilter jwtAuthorizationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Authorization 헤더가 없을 경우 필터 체인 진행 테스트")
    void doFilterInternal_NoAuthHeader_ContinueFilterChain() throws ServletException, IOException {
        // given
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        // when
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verify(jwtManager, never()).validateToken(any());
        verify(userDetailsService, never()).extractAuthentication(any());
    }

    @Test
    @DisplayName("올바른 Bearer 토큰으로 인증 성공 테스트")
    void doFilterInternal_ValidToken_Success() throws ServletException, IOException {
        // given
        String token = "token";
        String bearerToken = "Bearer " + token;
        Authentication authentication = mock(Authentication.class);

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);
        doNothing().when(jwtManager).validateToken(token);
        when(userDetailsService.extractAuthentication(token)).thenReturn(authentication);

        // when
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtManager).validateToken(token);
        verify(userDetailsService).extractAuthentication(token);
        verify(filterChain).doFilter(request, response);

        Authentication resultAuth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(resultAuth).isEqualTo(authentication);
    }

    @Test
    @DisplayName("잘못된 토큰으로 인증 실패 테스트")
    void doFilterInternal_InvalidToken_SetErrorResponse() throws ServletException, IOException {
        // given
        String token = "token";
        String bearerToken = "Bearer " + token;
        ApplicationException exception = new ApplicationException(ExceptionCase.INVALID_TOKEN);
        PrintWriter writer = mock(PrintWriter.class);

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);
        doThrow(exception).when(jwtManager).validateToken(token);
        when(response.getWriter()).thenReturn(writer);

        // when
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtManager).validateToken(token);
        verify(response).setStatus(ExceptionCase.INVALID_TOKEN.getHttpStatus().value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Bearer 접두사가 없는 토큰 처리 테스트")
    void doFilterInternal_NonBearerToken_ContinueFilterChain() throws ServletException, IOException {
        // given
        String token = "token";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(token);

        // when
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verify(jwtManager, never()).validateToken(any());
        verify(userDetailsService, never()).extractAuthentication(any());
    }

}