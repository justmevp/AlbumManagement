package org.studyeasy.SpringRestDemo.security;

import java.security.*;

final class KeyGeneratorUtils {
    private KeyGeneratorUtils(){}

    static KeyPair generateRSKey(){
        KeyPair keyPair;
        try{
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();

        }catch(Exception ex){
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }
    
}
