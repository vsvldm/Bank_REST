package com.example.bankcards.service.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.bankcards.dto.user.ChangeRoleRequest;
import com.example.bankcards.dto.user.UserRegistrationRequest;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.dto.user.UserUpdateRequest;
import com.example.bankcards.entity.role.Role;
import com.example.bankcards.entity.user.ChangeRoleType;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.exception.BadRequestException;
import com.example.bankcards.exception.exception.NotFoundException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import java.security.Principal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserServiceImpl userService;

    @Test
    void create_ValidRequest_ReturnsUserResponse() {
        UserRegistrationRequest request = new UserRegistrationRequest("user", "password", "password", "user@mail.com");
        User user = User.builder()
                .username("user")
                .password("password")
                .email("user@mail.com")
                .roles(new HashSet<>())
                .build();

        Role role = new Role(1L, "ROLE_USER");
        User savedUser = User.builder()
                .id(1L)
                .username("user")
                .password("encoded")
                .email("user@mail.com")
                .roles(Set.of(role))
                .build();

        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userMapper.toUser(request)).thenReturn(user);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toUserResponse(savedUser)).thenReturn(new UserResponse(1L, "user", "user@mail.com"));

        UserResponse response = userService.create(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("user", response.getUsername());
        verify(userRepository).save(user);
        assertTrue(user.getRoles().contains(role));
    }


    @Test
    void create_DuplicateUsername_ThrowsException() {
        UserRegistrationRequest request = new UserRegistrationRequest("user", "pass", "pass", "mail@mail.com");
        when(userRepository.existsByUsername("user")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.create(request));
    }


    @Test
    void changeUserRole_AddRole_Success() {
        Principal principal = () -> "admin";
        ChangeRoleRequest request = new ChangeRoleRequest("targetUser", "ADMIN", ChangeRoleType.ADD);

        User admin = User.builder()
                .id(1L)
                .username("admin")
                .roles(new HashSet<>())
                .build();

        User targetUser = User.builder()
                .id(2L)
                .username("targetUser")
                .roles(new HashSet<>())
                .build();

        Role newRole = new Role(2L, "ROLE_ADMIN");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findByUsername("targetUser")).thenReturn(Optional.of(targetUser));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(newRole));
        when(userMapper.toUserResponse(targetUser)).thenReturn(new UserResponse(2L, "targetUser", "target@mail.com"));

        UserResponse response = userService.changeUserRole(principal, request);

        assertNotNull(response);
        assertTrue(targetUser.getRoles().contains(newRole));
        verify(userRepository, times(1)).findByUsername("admin");
        verify(userRepository, times(1)).findByUsername("targetUser");
    }

    @Test
    void changeUserRole_SelfModification_ThrowsException() {
        Principal principal = () -> "user";
        ChangeRoleRequest request = new ChangeRoleRequest("user", "ADMIN", ChangeRoleType.ADD);

        User user = User.builder()
                .id(1L)
                .username("user")
                .roles(new HashSet<>())
                .build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userService.changeUserRole(principal, request));

        assertEquals("User cannot ADD the role himself.", exception.getMessage());
    }

    @Test
    void changeUserRole_RemoveDefaultRole_ThrowsException() {
        Principal principal = () -> "admin";
        ChangeRoleRequest request = new ChangeRoleRequest("target", "USER", ChangeRoleType.REMOVE);

        User admin = User.builder().id(1L).build();
        User target = User.builder().id(2L).roles(new HashSet<>()).build();
        Role defaultRole = new Role(1L, "ROLE_USER");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findByUsername("target")).thenReturn(Optional.of(target));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userService.changeUserRole(principal, request));

        assertEquals("User cannot remove default role.", exception.getMessage());
    }

    @Test
    void update_PasswordUpdate_EncodesPassword() {
        Principal principal = () -> "user";
        UserUpdateRequest updates = UserUpdateRequest.builder()
                .password("newPass")
                .confirmPassword("newPass")
                .build();

        User user = User.builder()
                .id(1L)
                .username("user")
                .password("oldEncoded")
                .email("user@mail.com")
                .roles(new HashSet<>())
                .build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass")).thenReturn("newEncoded");
        when(userMapper.toUserResponse(user)).thenReturn(new UserResponse(1L, "user", "user@mail.com"));

        userService.update(principal, updates);

        assertEquals("newEncoded", user.getPassword());
        verify(passwordEncoder).encode("newPass");
    }

    @Test
    void getById_NonExistingUser_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getById(999L));

        assertEquals("User with id=999 not found", exception.getMessage());
    }

    @Test
    void loadUserByUsername_InvalidUser_ThrowsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("unknown"));

        assertEquals("User unknown not found", exception.getMessage());
    }

    @Test
    void deleteById_SelfDeletion_ThrowsException() {
        Principal principal = () -> "user";
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(
                User.builder().id(1L).build()
        ));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userService.deleteById(userId, principal));

        assertEquals("User cannot delete himself.", exception.getMessage());
    }

    @Test
    void deleteById_NonExistingUser_ThrowsException() {
        Long userId = 999L;
        Principal principal = () -> "admin";

        when(userRepository.existsById(userId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.deleteById(userId, principal));

        assertEquals("User with id=999 not found.", exception.getMessage());
    }
}