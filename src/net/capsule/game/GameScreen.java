package net.capsule.game;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.nodes.Sky;
import me.ramazanenescik04.diken.game.nodes.SpawnLocation;
import me.ramazanenescik04.diken.gui.compoment.*;
import me.ramazanenescik04.diken.gui.screen.Screen;
import me.ramazanenescik04.diken.gui.window.SettingsWindow;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import net.capsule.Capsule;
import net.capsule.game.node.MaterialPart;
import net.capsule.gui.GameSelectionScreen;

public class GameScreen extends Screen {
	
	private Panel pausePanel;
	private TextField chatBar;
	private ProgressBar healthBar;
	private boolean chatBarEnabled, pauseMenuEnabled;
	
	public World theWorld;
	private SoloPlayer thePlayer = new SoloPlayer(100, 100);
	
	private List<String> chatMessageList;
	
	public void openScreen() {
		System.gc();
		
		chatMessageList = new ArrayList<>();
		theWorld = new World("TestGame", engine.getWidth(), engine.getHeight());
		/*try {
			theWorld = World.loadWorld(new File("./world.dew"));
			theWorld.gameName = "TestGame";
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		theWorld.setBounds(0, 0, engine.getWidth(), engine.getHeight());
		theWorld.addNode(new Sky(0xffcefbf9 + 0xff7d7d7d));
		MaterialPart part = new MaterialPart(100, 100, 200, 200, MaterialPart.Material.Smooth_Stud);
		part.setSolid(false);
		part.addChild(new MaterialPart(150, 150, 16, 16, MaterialPart.Material.Sapling));
		part.addChild(new MaterialPart(100, 100, 16, 16, MaterialPart.Material.Flower));
		part.addChild(new MaterialPart(170, 150, 16, 16, MaterialPart.Material.Rose));
		part.addChild(new MaterialPart(150, 100, 16, 16, MaterialPart.Material.Web));
		theWorld.addNode(part);
		theWorld.addNode(new SpawnLocation(100, 100, 16, 16));
		theWorld.addNode(new SpawnLocation(-100, -100, 16, 16));
		theWorld.addNode(new SpawnLocation(-100, 100, 16, 16));
		theWorld.addNode(new SpawnLocation(100, -100, 16, 16));
		
		theWorld.addNode(thePlayer);
		thePlayer.setFollowCamera(true);
		thePlayer.setDebugRenderer(true);
		
		theWorld.root.printTree(true);
		
		chatBar = new TextField(2, engine.getHeight() - 22, engine.getWidth() - 2, 20);
		pausePanel = new Panel(0, 0, engine.getWidth(), engine.getHeight());
		healthBar = new ProgressBar(engine.getWidth() / 2 - 110 / 2, engine.getHeight() - 26, 110, 16);
		healthBar.text = "Health";
		healthBar.color = 0xff4fff4f;
		healthBar.color2 = 0xff33a633;
		
		this.getContentPane().add(theWorld);
		this.getContentPane().add(healthBar);
		
		initPausePanel();
		
		ArrayBitmap menuButtonTextures = (ArrayBitmap) ResourceLocator.getResource(new ResourceLocator.ResourceKey("capsule", "menu_buttons"));
		ImageButton pauseButton = new ImageButton(menuButtonTextures.getBitmap(0, 0), 2, 2, 20, 20);
		pauseButton.setRunnable(() -> {
			if (this.chatBarEnabled)
				this.closeChatMenu();
			
			this.openPauseMenu();
		});
		this.getContentPane().add(pauseButton);
		
		ImageButton chatButton = new ImageButton(menuButtonTextures.getBitmap(1, 0), 24, 2, 20, 20);
		chatButton.setRunnable(() -> {
			if (this.chatBarEnabled) {
				this.closeChatMenu();
			} else {
				this.openChatMenu();
			}
		});
		this.getContentPane().add(chatButton);
	}
	
	private void initPausePanel() {
		pausePanel.clear();
		Button resumeButton = new Button("Resume The Game", pausePanel.width / 2 - 120 / 2, (pausePanel.height / 2 - 20 / 2) - 25, 120, 22).setRunnable(() -> {			
			this.closePauseMenu();
		}).setButtonColor(0xff005cff);
		Button settingsButton = new Button("Settings", pausePanel.width / 2 - 120 / 2, (pausePanel.height / 2 - 20 / 2), 120, 22).setRunnable(() -> {
			if (!this.engine.wManager.isWindowActive(SettingsWindow.class)) {
				this.engine.wManager.addWindow(new SettingsWindow());
			};
		}).setButtonColor(0xff005cff);
		Button exitButton = new Button("Exit The Game", pausePanel.width / 2 - 120 / 2, (pausePanel.height / 2 - 20 / 2) + 25, 120, 22).setRunnable(() -> {
			this.engine.setCurrentScreen(new GameSelectionScreen());
			System.gc();
		}).setButtonColor(0xff005cff);
		pausePanel.add(resumeButton);
		pausePanel.add(settingsButton);
		pausePanel.add(exitButton);
	}
	
	public void resized() {
		chatBar.setBounds(2, engine.getHeight() - 22, engine.getWidth() - 2, 20);
		pausePanel.setSize(engine.getWidth(), engine.getHeight());
		theWorld.setSize(engine.getWidth(), engine.getHeight());
		healthBar.setBounds(engine.getWidth() / 2 - 110 / 2, engine.getHeight() - 26, 110, 16);
		
		pausePanel.get(0).setLocation(pausePanel.width / 2 - 120 / 2, (pausePanel.height / 2 - 20 / 2) - 25);
		pausePanel.get(1).setLocation(pausePanel.width / 2 - 120 / 2, (pausePanel.height / 2 - 20 / 2));
		pausePanel.get(2).setLocation(pausePanel.width / 2 - 120 / 2, (pausePanel.height / 2 - 20 / 2) + 25);
	}
	
	@Override
	public void render(Bitmap bitmap) {				
		super.render(bitmap);
		
		if (pauseMenuEnabled) {
			bitmap.blendFill(0, 0, engine.getWidth(), engine.getHeight(), 0xaa000000);
			
			bitmap.draw(this.pausePanel.render(), 0, 0);
		}
		
		bitmap.drawText("isAlive: " + this.thePlayer.isAlive(), 0, 24, false);
		
		for (int i = 0; i < this.chatMessageList.size(); i++) {
			String text = this.chatMessageList.get(i);
			bitmap.drawText(text, 2, this.engine.getHeight() - (i * 9) - 35, false);
		}
	}
	
	public void openPauseMenu() {
		if (pauseMenuEnabled) return; // Zaten açıksa tekrar açma kodunu çalıştırma!
		
		pauseMenuEnabled = true;
		theWorld.active = false;
		initPausePanel();
		this.getContentPane().add(pausePanel);
		this.getContentPane().get(1).setActive(false);
		this.getContentPane().get(2).setActive(false);
	}
	
	public void openChatMenu() {
		if (chatBarEnabled) return; // Zaten açıksa tekrar açma kodunu çalıştırma!
		
		chatBar.setFocused(true);
		this.getContentPane().add(chatBar);
		chatBarEnabled = true;
	}
	
	public void closePauseMenu() {
		if (!pauseMenuEnabled) return; // Zaten açıksa tekrar açma kodunu çalıştırma!
		
		pauseMenuEnabled = false;
		theWorld.active = true;
		this.getContentPane().remove(pausePanel);
		this.getContentPane().get(1).setActive(true);
		this.getContentPane().get(2).setActive(true);
	}
	
	public void closeChatMenu() {
		if (!chatBarEnabled) return; // Zaten açıksa tekrar açma kodunu çalıştırma!
		
		chatBar.setFocused(false);
		chatBarEnabled = false;
		chatBar.text = "";
		this.getContentPane().remove(chatBar);
	}

	@Override
	public void keyDown(char eventCharacter, int eventKey) {
		super.keyDown(eventCharacter, eventKey);
		
		if (!chatBarEnabled && !pauseMenuEnabled) {
			if (eventKey == Keyboard.KEY_DIVIDE) {
				this.openChatMenu();
	
			} else if (eventKey == Keyboard.KEY_ESCAPE) {
				this.openPauseMenu();
			}
		} else if (chatBarEnabled && !pauseMenuEnabled) {
			if (eventKey == Keyboard.KEY_ESCAPE || eventKey == Keyboard.KEY_RETURN) {
				if (eventKey == Keyboard.KEY_RETURN) {
					sendMessage(Capsule.instance.account.getUsername() + ": " + chatBar.text);
				}
				
				this.closeChatMenu();
			}
		} else if (!chatBarEnabled && pauseMenuEnabled) {
			if (eventKey == Keyboard.KEY_ESCAPE) {
				this.closePauseMenu();
			}
		}
		
		if (eventKey == Keyboard.KEY_R) {
			this.thePlayer.setHealth(-1);
		}
	}
	
	public void tick() {
		super.tick();
		
		if (thePlayer.followCamera) {
			thePlayer.centerCamera(this.theWorld, engine, 57, 64);
		}
		
		healthBar.value = thePlayer.health;
		healthBar.maxValue = thePlayer.maxHealth;
		
		boolean busy = engine.wManager.activeWindow != null || pauseMenuEnabled || chatBarEnabled || !this.thePlayer.isAlive();
		
		if (thePlayer.canMove && busy) {
			thePlayer.canMove = false;
		} else if (!thePlayer.canMove && !busy) {
			thePlayer.canMove = true;
		}
	}

	public void sendMessage(String message) {
		this.chatMessageList.add(0, message);

		while(this.chatMessageList.size() > 50) {
			this.chatMessageList.remove(this.chatMessageList.size() - 1);
		}
	}
}
