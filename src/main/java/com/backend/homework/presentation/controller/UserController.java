package com.backend.homework.presentation.controller;

import com.backend.homework.application.dto.TokenResponse;
import com.backend.homework.application.dto.UserResponse;
import com.backend.homework.application.service.UserApplicationService;
import com.backend.homework.infrastructure.security.UserDetailsImpl;
import com.backend.homework.presentation.request.LoginRequest;
import com.backend.homework.presentation.request.SignUpRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/users")
@RestController
public class UserController {

    private final UserApplicationService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        return ResponseEntity
                .status(201)
                .body(userService.signUp(request));
    }

    @PostMapping("/sign")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity
                .status(200)
                .body(userService.login(request));
    }

    @GetMapping
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity
                .status(200)
                .body(userService.getMyInfo(userDetails.getUser().getUsername()));
    }

}
