package com.backlogr.core.user;

import com.backlogr.domain.user.User;
import com.backlogr.dto.user.CreateUserRequest;
import com.backlogr.dto.user.UserResponse;
import com.backlogr.repository.user.UserRepository;
import com.backlogr.shared.Result;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

@ApplicationScoped
public class UserCore {

    @Inject
    UserRepository userRepository;

    @Transactional
    public Result<UserResponse> createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            return Result.conflict("A user with that email already exists.");
        }

        User user = new User();
        user.email = request.email();
        user.passwordHash = BCrypt.hashpw(request.password(), BCrypt.gensalt());
        userRepository.persist(user);

        return Result.created(toResponse(user));
    }

    public Result<UserResponse> getMe(UUID userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return Result.notFound("User not found.");
        }
        return Result.ok(toResponse(user));
    }

    private static UserResponse toResponse(User user) {
        return new UserResponse(user.id, user.email, user.createdAt, user.lastModifiedAt);
    }
}
