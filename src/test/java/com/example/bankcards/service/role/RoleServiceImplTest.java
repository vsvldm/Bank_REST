package com.example.bankcards.service.role;

import com.example.bankcards.dto.role.RoleRequest;
import com.example.bankcards.dto.role.RoleResponse;
import com.example.bankcards.entity.role.Role;
import com.example.bankcards.exception.exception.BadRequestException;
import com.example.bankcards.exception.exception.CreationException;
import com.example.bankcards.exception.exception.NotFoundException;
import com.example.bankcards.mapper.RoleMapper;
import com.example.bankcards.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.example.bankcards.util.GlobalConstants.DEFAULT_ROLE;
import static com.example.bankcards.util.GlobalConstants.ROLE_PREFIX;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    private static final Long ROLE_ID = 1L;
    private static final String ROLE_NAME = "ADMIN";
    private static final String FORMATTED_ROLE_NAME = ROLE_PREFIX + "ADMIN";

    // Тесты для метода create()
    @Test
    void createRole_Success() {
        RoleRequest request = new RoleRequest("admin");
        Role role = new Role(ROLE_ID, FORMATTED_ROLE_NAME);
        RoleResponse response = new RoleResponse(ROLE_ID, FORMATTED_ROLE_NAME);

        when(roleRepository.existsByName(FORMATTED_ROLE_NAME)).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        when(roleMapper.toRoleResponse(role)).thenReturn(response);

        RoleResponse result = roleService.create(request);

        assertNotNull(result);
        assertEquals(ROLE_ID, result.getId());
        assertEquals(FORMATTED_ROLE_NAME, result.getName());
        verify(roleRepository).existsByName(FORMATTED_ROLE_NAME);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void createRole_AlreadyExists_ThrowsException() {
        RoleRequest request = new RoleRequest("admin");

        when(roleRepository.existsByName(FORMATTED_ROLE_NAME)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> roleService.create(request));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void createRole_SaveFails_ThrowsCreationException() {
        RoleRequest request = new RoleRequest("admin");

        when(roleRepository.existsByName(FORMATTED_ROLE_NAME)).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenThrow(new RuntimeException("DB error"));

        assertThrows(CreationException.class, () -> roleService.create(request));
    }

    // Тесты для метода update()
    @Test
    void updateRole_Success() {
        RoleRequest updateRequest = new RoleRequest("editor");
        String newName = ROLE_PREFIX + "EDITOR";
        Role existingRole = new Role(ROLE_ID, ROLE_NAME);
        Role updatedRole = new Role(ROLE_ID, newName);
        RoleResponse response = new RoleResponse(ROLE_ID, newName);

        when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.of(existingRole));
        when(roleRepository.existsByName(newName)).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(updatedRole);
        when(roleMapper.toRoleResponse(updatedRole)).thenReturn(response);

        RoleResponse result = roleService.update(ROLE_ID, updateRequest);

        assertEquals(newName, result.getName());
        verify(roleRepository).save(existingRole);
    }

    @Test
    void updateRole_DefaultRole_ThrowsException() {
        RoleRequest updateRequest = new RoleRequest("user");
        Role defaultRole = new Role(ROLE_ID, DEFAULT_ROLE);

        when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.of(defaultRole));

        assertThrows(BadRequestException.class, () -> roleService.update(ROLE_ID, updateRequest));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void updateRole_NameExists_ThrowsException() {
        RoleRequest updateRequest = new RoleRequest("editor");
        Role existingRole = new Role(ROLE_ID, ROLE_NAME);
        String newName = ROLE_PREFIX + "EDITOR";

        when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.of(existingRole));
        when(roleRepository.existsByName(newName)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> roleService.update(ROLE_ID, updateRequest));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void updateRole_NotFound_ThrowsException() {
        RoleRequest updateRequest = new RoleRequest("editor");

        when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> roleService.update(ROLE_ID, updateRequest));
    }

    // Тесты для метода getAll()
    @Test
    void getAllRoles_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Role role = new Role(ROLE_ID, ROLE_NAME);
        RoleResponse response = new RoleResponse(ROLE_ID, ROLE_NAME);
        Page<Role> page = new PageImpl<>(Collections.singletonList(role));

        when(roleRepository.findAll(pageable)).thenReturn(page);
        when(roleMapper.toRoleResponse(role)).thenReturn(response);

        List<RoleResponse> result = roleService.getAll(pageable);

        assertEquals(1, result.size());
        assertEquals(ROLE_ID, result.get(0).getId());
        verify(roleRepository).findAll(pageable);
    }

    // Тесты для метода getByName()
    @Test
    void getRoleByName_Success() {
        Role role = new Role(ROLE_ID, ROLE_NAME);
        RoleResponse response = new RoleResponse(ROLE_ID, ROLE_NAME);

        when(roleRepository.findByName(ROLE_NAME)).thenReturn(Optional.of(role));
        when(roleMapper.toRoleResponse(role)).thenReturn(response);

        RoleResponse result = roleService.getByName(ROLE_NAME);

        assertEquals(ROLE_NAME, result.getName());
    }

    @Test
    void getRoleByName_NotFound_ThrowsException() {
        when(roleRepository.findByName(ROLE_NAME)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> roleService.getByName(ROLE_NAME));
    }

    // Тесты для метода delete()
    @Test
    void deleteRole_Success() {
        Role role = new Role(ROLE_ID, ROLE_NAME);

        when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.of(role));
        doNothing().when(roleRepository).deleteById(ROLE_ID);

        roleService.delete(ROLE_ID);

        verify(roleRepository).deleteById(ROLE_ID);
    }

    @Test
    void deleteRole_DefaultRole_ThrowsException() {
        Role defaultRole = new Role(ROLE_ID, DEFAULT_ROLE);

        when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.of(defaultRole));

        assertThrows(BadRequestException.class, () -> roleService.delete(ROLE_ID));
        verify(roleRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteRole_NotFound_ThrowsException() {
        when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> roleService.delete(ROLE_ID));
    }
}