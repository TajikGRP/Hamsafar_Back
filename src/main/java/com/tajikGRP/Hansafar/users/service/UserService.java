package com.tajikGRP.Hansafar.users.service;

import com.tajikGRP.Hansafar.users.domain.UserRole;
import com.tajikGRP.Hansafar.users.domain.Users;
import com.tajikGRP.Hansafar.users.dto.UserCreateRequest;
import com.tajikGRP.Hansafar.users.dto.UserResponse;
import com.tajikGRP.Hansafar.users.dto.UserUpdateRequest;
import com.tajikGRP.Hansafar.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UsersRepository usersRepository;

    @Transactional
    public UserResponse createUser(
            UserCreateRequest request
    ) {
        if (usersRepository.existsByEmail(request.email())) {
            throw new IllegalStateException(
                    "Email is already in use."
            );
        }

        Users user = Users.builder()
                .email(request.email())
                .name(request.name())
                .role(UserRole.PASSENGER)
                .build();

        Users savedUser = usersRepository.save(user);

        return toResponse(savedUser);
    }

    public List<UserResponse> getUsers() {
        return usersRepository
                .findAllByDeletedAtIsNullOrderByIdAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse getUser(Long userId) {
        Users user = findActiveUser(userId);

        return toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(
            Long userId,
            UserUpdateRequest request
    ) {
        Users user = findActiveUser(userId);

        boolean duplicatedEmail =
                usersRepository.existsByEmailAndIdNot(
                        request.email(),
                        userId
                );

        if (duplicatedEmail) {
            throw new IllegalStateException(
                    "Email is already in use."
            );
        }

        user.update(
                request.email(),
                request.name()
        );

        return toResponse(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        Users user = findActiveUser(userId);

        if (user.getRole() != UserRole.PASSENGER) {
            throw new IllegalStateException(
                    "Driver accounts must be deleted through the driver API."
            );
        }

        user.softDelete();
    }

    private Users findActiveUser(Long userId) {
        return usersRepository
                .findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found."
                        )
                );
    }

    private UserResponse toResponse(Users user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}