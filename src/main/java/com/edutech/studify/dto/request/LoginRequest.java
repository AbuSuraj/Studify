package com.edutech.studify.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    /**
     * If true, every other active session for this user is logged out as
     * part of this login. Defaults to false (primitive boolean default) -
     * i.e. "keep my other sessions logged in" is the safe default when the
     * client doesn't explicitly ask the user.
     */
    private boolean terminateOtherSessions;
}