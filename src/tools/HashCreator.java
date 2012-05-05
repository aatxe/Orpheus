package tools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashCreator {
	private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
	private static MessageDigest hash;
	
	
	public static void reset() {
		hash = null;	
	}
	
	public static void setMessageDigest(MessageDigest md) {
		hash = md;
	}
	
	public static String getHash(String message) throws NoSuchAlgorithmException {
		if (hash == null) {
			hash = MessageDigest.getInstance("SHA1");
		}
		hash.update(message.getBytes());
		return asHex(hash.digest());
	}
	
	public static String getHash(MessageDigest md, String message) {
		md.update(message.getBytes());
		return asHex(md.digest());
    }
	
	public static String asHex(byte[] buf) {
    	char[] chars = new char[2 * buf.length];
    	for (int i = 0; i < buf.length; ++i) {
    		chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
    		chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
    	}
    	return new String(chars);
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
