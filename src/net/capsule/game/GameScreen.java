package net.capsule.game;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.nodes.Part;
import me.ramazanenescik04.diken.gui.compoment.*;
import me.ramazanenescik04.diken.gui.screen.Screen;
import me.ramazanenescik04.diken.gui.window.SettingsWindow;
import me.ramazanenescik04.diken.resource.Bitmap;
import net.capsule.Capsule;

public class GameScreen extends Screen {
	
	private Panel pausePanel;
	private TextField chatBar;
	private boolean chatBarEnabled, pauseMenuEnabled;
	
	public World theWorld;
	private SoloPlayer thePlayer = new SoloPlayer(100, 100);
	
	private List<String> chatMessageList;
	
	public void openScreen() {
		chatMessageList = new ArrayList<>();
		theWorld = new World("TestGame", engine.getWidth(), engine.getHeight());
		theWorld.setBounds(0, 0, engine.getWidth(), engine.getHeight());
		thePlayer.setDebugRenderer(true);
		theWorld.addNode(new Part(100, 100, 200, 200) {
			@Override
			public void onAdded() {
				this.color = 0xff00ffff;
			}
		});
		theWorld.addNode(thePlayer);
		thePlayer.setFollowCamera(true);
		chatBar = new TextField(2, engine.getHeight() - 22, engine.getWidth() - 2, 20);
		pausePanel = new Panel(0, 0, engine.getWidth(), engine.getHeight());
		
		this.getContentPane().add(theWorld);
		initPausePanel();
	}
	
	private void initPausePanel() {
		Button resumeButton = new Button("Resume The Game", pausePanel.width / 2 - 120 / 2, (pausePanel.height / 2 - 20 / 2) - 25, 120, 20).setRunnable(() -> {
			this.pauseMenuEnabled = false;
			this.getContentPane().remove(pausePanel);
		}).setButtonColor(0xff005cff);
		Button settingsButton = new Button("Settings", pausePanel.width / 2 - 120 / 2, (pausePanel.height / 2 - 20 / 2), 120, 20).setRunnable(() -> {
			if (!this.engine.wManager.isWindowActive(SettingsWindow.class)) {
				this.engine.wManager.addWindow(new SettingsWindow());
			};
		}).setButtonColor(0xff005cff);
		Button exitButton = new Button("Exit The Game", pausePanel.width / 2 - 120 / 2, (pausePanel.height / 2 - 20 / 2) + 25, 120, 20).setRunnable(() -> {
			Capsule.instance.close();
		}).setButtonColor(0xff005cff);
		pausePanel.add(resumeButton);
		pausePanel.add(settingsButton);
		pausePanel.add(exitButton);
	}
	
	public void resized() {
		chatBar.setBounds(2, engine.getHeight() - 22, engine.getWidth() - 2, 20);
		pausePanel.setSize(engine.getWidth(), engine.getHeight());
		theWorld.setSize(engine.getWidth(), engine.getHeight());
		
		pausePanel.get(0).setLocation(pausePanel.width / 2 - 120 / 2, (pausePanel.height / 2 - 20 / 2) - 25);
		pausePanel.get(1).setLocation(pausePanel.width / 2 - 120 / 2, (pausePanel.height / 2 - 20 / 2));
		pausePanel.get(2).setLocation(pausePanel.width / 2 - 120 / 2, (pausePanel.height / 2 - 20 / 2) + 25);
	}
	
	@Override
	public void render(Bitmap bitmap) {		
		bitmap.clear(0xffcefbf9 + 0xff7d7d7d);
		
		if (pauseMenuEnabled) {
			bitmap.blendFill(0, 0, engine.getWidth(), engine.getHeight(), 0xaa000000);
		}
		
		super.render(bitmap);
		
		bitmap.drawText("Position: " + thePlayer.x + " - " + thePlayer.y, 2, 2, false);
		
		for (int i = 0; i < this.chatMessageList.size(); i++) {
			String text = this.chatMessageList.get(i);
			bitmap.drawText(text, 2, this.engine.getHeight() - (i * 9) - 35, false);
		}
	}

	@Override
	public void keyDown(char eventCharacter, int eventKey) {
		super.keyDown(eventCharacter, eventKey);
		
		if (!chatBarEnabled && !pauseMenuEnabled) {
			if (eventKey == Keyboard.KEY_DIVIDE) {
				chatBar.setFocused(true);
				this.getContentPane().add(chatBar);
				chatBarEnabled = true;
			} else if (eventKey == Keyboard.KEY_ESCAPE) {
				this.getContentPane().add(pausePanel);
				pauseMenuEnabled = true;
			}
		} else if (chatBarEnabled && !pauseMenuEnabled) {
			if (eventKey == Keyboard.KEY_ESCAPE || eventKey == Keyboard.KEY_RETURN) {
				chatBar.setFocused(false);
				if (eventKey == Keyboard.KEY_RETURN) {
					sendMessage(Capsule.instance.account.getUsername() + ": " + chatBar.text);
				}
				
				chatBarEnabled = false;
				chatBar.text = "";
				this.getContentPane().remove(chatBar);
			}
		} else if (!chatBarEnabled && pauseMenuEnabled) {
			if (eventKey == Keyboard.KEY_ESCAPE) {
				this.getContentPane().remove(pausePanel);
				pauseMenuEnabled = false;
			}
		}
	}
	
	public void tick() {
		super.tick();
		
		if (thePlayer.followCamera) {
			thePlayer.centerCamera(this.theWorld, engine);
		}
		
		if (thePlayer.canMove && (pauseMenuEnabled || chatBarEnabled)) {
			thePlayer.canMove = false;
		} else if (!thePlayer.canMove && !(pauseMenuEnabled || chatBarEnabled)) {
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
