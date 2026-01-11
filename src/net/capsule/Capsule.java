package net.capsule;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.JOptionPane;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.SystemInfo;
import me.ramazanenescik04.diken.game.Animation;
import me.ramazanenescik04.diken.game.Config;
import me.ramazanenescik04.diken.gui.window.OptionWindow;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.IOResource;
import me.ramazanenescik04.diken.resource.IResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import net.capsule.account.Account;
import net.capsule.gui.GameSelectionScreen;
import net.capsule.gui.LoginScreen;
import net.capsule.util.Util;

public class Capsule {
	public Account account;
	
	//Oyun Motorları
	public DikenEngine gameEngine;
	
	public static Capsule instance;
	
	public Capsule() {		
		this.gameEngine = new DikenEngine(null, 320 * 2, 240 * 2, 2);
		this.gameEngine.setTitle("Capsule");
		try {
			this.gameEngine.setIcon((Bitmap) IOResource.loadResource(URI.create("http://capsule.net.tr/favicon.png").toURL().openStream(), EnumResource.IMAGE));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {}
		this.gameEngine.addOnCloseRunnable(() -> {
			System.exit(0);
		});
		this.gameEngine.start();
	}
	
	public void close() {
		this.gameEngine.close();
	}
	
	public static void main(String[] args) {
		loadResources();
		
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
			if (!inst_dir.exists()) {
				inst_dir.mkdir();
			}
			
			if (!log_dir.exists()) {
				log_dir.mkdir();
			}
			
			if (!game_dir.exists()) {
				game_dir.mkdir();
			}
			
			Config.defaultConfigFile = new File(Util.getDirectory(), "config.dat");
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
				JOptionPane.showMessageDialog(null, "Unable to Log In to Your Account! Login Dialog Opens");
			}
			instance.account = account_1;
		}
		
		Capsule.instance.gameEngine.wManager.addWindow(new OptionWindow("Welcome to Capsule!", "Welcome", null));
		
		if (instance.account == null) {
			
			//TODO burası yeniden yazılacak
			LoginScreen loginScreen = new LoginScreen();
			Capsule.instance.gameEngine.setCurrentScreen(loginScreen);
			
			Future<Account> accountFuture = loginScreen.getAccount();
			while (!accountFuture.isDone()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			try {
				Account account_ = accountFuture.get();
				if (account_ == null) {
					JOptionPane.showMessageDialog(null, "Your Account Password and Username Are Incorrect!");
					System.exit(0);
				}
				
				Capsule.instance.gameEngine.setCurrentScreen(null);
				instance.account = account_;
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} finally {
				if (instance.account == null) {
					System.exit(0);
				}	
			}
		}
		
		instance.account.saveAccountLocalFile();
		
		if (argMap.containsKey("studio") || argMap.containsKey("s")) {
			
		} else {
			if (argMap.containsKey("game")) {
				
			}
			Capsule.instance.gameEngine.setCurrentScreen(new GameSelectionScreen());
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
		
		ResourceLocator.addResource(new ResourceLocator.ResourceKey("capsule", "leftWalkAnim"), (IResource)leftWalkAnim);
		ResourceLocator.addResource(new ResourceLocator.ResourceKey("capsule", "rightWalkAnim"), (IResource)rightWalkAnim);
		
		Bitmap[][] def_tiles = IOResource.loadResourceAndCut(DikenEngine.class.getResourceAsStream("/def_tiles.png"), 32, 32);
		ArrayBitmap tiles = new ArrayBitmap(new Bitmap[0][]);
		tiles.bitmap = def_tiles;
		ResourceLocator.addResource(new ResourceLocator.ResourceKey("capsule", "default_tiles"), (IResource)tiles);
		
		ArrayBitmap menu_buttons = new ArrayBitmap(IOResource.loadResourceAndCut(Capsule.class.getResourceAsStream("/menubuttons.png"), 16, 16));
		ResourceLocator.addResource(new ResourceLocator.ResourceKey("capsule", "menu_buttons"), (IResource)menu_buttons);
	}
	
	public static Map<String, String> parseArgs(String[] args) {
        Map<String, String> options = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.startsWith("--")) {
                // --key=value veya --key value
                String key = arg.substring(2);
                String value = "true";

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
                String value = "true";

                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    value = args[++i];
                }

                options.put(key, value);
            }
        }

        return options;
    }
	
	static {
		Bitmap capsuleLogo = (Bitmap) IOResource.loadResource(Capsule.class.getResourceAsStream("/title.png"), EnumResource.IMAGE);
		ResourceLocator.addResource(new ResourceLocator.ResourceKey("capsule", "logo"), capsuleLogo);
	}
}
