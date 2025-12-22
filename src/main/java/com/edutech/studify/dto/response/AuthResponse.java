package com.edutech.studify.dto.response;

import com.edutech.studify.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private Role role;
    private long expiresIn; // in milliseconds
}