package ru.yandex.practicum.gateway.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security")
public class SecurityUserProperties {

    private List<UserConfig> users = new ArrayList<>();

    @Getter
    @Setter
    public static class UserConfig {
        private String username;
        private String password;
        private List<String> roles = new ArrayList<>();
    }
}