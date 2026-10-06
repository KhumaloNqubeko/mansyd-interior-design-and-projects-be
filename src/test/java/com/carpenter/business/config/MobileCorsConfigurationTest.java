package com.carpenter.business.config;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;

class MobileCorsConfigurationTest {
    private MockEnvironment environment(boolean local) throws IOException {
        MockEnvironment env = new MockEnvironment();
        var loader = new YamlPropertySourceLoader();
        loader.load("base", new ClassPathResource("application.yml")).forEach(source -> env.getPropertySources().addLast(source));
        if (local) loader.load("local", new ClassPathResource("application-local.yml")).forEach(source -> env.getPropertySources().addFirst(source));
        return env;
    }
    private org.springframework.web.cors.CorsConfiguration cors(MockEnvironment env) {
        var source = new SecurityConfig().corsConfigurationSource(env.getProperty("app.frontend-origin"), env.getProperty("app.mobile-origin"), env.getProperty("app.mobile-preview-origin"));
        var request = new MockHttpServletRequest("GET", "/api/projects/my");
        return source.getCorsConfiguration(request);
    }
    @Test void localProfileAllowsTheCustomerPreviewAndNativeOrigin() throws Exception {
        var config = cors(environment(true));
        assertThat(config.getAllowedOrigins()).contains("http://localhost:4300", "https://localhost");
        assertThat(config.getAllowCredentials()).isTrue();
    }
    @Test void productionDefaultsDoNotAllowTheDevelopmentPreview() throws Exception {
        assertThat(cors(environment(false)).getAllowedOrigins()).contains("https://localhost").doesNotContain("http://localhost:4300");
    }
}
