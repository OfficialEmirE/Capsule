package net.capsule;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.json.JSONObject;

import me.ramazanenescik04.diken.DikenEngine;
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
import net.capsule.gui.DockView;
import net.capsule.gui.GameSelectionScreen;
import net.capsule.gui.LoginScreen;
import net.capsule.studio.*;
import net.capsule.util.Util;

public class Capsule {
	public static final Version version = new Version("0.4.0");
	public static Capsule instance;
	
	public Account account;
	public DikenEngine gameEngine;
	public JFrame gameFrame;
	
	public Capsule() {				
		account = Account.getAccountLocalFile();
		
		gameEngine = new DikenEngine(320 * 4, 240 * 4, 2);
		
		gameFrame = new JFrame("Capsule");
		gameFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		gameFrame.add(gameEngine);
		try {
			Bitmap icon = (Bitmap) IOResource.loadResource(URI.create("http://capsule.net.tr/favicon.png").toURL().openStream(), EnumResource.IMAGE);
			gameFrame.setIconImage(Toolkit.getDefaultToolkit().createImage(icon.toBytes("png")));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {}
		gameFrame.pack();
		gameFrame.setLocationRelativeTo(null);
		gameFrame.setVisible(true);
		
		gameFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				close();
			}
		});
		
		gameEngine.start();
		
		Thread.startVirtualThread(() -> checkUpdate());
	}
	
	public void close() {
		this.gameEngine.stop();
		
		if (this.account != null) {
			this.account.saveAccountLocalFile();
		}

		this.gameFrame.dispose();
	}
	
	public static void main(String[] args) {
		log("Starting Capsule " + version);
		try {
			if (SystemInfo.instance.getOS() == SystemInfo.OS.LINUX) {
			   Util.findLinuxHomeDirectory();
			}

			File inst_dir = new File(Util.getDirectory());
			File log_dir = new File(Util.getDirectory() + "logs/");
			File game_dir = new File(Util.getDirectory() + "cache/");
			File versions_dir = new File(Util.getDirectory() + "versions/");
			File projects_dir = new File(Util.getDirectory() + "projects/");
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
			
			if (!projects_dir.exists()) {
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
		
		if (argMap.containsKey("apikey")) {
			String apikey = argMap.get("apikey");
			
			Account account_1 = null;
			try {
				account_1 = Util.login(UUID.fromString(apikey));
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
			
			if (account_1 == null) {
				OptionWindow.showMessage("API_KEY geçerli değil. Lütfen Geçerli bir API_KEY gir", "Error", OptionWindow.ERROR_MESSAGE, OptionWindow.OK_BUTTON);
				System.exit(1);
			}
			instance.account = account_1;
		}
		
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
				try {Thread.sleep(100);} catch (InterruptedException e) {}
			}
			
			if (accountFuture.isCancelled()) {
				instance.close();
				System.exit(0);
			}
			
			try {
				Account account_ = accountFuture.get();
				if (account_ == null) {
					OptionWindow.showMessage("Your Account Password and Username Are Incorrect!", "Error", OptionWindow.ERROR_MESSAGE, OptionWindow.OK_BUTTON);
					instance.close();
					System.exit(1);
				}
				
				Capsule.instance.gameEngine.setCurrentScreen(null);
				instance.account = account_;
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			} finally {
				if (instance.account == null) {
					instance.close();
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
			
			if (!Util.IsThisGameYours(gameID, instance.account)) {
				OptionWindow.showMessage("Düzenlemeye çalıştığınız oyun size ait değil! Lütfen kendi oyununuzu düzenleyin!", "Error", OptionWindow.ERROR_MESSAGE, OptionWindow.OK_BUTTON);
				System.exit(-1);
			} else {
				final int __gameID = gameID;
				EventQueue.invokeLater(() -> {
					try {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
						
						JFrame frame = instance.gameFrame;
						frame.setVisible(false);
						
						StudioPanel window = new StudioPanel(__gameID);
						frame.addWindowListener(new WindowAdapter() {
							@Override
							public void windowClosing(WindowEvent e) {
								File layoutFile = new File(Util.getDirectory() + "layout.xml");
								DockView.saveLayout(window.control, layoutFile);
								
								try {
									window.theGameData.saveWorld();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						});
						frame.add(window);
						frame.pack();
						frame.setLocationRelativeTo(null);
						frame.setVisible(true);
						
						frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
						frame.toFront();
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
				//Capsule.instance.gameEngine.setCurrentScreen(new WorldEditor(gameID));
			}
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
                String key = arg.substring(2);
                String value = "";

                if (key.contains("=")) {
                    String[] parts = key.split("=", 2);
                    key = parts[0];
                    value = parts[1];
                } else if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    value = args[++i];
                }

                options.put(key, value);
            } else if (arg.startsWith("-")) {
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

    public static void log(String string) {
        System.out.println("[Capsule] " + string);
    }

    public static JSONObject checkUpdate() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://capsule.net.tr/api/v1/checkversion.php?version=" + version.toString()))
                    .GET().build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            return new JSONObject(response.body());
        } catch (Exception e) {
            return null;
        }
    }
}
