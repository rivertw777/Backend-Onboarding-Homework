package com.backend.homework.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ExceptionCase {

    USERNAME_ALREADY_TAKEN(HttpStatus.BAD_REQUEST, "이미 등록된 사용자 이름입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
