package com.example.bankcards.service.user;

import com.example.bankcards.dto.user.ChangeRoleRequest;
import com.example.bankcards.dto.user.UserRegistrationRequest;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.dto.user.UserUpdateRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.security.Principal;
import java.util.List;

public interface UserService extends UserDetailsService {
    UserResponse create(UserRegistrationRequest userRegistrationRequest);

    UserResponse changeUserRole(Principal principal, ChangeRoleRequest changeRoleRequest);

    UserResponse update(Principal principal, UserUpdateRequest userUpdateRequest);

    List<UserResponse> getAll(Pageable pageable);

    UserResponse getById(Long userId);

    UserResponse getByUsername(String username);

    void deleteById(Long userId, Principal principal);
}
