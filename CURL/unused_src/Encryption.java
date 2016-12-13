import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
// TODO: update class description.
/**
 * Encryption.java
 * Purpose: This class deals with all of the encryption of the Patient data.
 * This class is only used in the BF class.
 * 
 * @author Vijay Thurimella
 * @version 2.0 8/31/10
 */

public class Encryption 
{
	// The Algorithm to use for the HMAC_SHA1 computation
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	// The set of keys to use for the HMAC SHA1
	private static String[] HMACkeys = populateKeys();
	
	// encoding used for storing hash values as strings
	private static Charset charset = Charset.forName("UTF-8"); 
	
	// SHA1 gives good enough accuracy in most circumstances. Change to MD5 if it's needed
	private static final String hashName = "SHA1"; 
	
	// The digest method is reused between instances to provide higher entropy.
	private static final MessageDigest digestFunction;
	static 
	{ 
		MessageDigest tmp;
		try {
			tmp = java.security.MessageDigest.getInstance(hashName);
		} catch (NoSuchAlgorithmException e) {
			tmp = null;
		}
		digestFunction = tmp;
	}
	
	
	/**
	 * This is a HMAC algorithm that has been verified by using the HASHCALC program
	 * @param data String that needs to be Encrypted
	 * @return String of encryption
	 * @throws java.security.SignatureException if Encryption error occurs
	 */
	//Add ArrayList full of keys
	private static String HMAC(String data, int i){
		String result = "";
		try {
			// Get an hmac_sha1 key from the raw key bytes
			byte[] keyBytes = HMACkeys[i].getBytes();			
			SecretKeySpec signingKey = new SecretKeySpec(keyBytes, HMAC_SHA1_ALGORITHM);
			// Get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);
			// Compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(data.getBytes());
			// Convert raw bytes to Hex
			byte[] hexBytes = new Hex().encode(rawHmac);
			//  Covert array of Hex bytes to a String
			result = new String(hexBytes, "ISO-8859-1");
			//System.out.println("MAC : " + result);
		} 
		catch (Exception e) {
				System.err.println(("Failed to generate HMAC : " + e.getMessage()));
		}
		return result;
	}

	// TODO: Add function documentation here
	public static long encrypt(String data, int n){
		long h = 0;
		byte[] res;

		synchronized (digestFunction) {
			res = digestFunction.digest(HMAC(data, n).getBytes(charset));
		}

		for (int i = 0; i < 4; i++) {
			h <<= 8;
			h |= ((int) res[i]) & 0xFF;
		}
		return h;
	}
	
	/**
	 * Returns a SHA1 of the input as a string
	 * @param text - input
	 * @return - returns the SHA1 as a string
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static String SHA1(String text) 
			throws NoSuchAlgorithmException, UnsupportedEncodingException  { 
		MessageDigest md;
		md = MessageDigest.getInstance("SHA-1");
		byte[] sha1hash = new byte[40];
		md.update(text.getBytes("iso-8859-1"), 0, text.length());
		sha1hash = md.digest();
		return MyUtilities.convertToHex(sha1hash);
	}
	
	
	/**
	 * The set of keys to use for the HMAC_SHA1 algorithm
	 * @return
	 */
	private static String[] populateKeys() 
	{
		String[] temp = {
				"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"
		};
		return temp;
	}
	
}