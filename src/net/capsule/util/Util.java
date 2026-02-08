package net.capsule.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import org.json.JSONObject;

import me.ramazanenescik04.diken.SystemInfo;
import me.ramazanenescik04.diken.game.World;
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
    	  HttpClient client = HttpClient.newBuilder()
       		   .followRedirects(HttpClient.Redirect.ALWAYS) // Bu satırı ekleyin
       	       .build();

          HttpRequest request = HttpRequest.newBuilder()
       		   .uri(url)
       		   .header("User-Agent", "Capsule-UtilDownloadFile")
       		   .header("Cache-Control", "no-cache")
       		   .header("Pragma", "no-cache")
               .GET()
               .build();

          HttpResponse<String> response =
                  client.send(request, HttpResponse.BodyHandlers.ofString());
          
          if (response.statusCode() >= 400) {
        	  throw new IOException("Status code: " + response.statusCode() + " - Body" + response.body());
          }
       
          return response.body();
	  } catch (IOException | InterruptedException var4) {
		  var4.printStackTrace();
		  return """
		 		{
		 			"status": "error",
		 			"message": "%s"
		 		}
		 		""".formatted(var4.getMessage());
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
   
   public static BufferedImage getImageWeb(URL uri) {
	   try {
		   return ImageIO.read(uri);
	   } catch (Exception e) {
		   System.err.println("Failed to fetch image from URL: " + uri.toString());
		   try {
			   return ImageIO.read(net.capsule.Capsule.class.getResourceAsStream("/missingIcon.png"));
		   } catch (Exception e2) {
			   return null;
		   }
	   }
   }
   
   public static BufferedImage getImageWeb(URI uri) {
	   try {
		   return ImageIO.read(uri.toURL());
	   } catch (Exception e) {
		   System.err.println("Failed to fetch image from URL: " + uri.toString());
		   try {
			   return ImageIO.read(net.capsule.Capsule.class.getResourceAsStream("/missingIcon.png"));
		   } catch (Exception e2) {
			   return null;
		   }
	   }
   }
   
   public synchronized static void downloadFile(
           URI downloadURI,
           File outputFile,
           Consumer<DownloadProgress> progressConsumer
   ) throws IOException, InterruptedException {

       HttpClient client = HttpClient.newBuilder()
    		   .followRedirects(HttpClient.Redirect.ALWAYS) // Bu satırı ekleyin
    	       .build();

       HttpRequest request = HttpRequest.newBuilder()
    		   .uri(downloadURI)
    		   .header("User-Agent", "Capsule-UtilDownloadFile")
    		   .header("Cache-Control", "no-cache")
    		   .header("Pragma", "no-cache")
               .GET()
               .build();

       HttpResponse<InputStream> response =
               client.send(request, HttpResponse.BodyHandlers.ofInputStream());

       long contentLength = response.headers()
               .firstValueAsLong("Content-Length")
               .orElse(-1);

       Files.createDirectories(outputFile.toPath().getParent());
       
       if (response.statusCode() >= 400) {
    	   throw new IOException("status code: " + response.statusCode());
       }

       try (InputStream in = response.body();
            OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {

           byte[] buffer = new byte[8192];
           long downloaded = 0;

           long lastTime = System.nanoTime();
           long lastBytes = 0;
           int lastPercent = 0;

           int read;
           while ((read = in.read(buffer)) != -1) {
               out.write(buffer, 0, read);
               downloaded += read;

               long now = System.nanoTime();
               long deltaTime = now - lastTime;

               if (deltaTime >= 1_000_000_000L) { // 1 saniye
                   long deltaBytes = downloaded - lastBytes;
                   double speedKBps = (deltaBytes / 1024.0)
                           / (deltaTime / 1_000_000_000.0);

                   int percent = contentLength > 0
                           ? (int) ((downloaded * 100) / contentLength)
                           : -1;

                   if (percent != lastPercent) {
                       lastPercent = percent;
                       if (progressConsumer != null) {
                    	   progressConsumer.accept(
                                   new DownloadProgress("Downloading File", percent, speedKBps, false)
                           );
                       }
                   }

                   lastBytes = downloaded;
                   lastTime = now;
               }
           }
       }
       
       if (progressConsumer != null) {
    	   progressConsumer.accept(new DownloadProgress("Finished Downloaded File", 100, 0, true));
       }
   }
   
   public synchronized static World downloadGame(String apiKey, int id, Consumer<DownloadProgress> progressConsumer) {
	   URI gameUri = URI.create("http://capsule.net.tr/api/v1/games/getdata.php?id=" + id + "&apiKey=" + apiKey);
	   File tempWorldFile = new File(Util.getDirectory() + "cache/" + id + "-" + System.currentTimeMillis() + ".dew");
	   tempWorldFile.deleteOnExit();
	   
	   World theWorld = null;
	   try {
		   downloadFile(gameUri, tempWorldFile, progressConsumer);
		   
		   if (progressConsumer != null) {
	    	   progressConsumer.accept(
	                   new DownloadProgress("Importing World File" , 0, 0, false)
	           );
	       }
		   theWorld = World.loadWorld(tempWorldFile);
		   if (progressConsumer != null) {
	    	   progressConsumer.accept(
	                   new DownloadProgress("Imported World File", 100, 0, true)
	           );
	       }
	   } catch (Exception e) {
		   e.printStackTrace();
		   tempWorldFile.delete();
		   
		   if (progressConsumer != null) {
	    	   progressConsumer.accept(
	                   new DownloadProgress("Error Downloading Game: " + e.getMessage(), 0, 0, false)
	           );
	       }
	   }
	   return theWorld;
   }
   
   public static void uploadGame(
           String id,
           String apiKey,
           File dewFile
   ) throws Exception {

       String boundary = "----JavaBoundary" + UUID.randomUUID();

       HttpRequest.BodyPublisher body =
               build(boundary, id, apiKey, dewFile);

       HttpRequest request = HttpRequest.newBuilder(URI.create("http://capsule.net.tr/api/v1/games/publish.php"))
               .header("Content-Type", "multipart/form-data; boundary=" + boundary)
               .POST(body)
               .build();

       HttpClient client = HttpClient.newHttpClient();

       HttpResponse<String> response =
               client.send(request, HttpResponse.BodyHandlers.ofString());

       System.out.println("Status: " + response.statusCode());
       System.out.println("Body: " + response.body());
   }
   
   private static HttpRequest.BodyPublisher build(
           String boundary,
           String id,
           String apiKey,
           File file
   ) throws IOException {

       var byteArrayOutputStream = new ByteArrayOutputStream();
       var writer = new PrintWriter(
               new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8),
               true
       );

       // id
       writer.append("--").append(boundary).append("\r\n");
       writer.append("Content-Disposition: form-data; name=\"id\"\r\n\r\n");
       writer.append(id).append("\r\n");

       // apikey
       writer.append("--").append(boundary).append("\r\n");
       writer.append("Content-Disposition: form-data; name=\"apikey\"\r\n\r\n");
       writer.append(apiKey).append("\r\n");

       // file
       writer.append("--").append(boundary).append("\r\n");
       writer.append(
               "Content-Disposition: form-data; name=\"file\"; filename=\""
                       + file.getName() + "\"\r\n"
       );
       writer.append("Content-Type: application/octet-stream\r\n\r\n");
       writer.flush();

       Files.copy(file.toPath(), byteArrayOutputStream);
       byteArrayOutputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));

       // end
       writer.append("--").append(boundary).append("--\r\n");
       writer.flush();

       return HttpRequest.BodyPublishers.ofByteArray(
               byteArrayOutputStream.toByteArray()
       );
   }

   public static boolean IsThisGameYours(int gameID, Account account) {
	   if (gameID == -1)
		   return true;
	   
	   JSONObject gamesData = new JSONObject(Util.getWebData("http://capsule.net.tr/api/v1/games/"));
		
		if (!gamesData.getString("status").equals("success")) {
			System.err.println("Failed to fetch games data: " + gamesData.getString("message"));
			return false;
		}
		
		for (Object gameObj : gamesData.getJSONArray("data")) {
			JSONObject gameJson = (JSONObject) gameObj;
			
			int gameId = gameJson.getInt("id");
			String authorUsername = gameJson.optString("username", "Anonymouns");
			
			if (gameId == gameID && authorUsername.equals(account.getUsername()))
				return true;
		}
		return false;
   }

   public static BufferedImage scaleImage(BufferedImage img, int width, int height) {
	   BufferedImage scaledImg = new BufferedImage(width, height, img.getType());
	   Image resizedImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
	   
	   Graphics2D g = scaledImg.createGraphics();
	   g.drawImage(resizedImg, 0, 0, null);
	   g.dispose();
	   
	   return scaledImg;
   }

   public static String postWebData(URI uri, String string) throws IOException, InterruptedException {	   
	   HttpClient client = HttpClient.newHttpClient();

       HttpRequest request = HttpRequest.newBuilder()
    		   .uri(uri)
    		   .header("User-Agent", "Capsule-UtilDownloadFile")
    		   .header("Cache-Control", "no-cache")
    		   .header("Pragma", "no-cache")
               .POST(HttpRequest.BodyPublishers.ofString(string))
               .build();

       HttpResponse<String> response =
               client.send(request, HttpResponse.BodyHandlers.ofString());
	   return response.body();
   }
}

