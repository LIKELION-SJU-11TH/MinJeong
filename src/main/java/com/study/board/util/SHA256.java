package com.study.board.util;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class SHA256 {
    public static String encrypt(String plainText) throws RuntimeException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256"); // SHA-256이라는 알고리즘을 이용하기 위한 객체 생성.

            byte[] bytes = plainText.getBytes();  //비트로 모두 변환.

            md.update(plainText.getBytes()); // plainText -> 바이트 배열로 변환 , 알고리즘에 입력 값을 추가함.

            byte[] byteData = md.digest(); //해시 값을 생성

            StringBuffer hexString = new StringBuffer(); // StringBuffer 생성 (모르면 구글링), 간단히 말하면 문자열을 조작할 수 있는 객체.

            for(int i = 0; i < byteData.length; ++i) { //byte를 hex로 변환해 hexString이라는 인스턴스에 이어붙여 문자열을 만듦 -> hexString은 암호화된 문자열이 됨.
                String hex = Integer.toHexString(255 & byteData[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
