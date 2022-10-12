package com.nortal.mid.mock.service;

import com.nortal.mid.mock.configuration.MidProperties;
import ee.sk.mid.MidHashType;
import ee.sk.mid.rest.dao.MidSessionSignature;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.rest.dao.request.MidAuthenticationRequest;
import ee.sk.mid.rest.dao.request.MidSignatureRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.cache2k.Cache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPrivateKey;
import java.util.Optional;

import static java.util.Base64.getDecoder;
import static java.util.Base64.getEncoder;
import static org.apache.commons.lang3.ArrayUtils.addAll;

@Slf4j
@Service
@RequiredArgsConstructor
public class MidSigningService {
    public static final String RESULT_OK = "OK";
    public static final String STATE_COMPLETE = "COMPLETE";
    public static final String NON_EWITH_ECDSA = "NONEwithECDSA";
    private final MidProperties midProperties;

    private final X509Certificate authenticationCertificate;
    private final PrivateKey authenticationPrivateKey;

    private final X509Certificate signingCertificate;
    private final PrivateKey signingPrivateKey;
    private final Cache<String, MidAuthenticationRequest> midAuthenticationCache;
    private final Cache<String, MidSignatureRequest> midSignatureCache;

    private String encodedAuthenticationCertificate;
    private String encodedSigningCertificate;

    @Value("${mid.cache.evict-after-status-request}")
    private boolean evictAfterStatusRequest;

    @PostConstruct
    private void encodeCertificates() throws CertificateEncodingException {
        encodedAuthenticationCertificate = getEncoder().encodeToString(authenticationCertificate.getEncoded());
        encodedSigningCertificate = getEncoder().encodeToString(signingCertificate.getEncoded());
    }

    public void authenticate(String sessionId, MidAuthenticationRequest authenticationRequest) {
        midAuthenticationCache.put(sessionId, authenticationRequest);
    }

    public void sign(String sessionId, MidSignatureRequest signatureRequest) {
        midSignatureCache.put(sessionId, signatureRequest);
    }

    public Optional<MidSessionStatus> getAuthenticationStatus(String sessionId) {
        MidAuthenticationRequest authenticationRequest = evictAfterStatusRequest ? midAuthenticationCache.peekAndRemove(sessionId) : midAuthenticationCache.get(sessionId);
        if (authenticationRequest == null) {
            return Optional.empty();
        } else {
            byte[] hashToSign = getDecoder().decode(authenticationRequest.getHash());
            MidHashType hashType = authenticationRequest.getHashType();
            return Optional.of(getStatus(encodedAuthenticationCertificate, authenticationPrivateKey, hashToSign, hashType));
        }
    }

    public Optional<MidSessionStatus> getSignatureStatus(String sessionId) {
        MidSignatureRequest signatureRequest = evictAfterStatusRequest ? midSignatureCache.peekAndRemove(sessionId) : midSignatureCache.get(sessionId);
        if (signatureRequest == null) {
            return Optional.empty();
        } else {
            byte[] hashToSign = getDecoder().decode(signatureRequest.getHash());
            MidHashType hashType = signatureRequest.getHashType();
            return Optional.of(getStatus(encodedSigningCertificate, signingPrivateKey, hashToSign, hashType));
        }
    }

    private MidSessionStatus getStatus(String encodedCertificate, PrivateKey privateKey, byte[] hashToSign, MidHashType hashType) {
        byte[] signature = signHash(privateKey, hashToSign, hashType);
        MidSessionStatus sessionStatus = new MidSessionStatus();
        sessionStatus.setCert(encodedCertificate);
        sessionStatus.setResult(RESULT_OK);
        sessionStatus.setState(STATE_COMPLETE);
        MidSessionSignature sessionSignature = new MidSessionSignature();
        sessionSignature.setAlgorithm(midProperties.signAlgorithm());
        sessionSignature.setValue(Base64.encodeBase64String(signature));
        sessionStatus.setSignature(sessionSignature);
        return sessionStatus;
    }

    private byte[] signHash(PrivateKey privateKey, byte[] hashToSign, MidHashType hashType) {
        if (privateKey instanceof ECPrivateKey ecPrivateKey) {
            byte[] signatureInAsn1 = sign(NON_EWITH_ECDSA, privateKey, hashToSign);
            int fieldSize = ecPrivateKey.getParams().getCurve().getField().getFieldSize();
            return DsaSignature.fromAsn1Encoding(signatureInAsn1).encodeInCvc(fieldSize);
        } else {
            return sign("NONEwith" + privateKey.getAlgorithm(), privateKey, addPadding(hashToSign, hashType));
        }
    }

    @SneakyThrows
    private byte[] sign(String signatureAlgo, PrivateKey privateKey, byte[] bytes) {
        Signature signature = Signature.getInstance(signatureAlgo);
        signature.initSign(privateKey);
        signature.update(bytes);
        return signature.sign();
    }

    private byte[] addPadding(byte[] digest, MidHashType hashType) {
        return addAll(hashType.getDigestInfoPrefix(), digest);
    }
}
