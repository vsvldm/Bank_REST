package com.example.bankcards.service.role;

import com.example.bankcards.dto.role.RoleRequest;
import com.example.bankcards.dto.role.RoleResponse;
import com.example.bankcards.entity.role.Role;
import com.example.bankcards.exception.exception.BadRequestException;
import com.example.bankcards.exception.exception.CreationException;
import com.example.bankcards.exception.exception.NotFoundException;
import com.example.bankcards.mapper.RoleMapper;
import com.example.bankcards.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.bankcards.util.GlobalConstants.DEFAULT_ROLE;
import static com.example.bankcards.util.GlobalConstants.ROLE_PREFIX;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional
    public RoleResponse create(RoleRequest roleRequest) {
        log.info("Creating new role from request: {}", roleRequest.getName());

        String roleName = formatRoleName(roleRequest.getName());
        log.debug("Formatted role name: {}", roleName);

        if (roleRepository.existsByName(roleName)) {
            log.warn("Role creation failed - role already exists: {}", roleName);
            throw new BadRequestException("Role name already exists");
        }

        Role role = new Role();
        role.setName(roleName);

        try {
            role = roleRepository.save(role);
            log.info("Successfully created role [ID: {}, Name: {}]", role.getId(), role.getName());
        } catch (Exception e) {
            log.error("Failed to create role: {}", e.getMessage());
            throw new CreationException(String.format("Failed to create role: %s", e.getMessage()));
        }

        return roleMapper.toRoleResponse(role);
    }

    @Override
    @Transactional
    public RoleResponse update(Long roleId, RoleRequest updateRequest) {
        log.info("Updating role ID: {} with data: {}", roleId, updateRequest.getName());

        Role role = getRoleById(roleId);
        log.debug("Found existing role: ID={}, Name={}", roleId, role.getName());

        if (role.getName().equals(DEFAULT_ROLE)) {
            log.error("Update rejected - attempt to modify default role: {}", DEFAULT_ROLE);
            throw new BadRequestException("Default role cannot be modified");
        }

        String newRoleName = formatRoleName(updateRequest.getName());
        log.debug("New formatted role name: {}", newRoleName);

        if (roleRepository.existsByName(newRoleName)) {
            log.warn("Role update failed - name already taken: {}", newRoleName);
            throw new BadRequestException("Role name already exists");
        }

        role.setName(newRoleName);
        Role updatedRole = roleRepository.save(role);
        log.info("Successfully updated role ID: {} with new name: {}", roleId, newRoleName);

        return roleMapper.toRoleResponse(updatedRole);
    }

    @Override
    public List<RoleResponse> getAll(Pageable pageable) {
        log.info("Fetching all roles with pagination - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        List<RoleResponse> roles = roleRepository.findAll(pageable)
                .get()
                .map(roleMapper::toRoleResponse)
                .toList();

        log.info("Retrieved {} roles", roles.size());
        return roles;
    }

    @Override
    public RoleResponse getByName(String roleName) {
        log.info("Fetching role by name: {}", roleName);

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    log.error("Role with name {} not found", roleName);
                    return new NotFoundException(String.format("Role with name %s not found", roleName));
                });

        log.debug("Found role with name: {}", roleName);

        return roleMapper.toRoleResponse(role);
    }

    @Override
    @Transactional
    public void delete(Long roleId) {
        log.info("Attempting to delete role with ID: {}", roleId);

        Role role = getRoleById(roleId);
        log.debug("Found role to delete: ID {}, Name {}", roleId, role.getName());

        if (role.getName().equals(DEFAULT_ROLE)) {
            log.error("Attempt to delete default role");
            throw new BadRequestException("The default role cannot be removed");
        }

        roleRepository.deleteById(roleId);
        log.info("Successfully deleted role with ID: {}", roleId);
    }

    private Role getRoleById(Long roleId) {
        log.debug("Looking for role by ID: {}", roleId);
        return roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    log.error("Role with ID {} not found", roleId);
                    return new NotFoundException(String.format("Role with id=%d not found.", roleId));
                });
    }

    private String formatRoleName(String name) {
        String formattedName = name.toUpperCase();
        if (!formattedName.startsWith(ROLE_PREFIX)) {
            formattedName = ROLE_PREFIX + formattedName;
        }
        log.trace("Formatted role name from '{}' to '{}'", name, formattedName);
        return formattedName;
    }
}