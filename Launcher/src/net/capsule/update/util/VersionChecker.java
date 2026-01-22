package net.capsule.update.util;

import net.capsule.Version;

public class VersionChecker {
	private static final String CAPSULE_PATH = "";
	private static Version clientVersion;
	
	public static void checkClientVersion() {
		
	}
	
	public static Version getClientVersion() {
		return new Version(new String(clientVersion.toString()));
	}
}
