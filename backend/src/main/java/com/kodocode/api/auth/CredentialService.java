package com.kodocode.api.auth;

import com.kodocode.api.admin.AdminUser;
import com.kodocode.api.admin.AdminUserRepository;
import com.kodocode.api.config.ApplicationProperties;
import java.time.Clock;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class CredentialService {

    private static final String DUMMY_PASSWORD_HASH = "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG";

    private final AdminUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationProperties properties;
    private final Clock clock;

    @Autowired
    public CredentialService(
            AdminUserRepository repository,
            PasswordEncoder passwordEncoder,
            ApplicationProperties properties
    ) {
        this(repository, passwordEncoder, properties, Clock.systemUTC());
    }

    CredentialService(
            AdminUserRepository repository,
            PasswordEncoder passwordEncoder,
            ApplicationProperties properties,
            Clock clock
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional(noRollbackFor = AuthException.class)
    public AdminUser authenticate(String email, String password) {
        Instant now = clock.instant();
        AdminUser user = repository.findByEmailForUpdate(email).orElse(null);

        if (user == null) {
            passwordEncoder.matches(password, DUMMY_PASSWORD_HASH);
            throw invalidCredentials();
        }
        if (!user.isActive()) throw invalidCredentials();
        if (user.isLockedAt(now)) {
            throw new AuthException("Acesso temporariamente bloqueado. Tente novamente mais tarde.", HttpStatus.LOCKED);
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= properties.security().maxFailedLoginAttempts()) {
                user.setLockedUntil(now.plus(properties.security().accountLockDuration()));
                user.setFailedLoginAttempts(0);
            }
            repository.save(user);
            throw invalidCredentials();
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(now);
        return repository.save(user);
    }

    @Transactional(noRollbackFor = AuthException.class)
    public AdminUser changePassword(AdminUser userReference, String currentPassword, String newPassword) {
        AdminUser user = repository.findByEmailForUpdate(userReference.getEmail())
                .orElseThrow(this::invalidCredentials);
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new AuthException("A senha atual esta incorreta.", HttpStatus.BAD_REQUEST);
        }
        validateNewPassword(currentPassword, newPassword);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setCredentialVersion(Math.addExact(user.getCredentialVersion(), 1));
        return repository.save(user);
    }

    private void validateNewPassword(String currentPassword, String newPassword) {
        boolean strong = newPassword.chars().anyMatch(Character::isUpperCase)
                && newPassword.chars().anyMatch(Character::isLowerCase)
                && newPassword.chars().anyMatch(Character::isDigit);
        if (!strong) {
            throw new AuthException("A nova senha deve conter letras maiusculas, minusculas e numeros.", HttpStatus.BAD_REQUEST);
        }
        if (currentPassword.equals(newPassword)) {
            throw new AuthException("A nova senha deve ser diferente da senha atual.", HttpStatus.BAD_REQUEST);
        }
    }

    private AuthException invalidCredentials() {
        return new AuthException("E-mail ou senha invalidos.", HttpStatus.UNAUTHORIZED);
    }
}
