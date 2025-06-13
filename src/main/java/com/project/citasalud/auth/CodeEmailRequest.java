package com.project.citasalud.auth;

import jakarta.validation.constraints.Email;
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
public class CodeEmailRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    String email;
    @NotBlank(message = "Code is required")
    @Pattern(regexp = "^[0-9]{6}$",
            message = "The code must contain 6 digits")
    String code;
}
