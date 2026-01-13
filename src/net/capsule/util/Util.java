package net.capsule.util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.json.JSONObject;

import me.ramazanenescik04.diken.SystemInfo;
import net.capsule.account.Account;

public class Util {
   public static String linuxHomeDir;

   public static void findLinuxHomeDirectory() {
      String linux_home = System.getenv("HOME");
      if (linux_home == null) {
         String linux_user = System.getenv("USER");
         if (linux_user == "root") {
            linuxHomeDir = "/root";
         } else {
            linuxHomeDir = "/home/" + linux_user;
         }
      } else {
         linuxHomeDir = linux_home;
      }

   }

   public static String getWebData(URI url) {
      try {
         BufferedReader reader = new BufferedReader(new InputStreamReader(url.toURL().openStream()));
         StringBuilder sb = new StringBuilder();
         String s = "";
         
         while((s = reader.readLine()) != null) {
			sb.append(s);
		 }
       
		 return sb.toString();
	  } catch (IOException var4) {
		  var4.printStackTrace();
		 return """
		 		{
		 			"status": "error",
		 			"message": "Cannot access the URL"
		 		}
		 		""";
	  }
   }

   // SAKIN BUNDAN ÖRNEK ALMA
   public static String getFileData(String path) {
      try {
         BufferedReader reader = new BufferedReader(new InputStreamReader(Util.class.getResourceAsStream(path)));

         StringBuilder sb = new StringBuilder();
         String s = "";
         
         while((s = reader.readLine()) != null) {
			sb.append(s);
		 }

         return sb.toString();
      } catch (IOException var5) {
         var5.printStackTrace();
         return """
         		{
         			"status": "error",
		 			"message": "File not found"
		 		}
         		""";
      }
   }

   public static String getWebData(String string) {
      try {
		return getWebData(new URI(string));
	  } catch (URISyntaxException e) {
		return """
				{
					"status": "error",
					"message": """ + e.getMessage() + """
				}
				""";
	  }
   }

   private static String backslashes(String input) {
      return input.replaceAll("/", "\\\\");
   }

   public static String getConfigPath() {
      switch(SystemInfo.instance.getOS()) {
      case SystemInfo.OS.WINDOWS:
         return System.getProperty("user.home") + "/AppData/Roaming/.capsule/config.cfg".replaceAll("/", "\\\\");
      case SystemInfo.OS.MACOS:
         return String.format("~/Library/Application Support/capsule/config.cfg");
      case SystemInfo.OS.LINUX:
         return linuxHomeDir + "/.capsule/config.cfg";
      default:
         return System.getProperty("user.home") + "/AppData/Roaming/.capsule/config.cfg".replaceAll("/", "\\\\");
      }
   }

   public static String getDirectory() {
      switch(SystemInfo.instance.getOS()) {
      case SystemInfo.OS.WINDOWS:
         return backslashes(System.getProperty("user.home") + "/AppData/Roaming/.capsule/");
      case SystemInfo.OS.MACOS:
         return String.format("~/Library/Application Support/capsule/");
      case SystemInfo.OS.LINUX:
         return linuxHomeDir + "/.capsule/";
      case SystemInfo.OS.OTHER:
         System.out.println("Unsupported operating system (assuming Linux).");
         return linuxHomeDir + "/.capsule/";
      default:
         System.out.println("Unknown operating system (assuming Windows).");
         return backslashes(System.getProperty("user.home") + "/AppData/Roaming/.capsule/");
      }
   }

   public static String getDesktop() {
	   return backslashes(System.getProperty("user.home") + "/Desktop/");
   }

   public static Account login(String username, String password) {
	   String jsonData = getWebData("http://capsule.net.tr/api/v1/account/login.php?username=" + username + "&password=" + password);
	   JSONObject json = new JSONObject(jsonData);
	   if (json.getString("status").equals("success")) {
		   JSONObject userObject = json.getJSONObject("user");
		   UUID apiKey = UUID.fromString(userObject.getString("apikey"));
		   Account account = new Account(username, apiKey);
		   //account.setLogoURI(URI.create(userObject.getString("avatar")));
		   
		   return account;
	   } else {
		   System.err.println(json.getString("message"));
	   }
	   
	   return null;
   }
   
   public static BufferedImage getImageWeb(URI uri) {
	   try {
		   return ImageIO.read(uri.toURL());
	   } catch (Exception e) {
		   System.err.println("Failed to fetch image from URL: " + uri.toString());
		   return null;
	   }
   }
}
