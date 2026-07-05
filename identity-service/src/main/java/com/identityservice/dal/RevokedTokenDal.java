package com.identityservice.dal;

import com.identityservice.entity.RevokedToken;

import java.util.Optional;

public interface RevokedTokenDal {

    RevokedToken save(RevokedToken revokedToken);

    boolean existsByTokenHash(String tokenHash);

    Optional<RevokedToken> findByTokenHash(String tokenHash);
}
