package com.ampnet.userservice.service;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Map;

// TODO: DELETE THIS CLASS
// Only for testing java example
public class IdentyumDecryptTest {

    @Test
    public void mustDecrypt() throws Exception {
        String data = readFile("identyum/encrypted-data.txt");
        Map<String, Object> decryptedData = decrypt(data, "12345abcde", "8c99227d-5108-4b1d-bcd2-449826032f99");
        String decryptedJson = new GsonBuilder().create().toJson(decryptedData);
        System.out.println(decryptedJson);
    }

    public Map<String, Object> decrypt(String value, String key, String initVector) throws Exception{
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] keyMD5 = md.digest(key.getBytes());
            byte[] ivMD5 = md.digest(initVector.getBytes());
            IvParameterSpec iv = new IvParameterSpec(ivMD5);
            SecretKeySpec skeySpec = new SecretKeySpec(keyMD5, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] decrypted = cipher.doFinal(Base64.decodeBase64(value));
            String data = new String(decrypted);
            GsonBuilder gb = new GsonBuilder();
            return gb.create().fromJson(data, new TypeToken<Map<String, Object>>(){}.getType());
        } catch (Exception ex) {
            throw new Exception("error");
        }
    }

    private String readFile(String path) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(path).getFile());
        return FileUtils.readFileToString(file, "UTF-8");
    }
}
