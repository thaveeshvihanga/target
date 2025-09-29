package lk.ac.kln.todoapplication.Auth;

import android.util.Base64;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtils {

    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256; // bits

    public static byte[] generateSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

    public static String hashPassword(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static String saltToString(byte[] salt) {
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    public static byte[] stringToSalt(String saltStr) {
        return Base64.decode(saltStr, Base64.NO_WRAP);
    }

    public static boolean verifyPassword(char[] attemptedPassword, String storedHash, String storedSalt) {
        byte[] salt = stringToSalt(storedSalt);
        String attemptedHash = hashPassword(attemptedPassword, salt);
        return slowEquals(storedHash.getBytes(), attemptedHash.getBytes());
    }

    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
