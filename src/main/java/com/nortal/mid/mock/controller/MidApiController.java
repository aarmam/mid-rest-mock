package com.nortal.mid.mock.controller;

import com.nortal.mid.mock.service.MidSigningService;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.rest.dao.request.MidAuthenticationRequest;
import ee.sk.mid.rest.dao.request.MidSignatureRequest;
import ee.sk.mid.rest.dao.response.MidAuthenticationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static java.util.UUID.randomUUID;

@RestController
@RequiredArgsConstructor
public class MidApiController {
    public static final String MID_API_AUTHENTICATION_REQUEST = "/authentication";
    public static final String MID_API_AUTHENTICATION_STATUS = "/authentication/session/{sessionId}";
    public static final String MID_API_SIGNATURE_REQUEST = "/signature";
    public static final String MID_API_SIGNATURE_STATUS = "/signature/session/{sessionId}";
    private final MidSigningService midSigningService;

    @PostMapping(MID_API_AUTHENTICATION_REQUEST)
    public MidAuthenticationResponse authenticate(@RequestBody MidAuthenticationRequest request) {
        String sessionId = randomUUID().toString();
        midSigningService.authenticate(sessionId, request);
        return new MidAuthenticationResponse(sessionId);
    }

    @GetMapping(MID_API_AUTHENTICATION_STATUS)
    public ResponseEntity<MidSessionStatus> authenticationStatus(@PathVariable String sessionId) {
        return midSigningService.getAuthenticationStatus(sessionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(MID_API_SIGNATURE_REQUEST)
    public MidAuthenticationResponse signature(@RequestBody MidSignatureRequest request) {
        String sessionId = randomUUID().toString();
        midSigningService.sign(sessionId, request);
        return new MidAuthenticationResponse(sessionId);
    }

    @GetMapping(MID_API_SIGNATURE_STATUS)
    public ResponseEntity<MidSessionStatus> signatureStatus(@PathVariable String sessionId) {
        return midSigningService.getSignatureStatus(sessionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
