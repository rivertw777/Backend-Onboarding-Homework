package com.backend.homework.domain.repository;

import com.backend.homework.domain.model.entity.User;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findByUsername(String username);
}
