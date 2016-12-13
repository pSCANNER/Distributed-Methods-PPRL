import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;


public class Encryption {
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	private static String HMACkey = "r}v!KMFOi{Gt/$:[$j7gz,%^k&X_>3s$Q|>Usy?1:{>Oy3C3##[Rp2M-5mwA7US"; //key for HMAC algorithm
	private static Charset charset = Charset.forName("UTF-8"); // encoding used for storing hash values as strings
	private static String hashName = "SHA1"; // SHA1 gives good enough accuracy in most circumstances. Change to MD5 if it's needed
    private static final MessageDigest digestFunction;
    static { // The digest method is reused between instances to provide higher entropy.
        MessageDigest tmp;
        try {
            tmp = java.security.MessageDigest.getInstance(hashName);
        } catch (NoSuchAlgorithmException e) {
            tmp = null;
        }
        digestFunction = tmp;
    }
	private static String HMAC(String data) throws java.security.SignatureException {
		String result;
		try {
			// Get an hmac_sha1 key from the raw key bytes
			byte[] keyBytes = HMACkey.getBytes();			
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
			throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
		}
		return result;
	}
	public static long encrypt(String data) throws Exception{
		return createHash(HMAC(data));
	}
    /**
     * Generates a digest based on the contents of a String.
     *
     * @param val specifies the input data.
     * @param charset specifies the encoding of the input data.
     * @return digest as long.
     */
    private static long createHash(String val, Charset charset) {
        return createHash(val.getBytes(charset));
    }

    /**
     * Generates a digest based on the contents of a String.
     *
     * @param val specifies the input data. The encoding is expected to be UTF-8.
     * @return digest as long.
     */
    private static long createHash(String val) {
        return createHash(val, charset);
    }

    /**
     * Generates a digest based on the contents of an array of bytes.
     *
     * @param data specifies input data.
     * @return digest as long.
     */
    private static long createHash(byte[] data) {
        long h = 0;
        byte[] res;

        synchronized (digestFunction) {
            res = digestFunction.digest(data);
        }

        for (int i = 0; i < 4; i++) {
            h <<= 8;
            h |= ((int) res[i]) & 0xFF;
        }
        return h;
    }

}
