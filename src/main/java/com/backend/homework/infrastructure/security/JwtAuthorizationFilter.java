package com.backend.homework.infrastructure.security;

import com.backend.homework.common.exception.ApplicationException;
import com.backend.homework.common.utils.ResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtManager jwtManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // 헤더에서 토큰 추출
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        String token = header.replace("Bearer ", "");

        // 토큰 검증 및 인가
        try {
            jwtManager.validateToken(token);
            Authentication authentication = userDetailsService.extractAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        catch (ApplicationException e) {
            ResponseWriter.setErrorResponse(response, e.getExceptionCase());
            return;
        }
        chain.doFilter(request, response);
    }

}

