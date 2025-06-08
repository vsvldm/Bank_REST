package com.example.bankcards.service.user;

import com.example.bankcards.dto.user.ChangeRoleRequest;
import com.example.bankcards.dto.user.UserRegistrationRequest;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.dto.user.UserUpdateRequest;
import com.example.bankcards.entity.role.Role;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.exception.BadRequestException;
import com.example.bankcards.exception.exception.CreationException;
import com.example.bankcards.exception.exception.NotFoundException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

import static com.example.bankcards.util.GlobalConstants.DEFAULT_ROLE;
import static com.example.bankcards.util.GlobalConstants.ROLE_PREFIX;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Executing loadUserByUsername for username: {}", username);

        User user = findUserByUsername(username);

        log.info("User {} found, creating UserDetails", username);

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .toList()
        );
    }

    @Override
    @Transactional
    public UserResponse create(UserRegistrationRequest userRegistrationRequest) {
        log.info("Starting create method with username: {}, email: {}",
                userRegistrationRequest.getUsername(),
                userRegistrationRequest.getEmail());

        if (userRepository.existsByUsername(userRegistrationRequest.getUsername())) {
            log.error("Username {} is already taken", userRegistrationRequest.getUsername());
            throw new BadRequestException("Username is already in use");
        }

        User user = userMapper.toUser(userRegistrationRequest);
        log.debug("Mapped UserRegistrationRequest to User entity");

        Role role = findRoleByName(DEFAULT_ROLE);
        log.info("Adding default role: {}", DEFAULT_ROLE);

        user.getRoles().add(role);

        try {
            user = userRepository.save(user);
            log.info("User saved successfully with ID: {}", user.getId());

            UserResponse userResponse = userMapper.toUserResponse(user);
            log.info("User {} created successfully", user.getUsername());

            return userResponse;
        } catch (Exception e) {
            log.error("Failed to create user: {} ", userRegistrationRequest.getUsername());
            throw new CreationException(String.format("Failed to create user: %s", userRegistrationRequest.getUsername()));
        }
    }

    @Override
    @Transactional
    public UserResponse changeUserRole(Principal principal, ChangeRoleRequest changeRoleRequest) {
        log.info("Executing changeUserRole for initiator: {}, target: {}, operation: {}, role: {}",
                principal.getName(),
                changeRoleRequest.getUsername(),
                changeRoleRequest.getOperationType(),
                changeRoleRequest.getRole());

        User sourceUser = findUserByUsername(principal.getName());
        User targetUser = findUserByUsername(changeRoleRequest.getUsername());
        String roleName = ROLE_PREFIX + changeRoleRequest.getRole().toUpperCase();

        log.debug("Source user ID: {}, Target user ID: {}", sourceUser.getId(), targetUser.getId());

        if (sourceUser.getId().equals(targetUser.getId())) {
            log.error("User {} attempted to modify own roles", principal.getName());
            throw new BadRequestException(String.format("User cannot %s the role himself.", changeRoleRequest.getOperationType()));
        }

        switch (changeRoleRequest.getOperationType()) {
            case ADD -> {
                Role role = findRoleByName(roleName);
                targetUser.getRoles().add(role);
                log.info("Added role {} to user {}", roleName, targetUser.getUsername());
            }
            case REMOVE -> {
                if (roleName.equals(DEFAULT_ROLE)) {
                    log.error("Attempted to remove default role from user {}", targetUser.getUsername());
                    throw new BadRequestException("User cannot remove default role.");
                }
                Role role = findRoleByName(roleName);
                targetUser.getRoles().remove(role);
                log.info("Removed role {} from user {}", roleName, targetUser.getUsername());
            }
            default -> {
                log.error("Invalid operation type: {}", changeRoleRequest.getOperationType());
                throw new BadRequestException(String.format("Operation type %s not exist", changeRoleRequest.getOperationType()));
            }
        }

        UserResponse userResponse = userMapper.toUserResponse(targetUser);
        log.info("Roles updated for user {}", targetUser.getUsername());

        return userResponse;
    }

    @Override
    @Transactional
    public UserResponse update(Principal principal, UserUpdateRequest updates) {
        log.info("Starting update for user: {}", principal.getName());

        User user = findUserByUsername(principal.getName());

        if (updates.getUsername() != null) {
            log.debug("Updating username to: {}", updates.getUsername());
            user.setUsername(updates.getUsername());
        }

        if (updates.getPassword() != null) {
            log.debug("Updating password");
            user.setPassword(passwordEncoder.encode(updates.getPassword()));
        }

        if (updates.getEmail() != null) {
            log.debug("Updating email to: {}", updates.getEmail());
            user.setEmail(updates.getEmail());
        }

        userRepository.save(user);
        log.info("User {} updated successfully", user.getUsername());

        return userMapper.toUserResponse(user);
    }

    @Override
    public List<UserResponse> getAll(Pageable pageable) {
        log.info("Executing getAll with page: {}, size: {}",
                pageable.getPageNumber(),
                pageable.getPageSize());

        List<UserResponse> userResponses = userRepository.findAll(pageable)
                .getContent()
                .stream()
                .map(userMapper::toUserResponse)
                .toList();

        log.info("Retrieved {} users", userResponses.size());

        return userResponses;
    }

    @Override
    public UserResponse getById(Long userId) {
        log.info("Executing getById for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User with ID {} not found", userId);
                    return new NotFoundException(String.format("User with id=%d not found", userId));
                });

        log.debug("Mapping user entity to response DTO");
        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse getByUsername(String username) {
        log.info("Executing getByUsername for username: {}", username);

        User user = findUserByUsername(username);
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public void deleteById(Long userId, Principal principal) {
        log.info("Starting deleteById for user ID: {}, initiator: {}", userId, principal.getName());

        if (!userRepository.existsById(userId)) {
            log.error("User with ID {} not found for deletion", userId);
            throw new NotFoundException(String.format("User with id=%d not found.", userId));
        }

        User initiator = findUserByUsername(principal.getName());

        if (initiator.getId().equals(userId)) {
            log.error("User {} attempted self-deletion", principal.getName());
            throw new BadRequestException("User cannot delete himself.");
        }

        userRepository.deleteById(userId);
        log.info("User with ID {} deleted successfully", userId);
    }

    private User findUserByUsername(String username) {
        log.debug("Searching for user by username: {}", username);

        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User {} not found", username);
                    return new UsernameNotFoundException(String.format("User %s not found", username));
                });
    }

    private Role findRoleByName(String name) {
        log.debug("Searching for role: {}", name);

        return roleRepository.findByName(name)
                .orElseThrow(() -> {
                    log.error("Role {} not found", name);
                    return new NotFoundException(String.format("Role %s not found", name));
                });
    }
}