package co.com.pragma.r2dbc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories(basePackages = "co.com.pragma.r2dbc.repositories")
public class R2dbcRepositoriesConfig {
}


