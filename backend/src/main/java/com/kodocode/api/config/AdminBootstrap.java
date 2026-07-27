package com.kodocode.api.config;

import com.kodocode.api.admin.AdminRole;
import com.kodocode.api.admin.AdminUser;
import com.kodocode.api.admin.AdminUserRepository;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final ApplicationProperties properties;
    private final AdminUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrap(
            ApplicationProperties properties,
            AdminUserRepository repository,
            PasswordEncoder passwordEncoder
    ) {
        this.properties = properties;
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ApplicationProperties.BootstrapAdmin admin = properties.bootstrapAdmin();
        if (!admin.enabled()) return;

        validate(admin);
        String email = admin.email().strip().toLowerCase(Locale.ROOT);
        if (repository.existsByEmailIgnoreCase(email)) {
            log.info("Bootstrap administrator already exists; no changes were made");
            return;
        }

        AdminUser user = new AdminUser();
        user.setName(admin.name().strip());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(admin.password()));
        user.setRole(AdminRole.ADMIN);
        user.setActive(true);
        repository.save(user);
        log.info("Initial administrator was provisioned successfully");
    }

    private void validate(ApplicationProperties.BootstrapAdmin admin) {
        if (isBlank(admin.name()) || isBlank(admin.email()) || isBlank(admin.password())) {
            throw new IllegalStateException("Todas as variaveis KODO_BOOTSTRAP_ADMIN_* sao obrigatorias quando o bootstrap esta ativo.");
        }
        boolean strong = admin.password().length() >= 12
                && admin.password().chars().anyMatch(Character::isUpperCase)
                && admin.password().chars().anyMatch(Character::isLowerCase)
                && admin.password().chars().anyMatch(Character::isDigit);
        if (!strong) {
            throw new IllegalStateException("A senha inicial deve ter ao menos 12 caracteres, maiuscula, minuscula e numero.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

