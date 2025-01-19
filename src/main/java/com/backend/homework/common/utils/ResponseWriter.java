package com.backend.homework.common.utils;

import com.backend.homework.common.exception.ExceptionCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;

public class ResponseWriter {
    public static void setErrorResponse(HttpServletResponse response, ExceptionCase exceptionCase) throws IOException {
        response.setStatus(exceptionCase.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ObjectMapper objectMapper = new ObjectMapper();
        String responseBody = objectMapper.writeValueAsString(exceptionCase.getMessage());
        response.getWriter().write(responseBody);
    }
}
