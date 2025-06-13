package com.project.citasalud.tokenJWT;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    List<Token> findAllValidIsFalseOrRevokedIsFalseByUser_Dni(String username);

    Optional<Token> findByToken(String jwtToken);
}
