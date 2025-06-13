package com.project.citasalud.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Cédula is required")
    @Pattern(regexp = "^[0-9]{9,10}$",
            message = "Cédula must contain only digits")
    String dni;
    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+])[A-Za-z\\d][A-Za-z\\d!@#$%^&*()_+]{7,19}$",
            message = "Password must contain at least one letter, one digit and one special character")
    String password;
}
