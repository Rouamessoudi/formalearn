package com.esprit.formation.config;

import com.esprit.formation.security.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, MlProperties.class})
public class AppPropertiesConfig {
}
