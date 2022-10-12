package com.nortal.mid.mock;

import ee.sk.mid.MidAuthenticationResponseValidator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@TestConfiguration
public class MidTestConfiguration {

    @Bean
    public KeyStore midTrustStore(ResourceLoader loader) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        Resource resource = loader.getResource("classpath:mid-truststore.p12");
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        trustStore.load(resource.getInputStream(), "1234".toCharArray());
        return trustStore;
    }

    @Bean
    public MidAuthenticationResponseValidator midAuthenticationResponseValidator(KeyStore midTrustStore) {
        return new MidAuthenticationResponseValidator(midTrustStore);
    }
}
