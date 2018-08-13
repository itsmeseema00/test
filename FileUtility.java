package org.rbfcu.netbranch.common.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.rbfcu.netbranch.common.web.NboConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a helper class to read files directly off the file system. Files are cached for performance.
 */
public final class FileUtility {

	private static final Logger LOG = LoggerFactory.getLogger(FileUtility.class);
	private static Map<String, byte[]> fileMap = new HashMap<String, byte[]>();

	static {
		reloadFiles();
	}

	private FileUtility() {
	}

	/**
	 * Loads files off the file system and caches them for lookup.
	 */
	public static void reloadFiles() {
		// TODO - these represent parallel arrays which is a bad practice
		String[] filePaths = NboConfig.getInstance().getValueAsArray("loadedFiles.paths");
		String[] fileNamesLogical = NboConfig.getInstance().getValueAsArray("loadedFiles.logicalNames");
		if (null != filePaths && null != fileNamesLogical && filePaths.length == fileNamesLogical.length) {
			fileMap.clear();
			for (int ctr = 0; ctr < filePaths.length; ctr++) {
				try {
					byte[] fileData = FileUtils.readFileToByteArray(new File(filePaths[ctr]));
					fileMap.put(fileNamesLogical[ctr], fileData);
					LOG.debug("Loaded file {}", fileNamesLogical[ctr]);
				} catch (Exception ex) {
					LOG.error("Error reading file", ex);
				}
			}
		} else {
			LOG.error("invalid config entry for either loadedFiles.paths or loadedFiles.logicalNames");
		}
	}

	public static byte[] getFileData(String logicalName) {
		return fileMap.get(logicalName);
	}

	public static PrivateKey getRsaPrivateKey() throws Exception {
		try {
			String rsaprivatekeyFile = NboConfig.getInstance().getValue("rsaprivate1024.key.file.path");
			FileInputStream fin = new FileInputStream(new File(rsaprivatekeyFile).getAbsolutePath());
			ObjectInputStream objIn = new ObjectInputStream(fin);
			return (PrivateKey) objIn.readObject();
		} catch (Exception e) {
			LOG.error("Error occured while getting rsaprivate1024 key");
			throw e;
		}
	}

	public static PublicKey getRsaPublicKey() throws Exception {
		try {
			String rsapublickeyFile = NboConfig.getInstance().getValue("rsapublic1024.key.file.path");
			FileInputStream fin = new FileInputStream(new File(rsapublickeyFile).getAbsolutePath());
			ObjectInputStream objIn = new ObjectInputStream(fin);
			return (PublicKey) objIn.readObject();
		} catch (Exception e) {
			LOG.error("Error occured while getting rsapublic1024 key");
			throw e;
		}
	}
}