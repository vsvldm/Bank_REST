package com.example.bankcards.dto.role;

import jakarta.validation.constraints.NotBlank;
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
public class RoleRequest {
    @NotBlank(message = "Role name is required")
    @Size(min = 3, max = 20, message = "Role name must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z_]+$", message = "Only letters and underscores allowed")
    private String name;

}
