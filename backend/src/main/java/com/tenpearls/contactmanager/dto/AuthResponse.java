package com.tenpearls.contactmanager.dto;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "token")
public class AuthResponse {
    @JsonIgnore
    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long id;
    private String email;
    private String phone;
}
