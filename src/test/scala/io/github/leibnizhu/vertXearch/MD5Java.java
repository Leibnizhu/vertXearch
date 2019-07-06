package io.github.leibnizhu.vertXearch;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Java {
    String[] hexStrs = new String[]{"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
    public String md5(String s) throws NoSuchAlgorithmException {
        byte[] codedBytes = MessageDigest.getInstance("MD5").digest(s.getBytes());
        StringBuilder sb = new StringBuilder();
        for(byte b: codedBytes) sb.append(hexStrs[(b & 0xf0) >> 4]).append(hexStrs[b & 0x0f]);
        return sb.toString();
    }
}
