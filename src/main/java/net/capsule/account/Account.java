package net.capsule.account;

import java.util.UUID;
import java.util.prefs.Preferences;

import me.ramazanenescik04.diken.log.ConsoleLog;

public record Account(String username, UUID apiKey) {
	public String getUsername() {
		return username;
	}
	
	public UUID getApiKey() {
		return apiKey;
	}
	
	public void logoff() {
		try {
			Preferences prefs = Preferences.userRoot().node("CapsuleApp-Account");
			prefs.clear();
			prefs.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Account getAccountLocalFile() {
		try {
			Preferences prefs = Preferences.userRoot().node("CapsuleApp-Account");

	        String key = prefs.get("api_key", null);
	        String username = prefs.get("username", null);
	        
	        if (key == null || username == null) {
	            return null; // Hesap bulunamadı
	        }
	        
	        return new Account(username, UUID.fromString(key));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void saveAccountLocalFile() {
		try {
			Preferences prefs = Preferences.userRoot().node("CapsuleApp-Account");

	        // Kayıt et
	        prefs.put("api_key", apiKey.toString());
	        prefs.put("username", username);
	        prefs.flush(); // diske yazar
	        System.out.println("[Capsule] Saved Account Data");
	        ConsoleLog.sendLog("[Capsule] Saved Account Data");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
