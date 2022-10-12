package com.nortal.mid.mock.configuration;

import ee.sk.mid.rest.dao.request.MidAuthenticationRequest;
import ee.sk.mid.rest.dao.request.MidSignatureRequest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@EnableAsync
@Configuration
@ConfigurationPropertiesScan
public class MidConfiguration {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Bean
    public KeyStore midSigningKeyStore(MidProperties midProperties, ResourceLoader loader) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        Resource resource = loader.getResource(midProperties.keyStorePath());
        KeyStore trustStore = KeyStore.getInstance(midProperties.keyStoreType());
        trustStore.load(resource.getInputStream(), midProperties.keyStorePassword().toCharArray());
        return trustStore;
    }

    @Bean
    public X509Certificate authenticationCertificate(MidProperties midProperties, KeyStore midSigningKeyStore) throws KeyStoreException {
        return (X509Certificate) midSigningKeyStore.getCertificate(midProperties.authKeyAlias());
    }

    @Bean
    public PrivateKey authenticationPrivateKey(MidProperties midProperties, KeyStore midSigningKeyStore) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        return (PrivateKey) midSigningKeyStore.getKey(midProperties.authKeyAlias(), midProperties.keyStorePassword().toCharArray());
    }

    @Bean
    public PublicKey authenticationPublicKey(X509Certificate authenticationCertificate) {
        return authenticationCertificate.getPublicKey();
    }

    @Bean
    public X509Certificate signingCertificate(MidProperties midProperties, KeyStore midSigningKeyStore) throws KeyStoreException {
        return (X509Certificate) midSigningKeyStore.getCertificate(midProperties.signKeyAlias());
    }

    @Bean
    public PrivateKey signingPrivateKey(MidProperties midProperties, KeyStore midSigningKeyStore) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        return (PrivateKey) midSigningKeyStore.getKey(midProperties.signKeyAlias(), midProperties.keyStorePassword().toCharArray());
    }

    @Bean
    public PublicKey signingPublicKey(X509Certificate signingCertificate) {
        return signingCertificate.getPublicKey();
    }

    @Bean
    public Cache<String, MidAuthenticationRequest> midAuthenticationCache(@Value("${mid.cache.expire-after-write}") Duration expireAfterWrite) {
        return Cache2kBuilder.of(String.class, MidAuthenticationRequest.class)
                .name("mid-auth")
                .expireAfterWrite(expireAfterWrite.toSeconds(), TimeUnit.SECONDS)
                .entryCapacity(100_000)
                .build();
    }

    @Bean
    public Cache<String, MidSignatureRequest> midSignatureCache(@Value("${mid.cache.expire-after-write}") Duration expireAfterWrite) {
        return Cache2kBuilder.of(String.class, MidSignatureRequest.class)
                .name("mid-sig")
                .expireAfterWrite(expireAfterWrite.toSeconds(), TimeUnit.SECONDS)
                .entryCapacity(100_000)
                .build();
    }
}

