package com.example.bankcards.dto.user;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {
    @Size(min = 4, max = 50, message = "Username must be 4-50 characters")
    private String username;

    @Pattern(regexp = "\\S+", message = "Password must not contain spaces")
    @Size(min = 6, max = 100, message = "Password must be 6-100 characters")
    private String password;

    @Pattern(regexp = "\\S+", message = "Confirm password must not contain spaces")
    @Size(min = 6, max = 100, message = "Confirm password must be 6-100 characters")
    private String confirmPassword;

    @Email(message = "Email format is invalid")
    private String email;

    @AssertTrue(message = "Both password fields must be provided or omitted together")
    public boolean isPasswordConsistent() {
        return (password == null && confirmPassword == null) ||
                (password != null && confirmPassword != null);
    }

    @AssertTrue(message = "Passwords must match")
    public boolean isPasswordsMatch() {
        return password == null ||
                confirmPassword == null ||
                password.equals(confirmPassword);
    }
}