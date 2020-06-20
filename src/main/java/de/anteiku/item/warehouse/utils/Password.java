package de.anteiku.item.warehouse;

import de.anteiku.item.warehouse.database.SQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

public class Password{

	private static final Logger LOG = LoggerFactory.getLogger(Password.class);
	private static final SecureRandom RAND = new SecureRandom();

	private static final int ITERATIONS = 65536;

	private static final int KEY_LENGTH = 512;
	private static final String ALGORITHM = "PBKDF2WithHmacSHA512";

	public static String generateSalt(int length){
		byte[] salt = new byte[length];
		RAND.nextBytes(salt);
		return Base64.getEncoder().encodeToString(salt);
	}

	public static String hashPassword(String password, String salt){
		char[] chars = password.toCharArray();
		byte[] bytes = salt.getBytes();

		PBEKeySpec spec = new PBEKeySpec(chars, bytes, ITERATIONS, KEY_LENGTH);

		Arrays.fill(chars, Character.MIN_VALUE);

		try{
			SecretKeyFactory fac = SecretKeyFactory.getInstance(ALGORITHM);
			byte[] securePassword = fac.generateSecret(spec).getEncoded();
			return Base64.getEncoder().encodeToString(securePassword);

		}
		catch(NoSuchAlgorithmException | InvalidKeySpecException e){
			LOG.error("Error while hasing password", e);
		}
		finally{
			spec.clearPassword();
		}
		return null;
	}

	public static boolean checkPassword(String password, String salt, String hash){
		String newHash = hashPassword(password, salt);
		if(newHash == null){
			return false;
		}
		return newHash.equals(hash);
	}
}
