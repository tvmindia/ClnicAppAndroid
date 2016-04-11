package com.tech.thrithvam.theclinicapp;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static android.util.Base64.*;

//AES 128bit Cross Platform (Java and C#) Encryption Compatibility

public class Cryptography {
    String key= "thrithvam2016";

    public String Encrypt(String plainText){
        String encryptedString="";
        try {
            byte[] plainTextBytes = plainText.getBytes("UTF-8");
            byte[] keyBytes= new byte[16];
            byte[] parameterKeyBytes= key.getBytes("UTF-8");
            System.arraycopy(parameterKeyBytes, 0, keyBytes, 0, Math.min(parameterKeyBytes.length, keyBytes.length));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(keyBytes);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            plainTextBytes = cipher.doFinal(plainTextBytes);
            encryptedString= encodeToString(plainTextBytes, DEFAULT);
        }
        catch (UnsupportedEncodingException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return encryptedString;
    }

    public String Decrypt(String encryptedText){
        String plainText="";
        try {
            byte[] cipheredBytes = Base64.decode(encryptedText, Base64.DEFAULT);
            byte[] keyBytes= new byte[16];
            byte[] parameterKeyBytes;
            parameterKeyBytes = key.getBytes("UTF-8");
            System.arraycopy(parameterKeyBytes, 0, keyBytes, 0, Math.min(parameterKeyBytes.length, keyBytes.length));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKeySpecy = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(keyBytes);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpecy, ivParameterSpec);
            cipheredBytes = cipher.doFinal(cipheredBytes);
            plainText= new String(cipheredBytes, "UTF-8");
        } catch (UnsupportedEncodingException | NoSuchPaddingException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return plainText;
    }

}
