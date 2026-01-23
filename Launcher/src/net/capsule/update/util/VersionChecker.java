package net.capsule.update.util;

import java.io.File;

import net.capsule.Version;

public class VersionChecker {
	private static final String CAPSULE_PATH = Util.getDirectory() + "jars/";
	private static Version clientVersion = new Version("0.1.0");
	
	public static Version getClientVersion() {
		return new Version(new String(clientVersion.toString()));
	}
	
	static {
		File file = new File(CAPSULE_PATH);
		
		if (!file.exists())
			file.mkdir();
	}
}
