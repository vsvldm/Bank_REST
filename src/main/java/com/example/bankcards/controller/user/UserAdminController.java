package com.example.bankcards.controller.user;

import com.example.bankcards.dto.user.ChangeRoleRequest;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "UserAdminController", description = "Управление пользователями (админ)")
@SecurityRequirement(name = "JWT")
public class UserAdminController {
    private final UserService userService;

    @Operation(
            summary = "Изменить роль пользователя",
            description = "Добавление или удаление роли у пользователя. Требует роли ADMIN"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Роль изменена"),
            @ApiResponse(responseCode = "404", description = "Пользователь или роль не найдены"),
            @ApiResponse(responseCode = "400", description = "Невозможно изменить системные роли")
    })
    @PatchMapping
    public UserResponse changeRole(
            @Parameter(hidden = true) Principal principal,
            @RequestBody @Valid ChangeRoleRequest changeRoleRequest) {
        return userService.changeUserRole(principal, changeRoleRequest);
    }

    @Operation(
            summary = "Получить всех пользователей",
            description = "Список пользователей с пагинацией. Требует роли ADMIN",
            parameters = {
                    @Parameter(name = "page", description = "Номер страницы", example = "0", in = ParameterIn.QUERY),
                    @Parameter(name = "size", description = "Размер страницы", example = "10", in = ParameterIn.QUERY),
                    @Parameter(name = "sort", description = "Поле сортировки", example = "username,asc", in = ParameterIn.QUERY)
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешное получение списка"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @GetMapping
    public List<UserResponse> getAll(
            @ParameterObject @PageableDefault(
                    sort = "username",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {
        return userService.getAll(pageable);
    }

    @Operation(
            summary = "Получить пользователя по ID",
            description = "Получение детальной информации о пользователе. Требует роли ADMIN",
            parameters = @Parameter(name = "userId", description = "ID пользователя", example = "1", in = ParameterIn.PATH)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/{userId}")
    public UserResponse getById(@PathVariable Long userId) {
        return userService.getById(userId);
    }

    @Operation(
            summary = "Удалить пользователя",
            description = "Удаление пользователя по ID. Требует роли ADMIN",
            parameters = @Parameter(name = "userId", description = "ID пользователя", example = "1", in = ParameterIn.PATH)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Пользователь удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "400", description = "Нельзя удалить текущего пользователя")
    })
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(
            @PathVariable Long userId,
            @Parameter(hidden = true) Principal principal) {
        userService.deleteById(userId, principal);
    }
}
