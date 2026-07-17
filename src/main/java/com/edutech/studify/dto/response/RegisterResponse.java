package com.edutech.studify.dto.response;

import com.edutech.studify.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {

    private Long userId;
    private String username;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
}