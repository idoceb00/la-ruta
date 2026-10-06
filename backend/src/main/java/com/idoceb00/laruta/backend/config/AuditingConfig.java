package com.idoceb00.laruta.backend.config;

import com.idoceb00.laruta.backend.security.CurrentUserProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class AuditingConfig {

    @Bean
    public AuditorAware<Long> auditorAware(CurrentUserProvider currentUserProvider) {
        return currentUserProvider::findCurrentUserId;
    }
}
