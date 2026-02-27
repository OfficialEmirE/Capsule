package net.capsule.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;
import java.util.function.Consumer;

import org.json.JSONObject;

import me.ramazanenescik04.diken.game.World;
import net.capsule.CapsuleException;
import net.capsule.account.Account;

public class CapsuleApiClient {
    private final StoragePaths storagePaths;

    public CapsuleApiClient(StoragePaths storagePaths) {
        this.storagePaths = storagePaths;
    }

    public String getWebData(URI url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();

        HttpRequest request = HttpRequest.newBuilder().uri(url).header("User-Agent", "Capsule-UtilDownloadFile")
                .header("Cache-Control", "no-cache").header("Pragma", "no-cache").GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new IOException("Status code: " + response.statusCode() + " - Body" + response.body());
        }

        return response.body();
    }

    public String getWebData(String string) {
        try {
            return getWebData(new URI(string));
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new CapsuleException("Failed to fetch web data", e);
        }
    }

    public String postWebData(URI uri, String body) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder().uri(uri).header("User-Agent", "Capsule-UtilDownloadFile")
                .header("Cache-Control", "no-cache").header("Pragma", "no-cache")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public Account login(UUID apiKey) throws IOException, InterruptedException {
        String jsonData = postWebData(URI.create("http://capsule.net.tr/api/v1/account/valid.php"),
                """
                           {"apiKey":"%s"}
                           """.formatted(apiKey));

        var json = new JSONObject(jsonData);
        if (json.getString("status").equals("success")) {
            JSONObject userObject = json.getJSONObject("user");
            if (!userObject.getBoolean("isValid")) {
                return null;
            }
            return new Account(userObject.getString("username"), apiKey);
        }
        return null;
    }

    public synchronized void downloadFile(URI downloadURI, File outputFile, Consumer<DownloadProgress> progressConsumer)
            throws IOException, InterruptedException {

        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();

        HttpRequest request = HttpRequest.newBuilder().uri(downloadURI).header("User-Agent", "Capsule-UtilDownloadFile")
                .header("Cache-Control", "no-cache").header("Pragma", "no-cache").GET().build();

        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        long contentLength = response.headers().firstValueAsLong("Content-Length").orElse(-1);

        Files.createDirectories(outputFile.toPath().getParent());

        if (response.statusCode() >= 400) {
            throw new IOException("status code: " + response.statusCode());
        }

        try (InputStream in = response.body(); OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {

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

                if (deltaTime >= 1_000_000_000L) {
                    long deltaBytes = downloaded - lastBytes;
                    double speedKBps = (deltaBytes / 1024.0) / (deltaTime / 1_000_000_000.0);

                    int percent = contentLength > 0 ? (int) ((downloaded * 100) / contentLength) : -1;

                    if (percent != lastPercent) {
                        lastPercent = percent;
                        if (progressConsumer != null) {
                            progressConsumer.accept(new DownloadProgress("Downloading File", percent, speedKBps, false));
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

    public synchronized World downloadGame(String apiKey, int id, Consumer<DownloadProgress> progressConsumer) {
        URI gameUri = URI.create("http://capsule.net.tr/api/v1/games/getdata.php?id=" + id + "&apiKey=" + apiKey);
        File tempWorldFile = storagePaths.getCachePath().resolve(id + "-" + System.currentTimeMillis() + ".dew").toFile();
        tempWorldFile.deleteOnExit();

        try {
            downloadFile(gameUri, tempWorldFile, progressConsumer);

            if (progressConsumer != null) {
                progressConsumer.accept(new DownloadProgress("Importing World File", 0, 0, false));
            }
            World theWorld = World.loadWorld(tempWorldFile);
            if (progressConsumer != null) {
                progressConsumer.accept(new DownloadProgress("Imported World File", 100, 0, true));
            }
            return theWorld;
        } catch (Exception e) {
            tempWorldFile.delete();
            throw new CapsuleException("Error downloading game", e);
        }
    }

    public void uploadGame(String id, String apiKey, File dewFile) throws IOException, InterruptedException {
        String boundary = "----JavaBoundary" + UUID.randomUUID();

        HttpRequest.BodyPublisher body = buildMultipartBody(boundary, id, apiKey, dewFile);

        HttpRequest request = HttpRequest.newBuilder(URI.create("http://capsule.net.tr/api/v1/games/publish.php"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary).POST(body).build();

        HttpClient client = HttpClient.newHttpClient();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest.BodyPublisher buildMultipartBody(String boundary, String id, String apiKey, File file)
            throws IOException {

        var byteArrayOutputStream = new ByteArrayOutputStream();
        var writer = new PrintWriter(new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8), true);

        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"id\"\r\n\r\n");
        writer.append(id).append("\r\n");

        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"apikey\"\r\n\r\n");
        writer.append(apiKey).append("\r\n");

        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName())
                .append("\"\r\n");
        writer.append("Content-Type: application/octet-stream\r\n\r\n");
        writer.flush();

        Files.copy(file.toPath(), byteArrayOutputStream);
        byteArrayOutputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));

        writer.append("--").append(boundary).append("--\r\n");
        writer.flush();

        return HttpRequest.BodyPublishers.ofByteArray(byteArrayOutputStream.toByteArray());
    }

    public boolean isThisGameYours(int gameID, Account account) throws IOException, InterruptedException {
        if (gameID == -1) {
            return true;
        }

        JSONObject gamesData = new JSONObject(getWebData("http://capsule.net.tr/api/v1/games/"));

        if (!gamesData.getString("status").equals("success")) {
            throw new CapsuleException("Failed to fetch games data: " + gamesData.getString("message"));
        }

        for (Object gameObj : gamesData.getJSONArray("data")) {
            JSONObject gameJson = (JSONObject) gameObj;

            int gameId = gameJson.getInt("id");
            String authorUsername = gameJson.optString("username", "Anonymouns");

            if (gameId == gameID && authorUsername.equals(account.getUsername())) {
                return true;
            }
        }
        return false;
    }
}
