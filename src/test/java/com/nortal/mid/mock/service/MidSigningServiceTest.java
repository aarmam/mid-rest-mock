package com.nortal.mid.mock.service;

import com.nortal.mid.mock.MidTestConfiguration;
import ee.sk.mid.*;
import ee.sk.mid.rest.dao.MidSessionSignature;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.rest.dao.request.MidAuthenticationRequest;
import ee.sk.mid.rest.dao.request.MidSignatureRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.security.cert.X509Certificate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ContextConfiguration(classes = {MidTestConfiguration.class}, initializers = ConfigDataApplicationContextInitializer.class)
class MidSigningServiceTest {
    @Autowired
    private MidSigningService midSigningService;
    @Autowired
    private MidAuthenticationResponseValidator midAuthenticationResponseValidator;
    @Autowired
    private X509Certificate authenticationCertificate;
    @Autowired
    private X509Certificate signingCertificate;

    @Test
    void validAuthenticationResult_WhenSignedWithAuthenticationKey() {
        MidAuthenticationHashToSign hashToSign = MidAuthenticationHashToSign.generateRandomHashOfType(MidHashType.SHA384);
        String sessionId = UUID.randomUUID().toString();
        MidAuthenticationRequest request = new MidAuthenticationRequest();
        request.setHash(hashToSign.getHashInBase64());
        request.setHashType(hashToSign.getHashType());
        midSigningService.authenticate(sessionId, request);

        MidSessionStatus authenticationSessionStatus = midSigningService.getAuthenticationStatus(sessionId).orElseThrow();

        assertTrue(midSigningService.getSignatureStatus(sessionId).isEmpty());
        assertNotNull(authenticationSessionStatus);
        MidAuthenticationResult result = assertSignature(authenticationSessionStatus, hashToSign, authenticationCertificate);
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void validSignatureResult_WhenSignedWithSigningKey() {
        MidAuthenticationHashToSign hashToSign = MidAuthenticationHashToSign.generateRandomHashOfType(MidHashType.SHA384);
        String sessionId = UUID.randomUUID().toString();
        MidSignatureRequest request = new MidSignatureRequest();
        request.setHash(hashToSign.getHashInBase64());
        request.setHashType(hashToSign.getHashType());
        midSigningService.sign(sessionId, request);

        MidSessionStatus signingSessionStatus = midSigningService.getSignatureStatus(sessionId).orElseThrow();

        assertTrue(midSigningService.getAuthenticationStatus(sessionId).isEmpty());
        assertNotNull(signingSessionStatus);
        MidAuthenticationResult result = assertSignature(signingSessionStatus, hashToSign, signingCertificate);
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    private MidAuthenticationResult assertSignature(MidSessionStatus signingResult, MidAuthenticationHashToSign hashToSign, X509Certificate certificate) {
        MidSessionSignature sessionSignature = signingResult.getSignature();
        MidAuthentication authentication = MidAuthentication.newBuilder()
                .withResult(signingResult.getResult())
                .withSignatureValueInBase64(sessionSignature.getValue())
                .withAlgorithmName(sessionSignature.getAlgorithm())
                .withCertificate(certificate)
                .withSignedHashInBase64(hashToSign.getHashInBase64())
                .withHashType(hashToSign.getHashType())
                .build();
        return assertDoesNotThrow(() -> midAuthenticationResponseValidator.validate(authentication));
    }
}