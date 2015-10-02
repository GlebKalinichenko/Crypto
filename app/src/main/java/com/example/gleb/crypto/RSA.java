package com.example.gleb.crypto;

import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by Gleb on 27.09.2015.
 */
public class RSA {
    public static KeyPairGenerator kpg;
    public static KeyPair kp;
    public static PublicKey publicKey;
    public static PrivateKey privateKey;
    public static byte[] encryptedBytes, decryptedBytes;
    public static Cipher cipher, cipher1, cipherMd5, cipherMd6;
    public static String encrypted, decrypted;

    public static byte[] Encrypt(byte[] plain) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        kp = kpg.genKeyPair();
        publicKey = kp.getPublic();
        privateKey = kp.getPrivate();

        cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        encryptedBytes = cipher.doFinal(plain);

        encrypted = bytesToString(encryptedBytes);

        createFile("PublicKey", publicKey.getEncoded());
        createFile("PrivateKey", privateKey.getEncoded());
        createFile("KeyWithPublicKey", encryptedBytes);

        return encryptedBytes;

    }

    public static byte[] encryptMd5(byte[] md5) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        cipherMd5 = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] encryptedMd5 = cipher.doFinal(md5);

        return encryptedMd5;

    }

    public static byte[] decryptMd5(byte[] md5) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        cipherMd6 = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] encryptedMd5 = cipher.doFinal(md5);

        return encryptedMd5;

    }

    public static byte[] Decrypt(byte[] result) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        cipher1=Cipher.getInstance("RSA");
        cipher1.init(Cipher.DECRYPT_MODE, privateKey);
        decryptedBytes = cipher1.doFinal(result);
        decrypted = new String(decryptedBytes);
        return decryptedBytes;

    }

    public static void createFile(String name, byte[] value){
        try {
            File gpxfile = new File(Environment.getExternalStorageDirectory(), name);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(new String(value));
            writer.flush();
            writer.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }

    }

    public static String bytesToString(byte[] b) {
        byte[] b2 = new byte[b.length + 1];
        b2[0] = 1;
        System.arraycopy(b, 0, b2, 1, b.length);
        return new BigInteger(b2).toString(36);
    }

    public  byte[] stringToBytes(String s) {
        byte[] b2 = new BigInteger(s, 36).toByteArray();
        return Arrays.copyOfRange(b2, 1, b2.length);
    }
}
