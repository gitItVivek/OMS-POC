package com.identityservice.dal;

import com.identityservice.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserDal {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
