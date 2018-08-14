package org.rbfcu.projectview.bean;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSAExample {
	private static final String private_key= "private.key";
	private static final String public_key= "public.key";

	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		KeyPair keyPair= keyPairGenerator.generateKeyPair();
		PublicKey publicKey= keyPair.getPublic();
		PrivateKey privateKey= keyPair.getPrivate();
		KeyFactory keyFactory=KeyFactory.getInstance("RSA");
		RSAPublicKeySpec rsaPublicKeySpec=keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);
		RSAPrivateKeySpec rsaPrivateKeySpec= keyFactory.getKeySpec(privateKey, RSAPrivateKeySpec.class);
		RSAExample rsaExample= new RSAExample();
		rsaExample.saveKeys(public_key,rsaPublicKeySpec.getModulus(),rsaPublicKeySpec.getPublicExponent());
		rsaExample.saveKeys(private_key,rsaPrivateKeySpec.getModulus(),rsaPrivateKeySpec.getPrivateExponent());
		
		byte[] encryptedData=rsaExample.encrypt("123456");
		rsaExample.decrypt(encryptedData);		
		
	}

	
	private byte[] encrypt(String data) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] dataToEncrypt= data.getBytes();
		byte[] encryptedData= null;
		
		PublicKey publicKey= readPublicKeyFromFile(this.public_key);
		Cipher cipher= Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		encryptedData= cipher.doFinal(dataToEncrypt);
		System.out.println("Encrypted Date: " +encryptedData);
		return encryptedData;
	}
	
	private byte[] decrypt(byte[] data) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] decryptedData= null;
		
		PrivateKey privateKey= readPrivateKeyFromFile(this.private_key);
		Cipher cipher= Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		decryptedData= cipher.doFinal(data);
		System.out.println("Decrypted Date: " +new String(decryptedData));
		
		return decryptedData;
	}

	private PrivateKey readPrivateKeyFromFile(String data) throws IOException {
		FileInputStream fis=null;
		ObjectInputStream ois=null;
		try{		
			fis = new FileInputStream(new File(data));
			ois = new ObjectInputStream(fis);
			BigInteger modulus= (BigInteger) ois.readObject();
			BigInteger publicExponent= (BigInteger) ois.readObject();
			RSAPrivateKeySpec rsaPrivateKeySpec= new RSAPrivateKeySpec(modulus, publicExponent);
			KeyFactory keyFactory=KeyFactory.getInstance("RSA");
			PrivateKey privateKey= keyFactory.generatePrivate(rsaPrivateKeySpec);
			return privateKey;
		}catch(Exception e){
			e.printStackTrace();
			}finally{
				if(ois != null){
					ois.close();
				if(fis != null){
					fis.close();
					}
				}
			}
		return null;
	}
	
	private PublicKey readPublicKeyFromFile(String data) throws IOException {
		FileInputStream fis=null;
		ObjectInputStream ois=null;
		try{		
			fis = new FileInputStream(new File(data));
			ois = new ObjectInputStream(fis);
			BigInteger modulus= (BigInteger) ois.readObject();
			BigInteger publicExponent= (BigInteger) ois.readObject();
			RSAPublicKeySpec rsaPublicKeySpec= new RSAPublicKeySpec(modulus, publicExponent);
			KeyFactory keyFactory=KeyFactory.getInstance("RSA");
			PublicKey publicKey= keyFactory.generatePublic(rsaPublicKeySpec);
			return publicKey;
		}catch(Exception e){
			e.printStackTrace();
			}finally{
				if(ois != null){
					ois.close();
				if(fis != null){
					fis.close();
				}
				}
			}
		return null;
	}

	private void saveKeys(String fileName, BigInteger modulus, BigInteger publicExponent) throws IOException {
		FileOutputStream fos=null;
		ObjectOutputStream oos=null;
		try{		
		fos = new FileOutputStream(fileName);
		oos = new ObjectOutputStream(new BufferedOutputStream(fos));
		oos.writeObject(modulus);
		oos.writeObject(publicExponent);
		}catch(Exception e){
			e.printStackTrace();
			}finally{
				if(oos != null){
					oos.close();
				if(fos != null){
					fos.close();
				}
				}
			}
		
	}

}
