package com.project.citasalud.mfa;

import com.project.citasalud.codeMFA.VerificationCode;
import com.project.citasalud.codeMFA.VerificationCodeRepository;
import com.project.citasalud.codeMFA.VerificationCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailVerificationTest {

    @Mock
    private EmailService emailService;

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @Mock
    private VerificationCodeService verificationCodeService;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(emailVerificationService, "expirationMinutes", 5L);
    }

    @Test
    public void shouldSendVerificationCodeAndSaveIt() {
        String email = "test@example.com";
        String code = "123456";

        when(verificationCodeService.generateVerificationCode()).thenReturn(code);

        emailVerificationService.sendVerificationCode(email);

        // Verifica que se guardó el código con los campos correctos
        ArgumentCaptor<VerificationCode> captor = ArgumentCaptor.forClass(VerificationCode.class);
        verify(verificationCodeRepository).save(captor.capture());

        VerificationCode savedCode = captor.getValue();
        assertEquals(email, savedCode.getUserEmail());
        assertEquals(code, savedCode.getCode());
        assertTrue(savedCode.getExpiresAt().isAfter(Instant.now().minusSeconds(1)));

        // Verifica que se haya enviado el correo
        verify(emailService).sendEmail(eq(email), anyString(), contains(code));
    }
}
