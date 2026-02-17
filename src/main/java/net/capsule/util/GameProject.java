package net.capsule.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.json.JSONException;
import org.json.JSONObject;

import me.ramazanenescik04.diken.game.World;
import net.capsule.account.Account;

public record GameProject(String projectName, String desc, File projectPath) {
	
	public static GameProject loadProject(File projectFile) throws JSONException, IOException {
		var data = new JSONObject(Files.readString(new File(projectFile, "/setting.json").toPath()));
		
		var name = data.getString("projectName");
		var desc = data.getString("desc");
		
		return new GameProject(name, desc, projectFile);
	}
	
	public void saveProject() throws IOException {
		var data = new JSONObject();
		data.put("projectName", projectName);
		data.put("desc", desc);
		
		var projectSetting = new File(projectPath, "/setting.json");
		Files.writeString(projectSetting.toPath(), data.toString(), StandardOpenOption.CREATE);
	}
	
	public World createNewWorld() {
		return new World(projectName, 100, 100);
	}
	
	public File getBuildFile() {
		var file = new File(projectPath, "/builds");
		
		if (!file.exists())
			file.mkdirs();
		
		return file;
	}
	
	public File getScriptFile() {
		var file = new File(projectPath, "/scripts");
		
		if (!file.exists())
			file.mkdirs();
		
		return file;
	}
	
	public File exportWorld(World world) {
		File worldFile = new File(this.getBuildFile(), projectName + ".dew");
		try {
			World.saveWorld(null, worldFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return worldFile;
	}
	
	public void publishExistGame(Account publishAccount, World world, int id) throws Exception {
		Util.uploadGame(id + "", publishAccount.getApiKey().toString(), exportWorld(world));
	}
	
	public void publishNonExistGame(Account publishAccount, World world) throws JSONException, Exception {
		JSONObject publishData = new JSONObject();
		publishData.put("apiKey", publishAccount.getApiKey().toString());
		publishData.put("title", projectName);
		publishData.put("desc", desc);
		publishData.put("image_url", "https://placehold.co/600x400.png");
		
		JSONObject bodyObject = null;
		try {
			bodyObject = new JSONObject(Util.postWebData(URI.create("http://capsule.net.tr/api/v1/games/create.php"), publishData.toString()));
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		if (bodyObject != null && bodyObject.getString("status").equals("success")) {
			var game = bodyObject.getJSONObject("game");
			
			publishExistGame(publishAccount, world, game.getInt("id"));
		}
	}
}
