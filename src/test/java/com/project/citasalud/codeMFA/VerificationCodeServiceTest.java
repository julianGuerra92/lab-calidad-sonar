package com.project.citasalud.codeMFA;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VerificationCodeServiceTest {
    @InjectMocks
    private VerificationCodeService verificationCodeService;

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test // Validación del formato de código generado
    void generateVerificationCode_ShouldReturnSixDigitString() {
        String code = verificationCodeService.generateVerificationCode();
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"));
    }

    @Test // Código correcto y dentro del tiempo
    void verifyCode_ShouldReturnTrue_WhenCodeIsValidAndNotExpired() {
        String email = "test@example.com";
        String code = "123456";

        VerificationCode verificationCode = VerificationCode.builder()
                .userEmail(email)
                .code(code)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        when(verificationCodeRepository.findByUserEmail(email)).thenReturn(Optional.of(verificationCode));

        boolean result = verificationCodeService.verifyCode(email, code);

        assertTrue(result);
        verify(verificationCodeRepository).deleteByUserEmail(email);
    }

    @Test // Código expirado
    void verifyCode_ShouldReturnFalse_WhenCodeIsExpired() {
        String email = "test@example.com";
        String code = "123456";

        VerificationCode verificationCode = VerificationCode.builder()
                .userEmail(email)
                .code(code)
                .createdAt(Instant.now().minusSeconds(600))
                .expiresAt(Instant.now().minusSeconds(1))
                .build();

        when(verificationCodeRepository.findByUserEmail(email)).thenReturn(Optional.of(verificationCode));

        boolean result = verificationCodeService.verifyCode(email, code);

        assertFalse(result);
        verify(verificationCodeRepository, never()).deleteByUserEmail(anyString());
    }

    @Test // Código incorrecto
    void verifyCode_ShouldReturnFalse_WhenCodeIsInvalid() {
        String email = "test@example.com";
        String correctCode = "123456";
        String userInput = "999999";

        VerificationCode verificationCode = VerificationCode.builder()
                .userEmail(email)
                .code(correctCode)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        when(verificationCodeRepository.findByUserEmail(email)).thenReturn(Optional.of(verificationCode));

        boolean result = verificationCodeService.verifyCode(email, userInput);

        assertFalse(result);
        verify(verificationCodeRepository, never()).deleteByUserEmail(anyString());
    }

    @Test // No se encuentra código
    void verifyCode_ShouldReturnFalse_WhenNoCodeFound() {
        String email = "test@example.com";

        when(verificationCodeRepository.findByUserEmail(email)).thenReturn(Optional.empty());

        boolean result = verificationCodeService.verifyCode(email, "123456");

        assertFalse(result);
        verify(verificationCodeRepository, never()).deleteByUserEmail(anyString());
    }
}
