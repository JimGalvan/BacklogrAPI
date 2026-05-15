package com.backlogr.core.auth;

import com.backlogr.domain.user.User;
import com.backlogr.dto.auth.LoginRequest;
import com.backlogr.dto.auth.LoginResponse;
import com.backlogr.repository.user.UserRepository;
import com.backlogr.shared.Result;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;

import java.time.Duration;

@ApplicationScoped
public class AuthCore {

    @Inject
    UserRepository userRepository;

    public Result<LoginResponse> login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);

        if (user == null || !BCrypt.checkpw(request.password(), user.passwordHash)) {
            return Result.unauthorized("Invalid email or password.");
        }

        String token = Jwt.issuer("backlogr")
                .subject(user.id.toString())
                .claim("email", user.email)
                .expiresIn(Duration.ofHours(24))
                .sign();

        return Result.ok(new LoginResponse(token));
    }
}
