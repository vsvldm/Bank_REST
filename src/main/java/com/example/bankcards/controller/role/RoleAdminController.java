package com.example.bankcards.controller.role;

import com.example.bankcards.dto.role.RoleRequest;
import com.example.bankcards.dto.role.RoleResponse;
import com.example.bankcards.service.role.RoleService;
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

import java.util.List;

@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
@Tag(name = "RoleAdminController", description = "Управление ролями")
@SecurityRequirement(name = "JWT")
public class RoleAdminController {
    private final RoleService roleService;

    @Operation(
            summary = "Создать роль",
            description = "Создание новой роли в системе. Требует роли ADMIN"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Роль создана"),
            @ApiResponse(responseCode = "400", description = "Неверные параметры"),
            @ApiResponse(responseCode = "409", description = "Роль уже существует")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse create(@Valid @RequestBody RoleRequest roleRequest) {
        return roleService.create(roleRequest);
    }

    @Operation(
            summary = "Обновить роль",
            description = "Изменение данных роли. Требует роли ADMIN",
            parameters = @Parameter(name = "roleId", description = "ID роли", example = "1", in = ParameterIn.PATH)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Роль обновлена"),
            @ApiResponse(responseCode = "404", description = "Роль не найдена")
    })
    @PutMapping("/{roleId}")
    public RoleResponse update(
            @PathVariable Long roleId,
            @Valid @RequestBody RoleRequest updateRequest) {
        return roleService.update(roleId, updateRequest);
    }

    @Operation(
            summary = "Получить все роли",
            description = "Список всех ролей с пагинацией. Требует роли ADMIN",
            parameters = {
                    @Parameter(name = "page", description = "Номер страницы", example = "0", in = ParameterIn.QUERY),
                    @Parameter(name = "size", description = "Размер страницы", example = "10", in = ParameterIn.QUERY),
                    @Parameter(name = "sort", description = "Поле сортировки", example = "name,asc", in = ParameterIn.QUERY)
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешное получение списка"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @GetMapping
    public List<RoleResponse> getAll(
            @ParameterObject @PageableDefault(
                    sort = "name",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {
        return roleService.getAll(pageable);
    }

    @Operation(
            summary = "Получить роль по имени",
            description = "Поиск роли по названию. Требует роли ADMIN",
            parameters = @Parameter(name = "roleName", description = "Название роли", example = "USER", in = ParameterIn.PATH)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Роль найдена"),
            @ApiResponse(responseCode = "404", description = "Роль не найдена")
    })
    @GetMapping("/{roleName}")
    public RoleResponse getByName(@PathVariable String roleName) {
        return roleService.getByName(roleName);
    }

    @Operation(
            summary = "Удалить роль",
            description = "Удаление роли по ID. Требует роли ADMIN",
            parameters = @Parameter(name = "roleId", description = "ID роли", example = "1", in = ParameterIn.PATH)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Роль удалена"),
            @ApiResponse(responseCode = "404", description = "Роль не найдена"),
            @ApiResponse(responseCode = "400", description = "Нельзя удалить системную роль")
    })
    @DeleteMapping("/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long roleId) {
        roleService.delete(roleId);
    }
}
