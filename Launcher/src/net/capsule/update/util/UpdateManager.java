package net.capsule.update.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.json.JSONObject;

import net.capsule.Version;

public class UpdateManager {
	public static final String GITHUB_REPO_URI = "https://api.github.com/repos/Ramazanenescik04/Capsule/releases/latest";
	public static UpdateManager instance;
	
	private Version repoVersion;
	private URI latestClientZipURI;
	
	public void installAndRunUpdate(Consumer<DownloadProgress> progressConsumer, Consumer<UpdateException> crash) {
		if (updateIsAvailable()) {
			File newDownloadLocation = new File(Util.getDirectory() + "cache/" + repoVersion.toString() + "-build.zip");
			
			Thread.startVirtualThread(() -> {
				try {
					System.out.println(latestClientZipURI);
					Util.downloadFile(latestClientZipURI, newDownloadLocation, progressConsumer);
				} catch (IOException | InterruptedException e) {
					newDownloadLocation.delete();
					if (crash == null) {
						e.printStackTrace();
						return;
					}
					
					crash.accept(new UpdateException(e.getMessage(), e));
				}
			});
		}
	}
	
	public void startUpdateChecker() {
        // Başlangıçtan 5 saniye sonra çalışır, her 24 saatte bir tekrarlar
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
		    Thread t = new Thread(runnable);
		    t.setDaemon(true); // İşte sihirli dokunuş burası!
		    return t;
		});

		// Artık güvenle başlatabilirsin
		scheduler.scheduleAtFixedRate(() -> {
		    System.out.println("Güncelleme kontrol ediliyor...");
		    checkUpdate();
		}, 0, 24 * 60, TimeUnit.MINUTES);
    }
	
	public boolean updateIsAvailable() {
	    // repoVersion, eldeki versiyondan büyükse ( > 0 ) güncelleme vardır.
	    return repoVersion.compareTo(VersionChecker.getClientVersion()) > 0;
	}
	
	public void checkUpdate() {
		getRepoVersionAndDownloadURL();
		
		if (updateIsAvailable()) {
			
		}
	}
	
	public void getRepoVersionAndDownloadURL() {
		try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GITHUB_REPO_URI))
                    .header("Accept", "application/vnd.github+json")
                    // .header("Authorization", "Bearer YOUR_TOKEN") // Hız sınırı için gerekebilir
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // JSON Ayrıştırma
                JSONObject jsonResponse = new JSONObject(response.body());

                // 1. Tag ismini al
                String tagName = jsonResponse.getString("tag_name");
                this.repoVersion = new Version(tagName);

                // 2. Dosyaları (Assets) listele
                JSONArray assets = jsonResponse.getJSONArray("assets");
                
                for (int i = 0; i < assets.length(); i++) {
                    JSONObject asset = assets.getJSONObject(i);
                    String fileName = asset.getString("name");
                    String downloadUrl = asset.getString("browser_download_url");
                    
                    if (fileName.equals(/*"build.zip"*/"DikenEngine.jar")) {
                    	this.latestClientZipURI = URI.create(downloadUrl + "?t=" + System.currentTimeMillis());
                    }
                }
            } else {
                System.out.println("Hata: " + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
