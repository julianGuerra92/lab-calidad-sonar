package com.project.citasalud.codeMFA;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findByUserEmail(String userEmail);

    void deleteByUserEmail(String userEmail);
}
