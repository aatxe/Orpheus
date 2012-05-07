package tools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashCreator {
	private static MessageDigest hash;
	
	
	public static void reset() {
		hash = null;	
	}
	
	public static void setMessageDigest(MessageDigest md) {
		hash = md;
	}
	
	public static String getHash(String message) throws NoSuchAlgorithmException {
		if (hash == null) {
			hash = MessageDigest.getInstance("SHA-1");
		}
		hash.update(message.getBytes());
		return HexTool.toString(hash.digest());
	}
	
	public static String getHash(String type, String message) throws NoSuchAlgorithmException {
		return HashCreator.getHash(MessageDigest.getInstance(type), message);
	}
	
	public static String getHash(MessageDigest md, String message) {
		md.update(message.getBytes());
		return HexTool.toString(md.digest());
    }
	
	// Just a simple tester for the hash calculator.
	public static void main(String[] args) { 
		try {
			System.out.println(getHash("password"));
			reset();
			setMessageDigest(MessageDigest.getInstance("SHA1"));
			System.out.println(getHash("password"));
			System.out.println(getHash(MessageDigest.getInstance("SHA1"), "password"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
}
