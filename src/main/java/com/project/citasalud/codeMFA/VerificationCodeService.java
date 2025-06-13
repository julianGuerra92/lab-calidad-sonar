package com.project.citasalud.codeMFA;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;

@Service
public class VerificationCodeService {

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    public String generateVerificationCode(){
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    @Transactional
    public boolean verifyCode(String email, String userEnteredCode){
        Optional<VerificationCode> codeOptional = verificationCodeRepository.findByUserEmail(email);
        if (codeOptional.isEmpty())
            return false;

        VerificationCode code = codeOptional.get();

        if(!code.getCode().equals(userEnteredCode) || Instant.now().isAfter(code.getExpiresAt())){
            return false;
        }

        verificationCodeRepository.deleteByUserEmail(email);

        return true;
    }

}
