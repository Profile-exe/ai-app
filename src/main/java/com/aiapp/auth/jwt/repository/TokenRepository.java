package com.aiapp.auth.jwt.repository;

import com.aiapp.auth.jwt.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface TokenRepository extends CrudRepository<RefreshToken, Long> {

    boolean existsByToken(String refreshToken);
}
