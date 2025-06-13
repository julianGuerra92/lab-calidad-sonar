package com.project.citasalud.mfa;

import com.project.citasalud.codeMFA.VerificationCode;
import com.project.citasalud.codeMFA.VerificationCodeRepository;
import com.project.citasalud.codeMFA.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class EmailVerificationService {

    @Autowired
    private EmailService emailService;
    @Autowired
    private VerificationCodeRepository verificationCodeRepository;
    @Autowired
    private VerificationCodeService verificationCodeService;
    @Value("${EXPIRATION_MINUTES}")
    private long expirationMinutes;

    public void sendVerificationCode(String email){
        String verificationCode = verificationCodeService.generateVerificationCode();

        VerificationCode code = VerificationCode.builder()
                        .userEmail(email)
                                .code(verificationCode)
                                        .createdAt(Instant.now())
                                                .expiresAt(Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES))
                                                        .build();
        verificationCodeRepository.save(code);

        emailService.sendEmail(email,
                "Cita Salud verification code",
                "Your verification code is: " + verificationCode + "\nPlease use the following code to complete your access");
    }

}
