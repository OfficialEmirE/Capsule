package net.capsule.studio;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;

import org.json.JSONObject;

import me.ramazanenescik04.diken.game.*;
import net.capsule.Capsule;
import net.capsule.CapsuleException;
import net.capsule.account.Account;
import net.capsule.studio.EditorUtil.EditorMode;
import net.capsule.util.PathUtil;

public class GameData {
	public final String gameName;
	public final File worldPath; // save and save-as
	
	public World theWorld; // World
	
	public Node selectedNode = null; // Default
	public EditorMode selectedTool = EditorMode.SELECT;
	
	private GameData(String gameName, File path) {this.gameName = gameName; this.worldPath = path;}
	
	public void publish(Account yourAccount, int gameID) throws Exception {
		saveWorld();
		
		Capsule.instance.getApiClient().uploadGame(gameID + "", yourAccount.getApiKey().toString(), worldPath);
	}
	
	public void publish(Account yourAccount) throws Exception {
		var createGameData = new JSONObject();
		createGameData.put("apiKey", yourAccount.getApiKey().toString());
		createGameData.put("title", this.gameName);
		createGameData.put("desc", "Auto Created Game");
		createGameData.put("image_url", "");
		
		var game = new JSONObject(Capsule.instance.getApiClient().postWebData(URI.create("http://capsule.net.tr/api/v1/games/create.php"), createGameData.toString()));
		
		if (!game.getString("status").equals("success")) {
			throw new CapsuleException("Error! API Problem: " + game.getString("message"));
		}
		
		publish(yourAccount, game.getJSONObject("game").getInt("id"));
	}
	
	public void saveWorld() throws IOException {
		var file = worldPath.getParentFile();
		if (!file.exists())
			file.mkdirs();
		
		World.saveWorld(theWorld, worldPath);
	}
	
	public static GameData createNewProject(String name, File path) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(path);
		
		if (path.isDirectory())
			throw new CapsuleException("path not Directory");
		
		GameData data = new GameData(name, path);
		data.theWorld = new World(name, 100, 100);
		
		return data;
	}
	
	public static GameData loadProject(File path) throws IOException, ReflectiveOperationException {
		Objects.requireNonNull(path);
		
		if (path.isDirectory() && !path.exists())
			throw new CapsuleException("path not Directory");
		
		GameData data = new GameData(PathUtil.getFileNameWithoutExtension(path), path);
		data.theWorld = World.loadWorld(path);
		
		return data;
	}
	
	public static GameData cloneWebProject(Account yourAccount, int gameID, File savePath) {
		Objects.requireNonNull(savePath);
		
		if (savePath.isDirectory())
			throw new CapsuleException("path not Directory");
		
		String gameJsonData;
		try {
			gameJsonData = Capsule.instance.getApiClient().getWebData(URI.create("http://capsule.net.tr/api/v1/games/?id=" + gameID));
		} catch (IOException | InterruptedException e) {
			throw new CapsuleException("Unable to fetch game metadata", e);
		}
		var game = new JSONObject(gameJsonData);
		
		if (!game.getString("status").equals("success")) {
			throw new CapsuleException("Error! API Problem: " + game.getString("message"));
		}
		
		GameData data = new GameData(game.getJSONObject("data").getString("title"), savePath);
		
		URI gameUri = URI.create("http://capsule.net.tr/api/v1/games/getdata.php?id=" + gameID + "&apiKey=" + yourAccount.getApiKey().toString());
		   
		try {
			Capsule.instance.getApiClient().downloadFile(gameUri, savePath, null);
			data.theWorld = World.loadWorld(savePath);
		} catch (Exception e) {
			throw new CapsuleException("Unable to clone web project", e);
		}
		
		return data;
	}
}
