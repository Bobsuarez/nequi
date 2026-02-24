package co.com.pragma.api.config.authority;

import co.com.pragma.config.security.ISecurityCustomizer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.stereotype.Component;

@Component
public class franchiseSecurityCustomizer implements ISecurityCustomizer {

    @Override
    public void customize(ServerHttpSecurity.AuthorizeExchangeSpec spec) {
        spec.pathMatchers(HttpMethod.POST, "/api/v1/franchises").hasRole("ADMIN");
        spec.pathMatchers(HttpMethod.PATCH, "/api/v1/franchises/*/name").hasRole("ADMIN");
    }
}
