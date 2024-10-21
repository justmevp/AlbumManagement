package org.studyeasy.SpringRestDemo.security;

import com.nimbusds.jose.jwk.RSAKey;
import java.security.*;
import java.security.interfaces.*;
import java.util.UUID;

public class Jwks {
    private Jwks() {
    }

    public static RSAKey generateRsa() {
        KeyPair keyPair = KeyGeneratorUtils.generateRSKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
    }

}
