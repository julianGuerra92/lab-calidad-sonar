package com.project.citasalud.mfa;

import com.project.citasalud.codeMFA.VerificationCode;
import com.project.citasalud.codeMFA.VerificationCodeRepository;
import com.project.citasalud.codeMFA.VerificationCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmailVerificationTest {

    private EmailService emailService;
    private VerificationCodeRepository codeRepository;
    private VerificationCodeService codeService;
    private EmailVerificationService verificationService;

    @BeforeEach
    public void setUp() throws Exception {
        emailService = mock(EmailService.class);
        codeRepository = mock(VerificationCodeRepository.class);
        codeService = mock(VerificationCodeService.class);

        verificationService = new EmailVerificationService();

        setPrivateField(verificationService, "emailService", emailService);
        setPrivateField(verificationService, "verificationCodeRepository", codeRepository);
        setPrivateField(verificationService, "verificationCodeService", codeService);
        setPrivateField(verificationService, "expirationMinutes", 10L);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    public void shouldSendVerificationCodeAndSaveIt() {
        String email = "test@example.com";
        String code = "123456";
        when(codeService.generateVerificationCode()).thenReturn(code);

        verificationService.sendVerificationCode(email);

        // Verifica que se guardó el código con los campos correctos
        ArgumentCaptor<VerificationCode> captor = ArgumentCaptor.forClass(VerificationCode.class);
        verify(codeRepository).save(captor.capture());

        VerificationCode savedCode = captor.getValue();
        assertEquals(email, savedCode.getUserEmail());
        assertEquals(code, savedCode.getCode());
        assertTrue(savedCode.getExpiresAt().isAfter(Instant.now()));

        // Verifica que se haya enviado el correo
        verify(emailService).sendEmail(eq(email), anyString(), contains(code));
    }
}
