package com.example.bankcards.controller.user;

import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.dto.user.UserUpdateRequest;
import com.example.bankcards.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "UserPrivateController", description = "Профиль пользователя")
@SecurityRequirement(name = "JWT")
public class UserPrivateController {
    private final UserService userService;

    @Operation(
            summary = "Обновить профиль",
            description = "Изменение данных пользователя. Требует роли USER"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Профиль обновлен"),
            @ApiResponse(responseCode = "400", description = "Неверные данные"),
            @ApiResponse(responseCode = "409", description = "Логин/email уже занят")
    })
    @PatchMapping
    public UserResponse update(
            @Parameter(hidden = true) Principal principal,
            @RequestBody @Valid UserUpdateRequest userUpdateRequest) {
        return userService.update(principal, userUpdateRequest);
    }
}
