package com.nortal.mid.mock.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import javax.validation.constraints.NotNull;

@ConstructorBinding
@ConfigurationProperties(prefix = "mid")
public record MidProperties(
        @NotNull
        String keyStorePath,
        @NotNull
        String keyStoreType,
        @NotNull
        String keyStorePassword,
        @NotNull
        String authKeyAlias,
        @NotNull
        String authAlgorithm,
        @NotNull
        String signKeyAlias,
        @NotNull
        String signAlgorithm) {
}
