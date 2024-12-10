package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtils {
	
	// ideally should be stored elsewhere securely
	private static final String APP_PEPPER = "f9b9c832-d41f-488e-aea6-4d68a0f8bfab";

	public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
	
	public static String hashPassword(String password, String salt) {
        try {
            String combinedPass = password + salt + APP_PEPPER;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            
            for (int i = 0; i < 10000; i++) {
                byte[] hashedBytes = md.digest(combinedPass.getBytes());
                combinedPass = Base64.getEncoder().encodeToString(hashedBytes);
            }
            
            return combinedPass;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }
	
	public static boolean verifyPassword(String inputPassword, String storedHash, String storedSalt) {
        String hashedInputPass = hashPassword(inputPassword, storedSalt);
        return hashedInputPass.equals(storedHash);
    }
}
