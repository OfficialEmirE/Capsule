package net.capsule.studio;

import java.io.File;
import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opencl.api.Filter;

import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.gui.compoment.Button;
import me.ramazanenescik04.diken.gui.compoment.Panel;
import me.ramazanenescik04.diken.gui.compoment.TextField;
import me.ramazanenescik04.diken.gui.screen.Screen;
import me.ramazanenescik04.diken.gui.screen.StaticBackground;
import me.ramazanenescik04.diken.gui.window.OptionWindow;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import net.capsule.Capsule;

import net.capsule.game.CapsuleGame;
import net.capsule.gui.GameListPanel;
import net.capsule.gui.RandomPositionBg;
import net.capsule.util.Util;

public class PublishGameScreen extends Screen {
	
	private Screen parent;
	private World theWorld;
	
	private GameListPanel games;
	
	private Bitmap gamesPanelBg;
	private TextField searchField;
	private Filter<CapsuleGame> searchAndUserFilter;
	
	public PublishGameScreen(Screen parent, World theWorld) {
		this.parent = parent;
		this.theWorld = theWorld;
		
		searchField = new TextField(0, 0, 170, 20);
		
		gamesPanelBg = ((ArrayBitmap) ResourceLocator.getResource("bgd-tiles")).getBitmap(0, 0);
		
		searchAndUserFilter = new PublishGameFilter();
	}

	public void openScreen() {
		int width = engine.getWidth();
		this.getContentPane().clear();
		
		Panel titlePanel = new Panel(0, 0, width, 60);
		titlePanel.setBackground(new StaticBackground(Bitmap.createClearedBitmap(64, 64, 0xffffffff)));

		Button logoffButton = new Button("Back", 10, 10, 80, 20).setRunnable(() -> {
			this.engine.setCurrentScreen(parent);
			System.gc();
		});

		titlePanel.add(logoffButton);
		
		searchField.setLocation((titlePanel.getWidth() / 2 - (searchField.width + 30) / 2), 10);
		titlePanel.add(searchField);
		
		Button searchButton = new Button("Search", (titlePanel.getWidth() / 2 + (searchField.width - 20) / 2), 10, 42, 20).setRunnable(() -> {
			games.page = 0; 
			games.searchWithFilter(searchAndUserFilter);
		});
		titlePanel.add(searchButton);
		
		this.getContentPane().add(titlePanel);
	
		games = new GameListPanel(0, 60, width, engine.getHeight() - 60);
		games.setBackground(new RandomPositionBg(gamesPanelBg));
		games.setFilter(searchAndUserFilter);
		games.setPlayPressedConsumer((game) -> {
			File savePath = EditorUtil.openSaveWorldDialog();
			if (savePath == null)
				return;
				
			try {
				World.saveWorld(theWorld, savePath);
			} catch (IOException e) {
				e.printStackTrace();
				OptionWindow.showMessageNoWait("Save Error: " + e.getMessage(), "Error", OptionWindow.ERROR_MESSAGE, OptionWindow.OK_BUTTON, (i) -> this.engine.setCurrentScreen(parent));
				return;
			}
			
			this.engine.setCurrentScreen(parent);
			
			try {
				Util.uploadGame(Integer.toString(game.getGameId()), Capsule.instance.account.getApiKey().toString(), savePath);
			} catch (Exception e) {
				e.printStackTrace();
				OptionWindow.showMessageNoWait("Upload Error: " + e.getMessage(), "Error", OptionWindow.ERROR_MESSAGE, OptionWindow.OK_BUTTON, null);
				return;
			}
		});
		this.getContentPane().add(games);
	}
	
	public void resized() {
		games.setSize(engine.getWidth(), engine.getHeight() - 60);
		
		Panel titlePanel = (Panel) this.getContentPane().get(0);
		titlePanel.setSize(engine.getWidth(), 60);
		
		searchField.setLocation((titlePanel.getWidth() / 2 - (searchField.width + 30) / 2), 10);
		
		Button searchButton = (Button) titlePanel.get(2);
		searchButton.setLocation((titlePanel.getWidth() / 2 + (searchField.width - 20) / 2), 10);
	}

	@Override
	public void render(Bitmap bitmap) {
		super.render(bitmap);
		
		bitmap.drawLine(0, 60, engine.getWidth(), 60, 0xffffffff, 1);
		bitmap.drawText("Select Publish Game", engine.getWidth() / 2, 45, true);
		bitmap.drawText("Pages " + (games.page + 1) + " / " + games.totalPages, engine.getWidth() / 2, engine.getHeight() - 20, true);
	}

	@Override
	public void keyDown(char eventCharacter, int eventKey) {
		if (this.searchField.isFocused() && eventKey == Keyboard.KEY_RETURN) {
			games.page = 0; 
			games.searchWithFilter(searchAndUserFilter);
		}
		
		super.keyDown(eventCharacter, eventKey);
	}
	
	private class PublishGameFilter implements Filter<CapsuleGame> {
		private String username;
		
		public PublishGameFilter() {
			this.username = Capsule.instance.account.getUsername().toLowerCase();
		}
		
		@Override
		public boolean accept(CapsuleGame object) {
			if (object.getAuthorUsername().toLowerCase().equals(username)) {
				String text = searchField.getText().toLowerCase();
				if (text.isEmpty()) {
					return true;
				} else if (object.getGameName().toLowerCase().contains(text)){
					return true;
				}
			}
			return false;
		}
	}

}
