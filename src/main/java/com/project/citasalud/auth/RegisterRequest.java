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
public class RegisterRequest {
    @NotBlank(message = "Cédula is required")
    @Pattern(regexp = "^[0-9]{9,10}$",
    message = "Cédula must contain only digits")
    String dni;
    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+])[A-Za-z\\d][A-Za-z\\d!@#$%^&*()_+]{7,19}$",
    message = "Password must contain at least one letter, one digit and one special character")
    String password;
    @NotBlank(message = "First name is required")
    String firstName;
    @NotBlank(message = "Last name is required")
    String lastName;
    @NotBlank(message = "Department is required")
    String department;
    @NotBlank(message = "City is required")
    String city;
    @NotBlank(message = "Address is required")
    String address;
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    String email;
    @NotBlank(message = "Number phone is required")
    @Pattern(regexp = "^[0-9]{10}$",
    message = "Number phone must contain only digits")
    String numberPhone;
}
