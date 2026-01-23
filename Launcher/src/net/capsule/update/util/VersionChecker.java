package net.capsule.update.util;

import java.io.*;

import net.capsule.Version;

public class VersionChecker {
	private static final String CAPSULE_PATH = Util.getDirectory() + "jars/";
	private static final String USING_VERSION_FILE = Util.getDirectory() + "version/using.dat";
	private static Version clientVersion = new Version("0.0.0");
	
	public static Version getClientVersion() {
		return new Version(new String(clientVersion.toString()));
	}
	
	public static void saveUsingLatestVersion() throws IOException {
		File using = new File(USING_VERSION_FILE);
		
		if (!using.exists()) {
			using.mkdir();
		}
		
		DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(using));
		outputStream.writeUTF(clientVersion.toString());
		outputStream.close();
	}
	
	static {
		File file = new File(CAPSULE_PATH);
		
		if (!file.exists())
			file.mkdir();
		
		File using = new File(USING_VERSION_FILE);
		
		if (!using.exists()) {
			using.mkdir();
			
			try {
				saveUsingLatestVersion();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				DataInputStream outputStream = new DataInputStream(new FileInputStream(using));
				var version = outputStream.readUTF();
				outputStream.close();
				
				clientVersion = new Version(version);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
