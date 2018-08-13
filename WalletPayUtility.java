package org.rbfcu.netbranch.card.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.Cipher;

import org.apache.commons.lang3.StringUtils;
import org.rbfcu.netbranch.common.web.NboConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WalletPayUtility {
	private static final Logger LOG = LoggerFactory.getLogger(WalletPayUtility.class);
	//private static final String ALGORITHM = "RSA";
	private static final String ALGORITHM = "RSA/None/PKCS1Padding";
	private static final String ENCODING_METHOD = "UTF8";

	public PrivateKey getRsaPrivateKey() throws Exception {
		try {
			String rsaPrivatekeyFile = NboConfig.getInstance().getValue("rbfcu-cert-tav-private");
			FileInputStream fin = new FileInputStream(new File(rsaPrivatekeyFile).getAbsolutePath());
			ObjectInputStream objIn = new ObjectInputStream(fin);
			return (PrivateKey) objIn.readObject();
		} catch (Exception e) {
			LOG.error("Error occured while getting rsaprivate key");
			throw e;
		}
	}

	public byte[] encrypt(String plaintext, Key privateKey) throws Exception {
		byte[] encryptedMsg = null;
		byte[] base64Encoded = null;
		if (StringUtils.isNotBlank(plaintext)) {
			Cipher cObj = Cipher.getInstance(ALGORITHM);
			cObj.init(Cipher.ENCRYPT_MODE, privateKey);
			encryptedMsg = cObj.doFinal(plaintext.getBytes(ENCODING_METHOD));//to bytes(UTF8) and RSA
			base64Encoded = Base64.getEncoder().encode(encryptedMsg);//base 64 encode
		} else {
			throw new RuntimeException("Cannot Encrypt empty or NULL message ");
		}
		LOG.debug("Encrypted RSA value" + base64Encoded);
		return base64Encoded;
	}

	public PublicKey getRsaPublicKey() throws Exception {
		try {
			String rsapublickeyFile = NboConfig.getInstance().getValue("private_seema");
			FileInputStream fin = new FileInputStream(new File(rsapublickeyFile).getAbsolutePath());
			ObjectInputStream objIn = new ObjectInputStream(fin);
			return (PublicKey) objIn.readObject();
		} catch (Exception e) {
			LOG.error("Error occured while getting rsapublic key");
			throw e;
		}
	}

	public String decrypt(String ciphertext, Key publicKey) throws Exception {
		byte[] decryptedMsg = null;
		String decryptedValue = "";
		if (StringUtils.isNotBlank(ciphertext)) {
			byte[] base64Decoded = Base64.getDecoder().decode(ciphertext.trim());
			Cipher cObj = Cipher.getInstance(ALGORITHM);
			cObj.init(Cipher.DECRYPT_MODE, publicKey);
			decryptedMsg = cObj.doFinal(base64Decoded);
		} else {
			throw new RuntimeException("Cannot Decrypt empty or NULL message ");
		}
		decryptedValue = decryptedMsg.toString();
		LOG.debug("Decrypted RSA value" + decryptedValue);
		return decryptedValue;
	}

	public static void main(String[] args) throws Exception {
		PrivateKey privateKey = new WalletPayUtility().getRsaPrivateKey();
		byte[] encryptedByte = new WalletPayUtility().encrypt("12345", privateKey);
		System.out.println(encryptedByte.toString());
		PublicKey publicKey = new WalletPayUtility().getRsaPublicKey();
		String decryptedValue = new WalletPayUtility().decrypt(encryptedByte.toString(), publicKey);
		System.out.println(decryptedValue);
	}
}