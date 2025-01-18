package com.backend.homework.application.dto;

import com.backend.homework.domain.model.entity.User;
import java.util.List;

public record UserResponse(String username, String nickname, List<AuthoritiesResponse> authorities) {
    public static UserResponse from(User user) {
        List<AuthoritiesResponse> authorities = user.getAuthorities().stream()
                .map(authority -> new AuthoritiesResponse(authority.getValue()))
                .toList();
        return new UserResponse(
                user.getUsername(),
                user.getNickname(),
                authorities
        );
    }
}
