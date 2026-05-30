package com.backlogr.core.auth;

import com.backlogr.domain.entities.user.User;
import com.backlogr.domain.dto.auth.LoginRequest;
import com.backlogr.domain.dto.auth.LoginResponse;
import com.backlogr.domain.dto.auth.RefreshRequest;
import com.backlogr.repository.user.UserRepository;
import com.backlogr.common.Result;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.mindrot.jbcrypt.BCrypt;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class AuthCore {

    private static final Duration JWT_TTL           = Duration.ofHours(1);
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);

    @Inject
    UserRepository userRepository;

    @Transactional
    public Result<LoginResponse> login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);

        if (user == null || !BCrypt.checkpw(request.password(), user.passwordHash)) {
            return Result.unauthorized("Invalid email or password.");
        }

        return Result.ok(issueTokens(user));
    }

    @Transactional
    public Result<LoginResponse> refresh(RefreshRequest request) {
        User user = userRepository.findByRefreshToken(request.refreshToken()).orElse(null);

        if (user == null || user.refreshTokenExpiry == null || Instant.now().isAfter(user.refreshTokenExpiry)) {
            return Result.unauthorized("Invalid or expired refresh token.");
        }

        return Result.ok(issueTokens(user));
    }

    @Transactional
    public Result<Void> logout(UUID userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return Result.notFound("User not found.");
        }
        user.refreshToken = null;
        user.refreshTokenExpiry = null;
        return Result.ok(null);
    }

    private LoginResponse issueTokens(User user) {
        String jwt = Jwt.issuer("backlogr")
                .subject(user.id.toString())
                .claim("email", user.email)
                .expiresIn(JWT_TTL)
                .sign();

        user.refreshToken = UUID.randomUUID().toString();
        user.refreshTokenExpiry = Instant.now().plus(REFRESH_TOKEN_TTL);

        return new LoginResponse(jwt, user.refreshToken);
    }
}
