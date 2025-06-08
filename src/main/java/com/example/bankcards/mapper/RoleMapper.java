package com.example.bankcards.mapper;

import com.example.bankcards.dto.role.RoleResponse;
import com.example.bankcards.entity.role.Role;
import org.springframework.stereotype.Component;


@Component
public class RoleMapper {
    public RoleResponse toRoleResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    }
}
