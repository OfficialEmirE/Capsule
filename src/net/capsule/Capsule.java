package net.capsule;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.json.JSONObject;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.SystemInfo;
import me.ramazanenescik04.diken.game.Animation;
import me.ramazanenescik04.diken.game.Config;
import me.ramazanenescik04.diken.gui.window.OptionWindow;
import me.ramazanenescik04.diken.log.ConsoleLog;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.IOResource;
import me.ramazanenescik04.diken.resource.IResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import net.capsule.account.Account;
import net.capsule.gui.GameSelectionScreen;
import net.capsule.gui.LoginScreen;
import net.capsule.studio.*;
import net.capsule.util.Util;

public class Capsule {
	public static final Version version = new Version("0.2.1");
	public static Capsule instance;
	
	public Account account;
	public DikenEngine gameEngine;
	
	public Capsule() {				
		this.gameEngine = new DikenEngine(null, 320 * 2, 240 * 2, 2);
		this.gameEngine.setTitle("Capsule");
		try {
			Bitmap icon = (Bitmap) IOResource.loadResource(URI.create("http://capsule.net.tr/favicon.png").toURL().openStream(), EnumResource.IMAGE);
			this.gameEngine.setIcon(icon.resize(32, 32), icon.resize(16, 16));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {}
		this.gameEngine.setResizable(true);
		this.gameEngine.start();
		this.gameEngine.addOnCloseRunnable(() -> {
			if (this.account != null) {
				this.account.saveAccountLocalFile();
			}
		});
		
		checkUpdate();
	}
	
	public void close() {
		this.gameEngine.close();
		
		if (this.account != null) {
			this.account.saveAccountLocalFile();
		}
	}
	
	public static void main(String[] args) {
		try {
			if (SystemInfo.instance.getOS() == SystemInfo.OS.LINUX) {
			   Util.findLinuxHomeDirectory();
			}
			
			String install_directory = Util.getDirectory();
			String log_directory = Util.getDirectory() + "logs/";
			String game_installed_directory = Util.getDirectory() + "cache/";
			File inst_dir = new File(install_directory);
			File log_dir = new File(log_directory);
			File game_dir = new File(game_installed_directory);
			File versions_dir = new File(Util.getDirectory() + "versions/");
			if (!inst_dir.exists()) {
				inst_dir.mkdir();
			}
			
			if (!log_dir.exists()) {
				log_dir.mkdir();
			}
			
			if (!game_dir.exists()) {
				game_dir.mkdir();
			}
			
			if (!versions_dir.exists()) {
				versions_dir.mkdir();
			}
			
			Config.defaultConfigFile = new File(Util.getDirectory(), "config.dat");
			ConsoleLog.setLogDirectory(log_dir);
		} catch (Exception var10) {
			var10.printStackTrace();
			System.exit(1);
		}
		
		instance = new Capsule();
		
		Map<String, String> argMap = parseArgs(args);
		
		for (String key : argMap.keySet()) {
			System.out.println("Arg: " + key + " Value: " + argMap.get(key));
		}
		
		instance.account = Account.getAccountLocalFile();
		
		if (argMap.containsKey("login")) {
			String account = argMap.get("login");
			
			System.out.println("Logging in with account: " + account);
			
			String username = account.split(":")[0];
			String password = account.split(":")[1];
			
			Account account_1 = Util.login(username, password);
			if (account_1 == null) {
				OptionWindow.showMessage("Unable to Log In to Your Account! Login Dialog Opens", "Login Error", OptionWindow.ERROR_MESSAGE, OptionWindow.OK_BUTTON);
			}
			instance.account = account_1;
		}
		
		if (instance.account == null) {
			LoginScreen loginScreen = new LoginScreen();
			Capsule.instance.gameEngine.setCurrentScreen(loginScreen);
			
			Future<Account> accountFuture = loginScreen.getAccount();
			while (!(accountFuture.isDone() || accountFuture.isCancelled())) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if (accountFuture.isCancelled()) {
				instance.gameEngine.close();
			}
			
			try {
				Account account_ = accountFuture.get();
				if (account_ == null) {
					OptionWindow.showMessage("Your Account Password and Username Are Incorrect!", "Error", OptionWindow.ERROR_MESSAGE, OptionWindow.OK_BUTTON);
					instance.gameEngine.close();
					System.exit(0);
				}
				
				Capsule.instance.gameEngine.setCurrentScreen(null);
				instance.account = account_;
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}  finally {
				if (instance.account == null) {
					instance.gameEngine.close();
					System.exit(0);
				}	
			}
		}
		
		instance.account.saveAccountLocalFile();
		
		if (argMap.containsKey("studio") || argMap.containsKey("s")) {
			int gameID = -1;
			String value = argMap.get("studio");
			
			if (!value.isEmpty()) {
				gameID = Integer.parseInt(value);
			}
			
			Capsule.instance.gameEngine.setCurrentScreen(new WorldEditor(gameID));
		} else {
			if (argMap.containsKey("game")) {
				String id = argMap.get("game");
				Capsule.instance.gameEngine.setCurrentScreen(new GameLoadingScreen(Integer.parseInt(id)));
			} else {
				Capsule.instance.gameEngine.setCurrentScreen(new GameSelectionScreen());
			}	
		}
	}
	
	public static void loadResources() {
		Bitmap def_body = (Bitmap)IOResource.loadResource(Capsule.class.getResourceAsStream("/default_c3/body.png"), EnumResource.IMAGE);
		Bitmap def_hand = (Bitmap)IOResource.loadResource(Capsule.class.getResourceAsStream("/default_c3/hand.png"), EnumResource.IMAGE);
		Bitmap def_face = (Bitmap)IOResource.loadResource(Capsule.class.getResourceAsStream("/default_c3/face.png"), EnumResource.IMAGE);
		
		ArrayBitmap def_avatar = new ArrayBitmap(new Bitmap[][] { { def_body, def_hand, def_face } });
		ResourceLocator.addResource(new ResourceLocator.ResourceKey("capsule", "default_avatar"), (IResource)def_avatar);
		
		Animation leftWalkAnim = (Animation)IOResource.loadResource(Capsule.class.getResourceAsStream("/default_c3/animation/walkanim-left.bin"), 
		    EnumResource.ANIMATION);
		Animation rightWalkAnim = (Animation)IOResource.loadResource(Capsule.class.getResourceAsStream("/default_c3/animation/walkanim-right.bin"), 
		    EnumResource.ANIMATION);
		
		Animation idleAnim = (Animation)IOResource.loadResource(Capsule.class.getResourceAsStream("/default_c3/animation/idleanim.bin"), 
			EnumResource.ANIMATION);
		
		ResourceLocator.addResource(new ResourceLocator.ResourceKey("capsule", "leftWalkAnim"), (IResource)leftWalkAnim);
		ResourceLocator.addResource(new ResourceLocator.ResourceKey("capsule", "rightWalkAnim"), (IResource)rightWalkAnim);
		ResourceLocator.addResource(new ResourceLocator.ResourceKey("capsule", "idleAnim"), (IResource)idleAnim);
		
		ArrayBitmap menu_buttons = new ArrayBitmap(IOResource.loadResourceAndCut(Capsule.class.getResourceAsStream("/menubuttons.png"), 16, 16));
		ResourceLocator.addResource(new ResourceLocator.ResourceKey("capsule", "menu_buttons"), (IResource)menu_buttons);
		
		ArrayBitmap materials = new ArrayBitmap(IOResource.loadResourceAndCut(Capsule.class.getResourceAsStream("/materials.png"), 16, 16));
		ResourceLocator.addResource(new ResourceLocator.ResourceKey("capsule", "materials"), (IResource)materials);
	}
	
	public static Map<String, String> parseArgs(String[] args) {
        Map<String, String> options = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.startsWith("--")) {
                // --key=value veya --key value
                String key = arg.substring(2);
                String value = "";

                if (key.contains("=")) {
                    String[] parts = key.split("=", 2);
                    key = parts[0];
                    value = parts[1];
                } else if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    value = args[++i]; // sonraki argüman value oluyor
                }

                options.put(key, value);

            } else if (arg.startsWith("-")) {
                // -a veya -a value
                String key = arg.substring(1);
                String value = "";

                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    value = args[++i];
                }

