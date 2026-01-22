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
	    return repoVersion.compareTo(Capsule.version) > 0;
	}
	
	public void checkUpdate() {
		getRepoVersionAndDownloadURL();
		
		if (updateIsAvailable()) {
			int e = OptionWindow.showMessage("Update available! Would you like to update?\n" + Capsule.version.toString() + " -> " + repoVersion.toString(), "Update available", OptionWindow.PLAIN_MESSAGE, OptionWindow.YES_NO_OPTION);
			
			if (e == OptionWindow.YES_BUTTON) {
				OptionWindow proccessWindow = new OptionWindow("Downloading Update\nPlease Wait.", "Downloading Update", OptionWindow.INFO_MESSAGE, OptionWindow.OK_BUTTON);
				proccessWindow.getContentPane().getCompoments().forEach(btn -> btn.setActive(false));
				ProgressBar bar = new ProgressBar(2, 18, proccessWindow.width - 2, 16);
				bar.value = 0;
				proccessWindow.getContentPane().add(bar);
				proccessWindow.setCloseable(false);
				proccessWindow.resized();
				
				proccessWindow.setLocation(
					(DikenEngine.getEngine().getWidth() / 2 - proccessWindow.getWidth() / 2) ,
					(DikenEngine.getEngine().getHeight()  / 2 - proccessWindow.getHeight()  / 2)
				);
				
				DikenEngine.getEngine().wManager.addWindow(proccessWindow);
				
				//İndir!
				installAndRunUpdate((dp) -> {
					bar.text = dp.progressName() + " - " + dp.toString();
					bar.value = dp.percent();
					
					if (dp.isFinished()) {
						proccessWindow.getContentPane().getCompoments().forEach(btn -> btn.setActive(true));
						proccessWindow.setCloseable(true);
						
						while (!proccessWindow.closed) {
							try {
								Thread.sleep(1);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
						}
						OptionWindow.showMessageNoWait("Yay :3", ":3", OptionWindow.INFO_MESSAGE, OptionWindow.OK_BUTTON, null);
					}
				}, (crash) ->  {
					crash.printStackTrace();
					proccessWindow.close();
						
					OptionWindow.showMessageNoWait("Updating Error: " + crash.getMessage(), "Error", OptionWindow.ERROR_MESSAGE, OptionWindow.OK_BUTTON, null);
				});
			}
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
