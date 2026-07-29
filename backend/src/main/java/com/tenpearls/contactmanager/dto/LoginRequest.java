package com.tenpearls.contactmanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username (email or phone) is required")
    private String username;

    @NotBlank(message = "Password is required")
    @ToString.Exclude
    private String password;
}