                options.put(key, value);
            }
        }

        return options;
    }
	
	private void checkUpdate() {
		var repoVersion = Capsule.version;
		try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://capsule.net.tr/api/v1/assets/check_update.php?name=capsule"))
                    .header("Accept", "application/vnd.github+json")
                    // .header("Authorization", "Bearer YOUR_TOKEN") // Hız sınırı için gerekebilir
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // JSON Ayrıştırma
                JSONObject jsonResponse = new JSONObject(response.body());

                // 1. Tag ismini al
                String tagName = jsonResponse.getString("tag_name");
                repoVersion = new Version(tagName);
            } else {
                System.out.println("Hata: " + response.statusCode());
                ConsoleLog.sendLog("Error: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            ConsoleLog.sendLog("Error: " + e.getMessage());
        }
		
		if (repoVersion.compareTo(Capsule.version) > 0) {
			OptionWindow.showMessageNoWait("Update Available! Please restart Capsule.\n" + Capsule.version + " -> " + repoVersion, "Warning", OptionWindow.WARNING_MESSAGE, 0, null);
		}
	}
	
	static {
		Bitmap capsuleLogo = (Bitmap) IOResource.loadResource(Capsule.class.getResourceAsStream("/title.png"), EnumResource.IMAGE);
		ResourceLocator.addResource(new ResourceLocator.ResourceKey("capsule", "logo"), capsuleLogo);
		
		loadResources();
	}
}
