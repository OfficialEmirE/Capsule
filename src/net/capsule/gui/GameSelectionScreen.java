package net.capsule.gui;

import java.net.URI;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opencl.api.Filter;

import me.ramazanenescik04.diken.gui.compoment.Button;
import me.ramazanenescik04.diken.gui.compoment.LinkButton;
import me.ramazanenescik04.diken.gui.compoment.LinkText;
import me.ramazanenescik04.diken.gui.compoment.Panel;
import me.ramazanenescik04.diken.gui.compoment.RenderImage;
import me.ramazanenescik04.diken.gui.compoment.TextField;
import me.ramazanenescik04.diken.gui.screen.Screen;
import me.ramazanenescik04.diken.gui.screen.StaticBackground;
import me.ramazanenescik04.diken.gui.window.OptionWindow;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import net.capsule.Capsule;
import net.capsule.GameLoadingScreen;
import net.capsule.game.CapsuleGame;

public class GameSelectionScreen extends Screen {
	
	private Bitmap capsuleLogoImage, gamesPanelBg;
	
	private TextField searchField;
	private GameListPanel games;
	private Panel warningPanel;

	private Filter<CapsuleGame> filter;
	
	public GameSelectionScreen() {
		searchField = new TextField(0, 0, 170, 20);
		
		capsuleLogoImage = ((Bitmap) ResourceLocator.getResource(new ResourceLocator.ResourceKey("capsule", "logo"))).resize(627 / 4, 205 / 4);
		gamesPanelBg = ((ArrayBitmap) ResourceLocator.getResource("bgd-tiles")).getBitmap(0, 0);
		
		filter = (o) -> {
			var text = this.searchField.getText().toLowerCase();
			
			if (text.isEmpty()) {
				return true;
			} else {
				return o.getGameName().toLowerCase().contains(text);
			}
		};
	}
	
	public void openScreen() {
		int width = engine.getWidth();
		this.getContentPane().clear();
		
		Panel titlePanel = new Panel(0, 0, width, 60);
		titlePanel.setBackground(new StaticBackground(Bitmap.createClearedBitmap(64, 64, 0xffffffff)));
		
		RenderImage capsuleLogo = new RenderImage(capsuleLogoImage, 10, 5);
		titlePanel.add(capsuleLogo);
		
		String username = Capsule.instance.account.getUsername();
		LinkButton linkableText = new LinkButton(username, 0, 10, username.length() * 8, 20).setURI(URI.create("http://capsule.net.tr/profile/?username=" + username));
		linkableText.setLocation(width - linkableText.width - 110, 10);
		titlePanel.add(linkableText);
		
		Button logoffButton = new Button("Logoff", titlePanel.width - 100, 10, 80, 20).setRunnable(() -> {
			OptionWindow.showMessageNoWait("Are you sure you want to logoff?", "Logoff Confirmation", OptionWindow.PLAIN_MESSAGE, OptionWindow.YES_NO_OPTION, (i) -> {
				if (i == OptionWindow.YES_BUTTON) {
					Capsule.instance.account.logoff();
					Capsule.instance.account = null;
					Capsule.instance.close();				
				}
			});
		});
		
		titlePanel.add(logoffButton);
		
		searchField.setLocation((titlePanel.getWidth() / 2 - (searchField.width + 30) / 2), 10);
		titlePanel.add(searchField);
		
		Button searchButton = new Button("Search", (titlePanel.getWidth() / 2 + (searchField.width - 20) / 2), 10, 42, 20).setRunnable(() -> {
			games.page = 0; 
			games.searchWithFilter(filter);
		});
		titlePanel.add(searchButton);
		
		this.getContentPane().add(titlePanel);
	
		games = new GameListPanel(0, 60, width, engine.getHeight() - 60);
		games.setBackground(new RandomPositionBg(gamesPanelBg));
		games.setFilter(filter);
		games.setPlayPressedConsumer((game) -> {
			this.engine.setCurrentScreen(new GameLoadingScreen(game));
		});
		
		this.getContentPane().add(games);
		
		warningPanel = new Panel(0, 0, 265, 16) {
			private Bitmap warningIcon = ((ArrayBitmap)ResourceLocator.getResource("win-icons")).getBitmap(4, 0);
			
			@Override
			public Bitmap render() {
				Bitmap btp = new Bitmap(width, height);
				btp.clear(0xffbdbd00);
				btp.draw(warningIcon, 0, 0);
				btp.drawLine(17, 0, 17, 15, -1, 2);
				
				btp.drawText("Capsule - Alpha! Bazı Özellikler Çalışmayabilir!", 19, 2, false);
				return btp;
			}
		};
		warningPanel.setLocation(width / 2 - warningPanel.getWidth() / 2, 40);	
		this.getContentPane().add(warningPanel);
		
		LinkText text = new LinkText("Email: ramazanenescik04@capsule.net.tr", 0, 0).setURI(URI.create("mailto://ramazanenescik04@capsule.net.tr"));
		this.getContentPane().add(text);
		text.tick(engine);
		text.setLocation((engine.getWidth() - text.getWidth()) / 2, engine.getHeight() - 10);
	}
	
	public void resized() {
		games.setSize(engine.getWidth(), engine.getHeight() - 60);
		
		Panel titlePanel = (Panel) this.getContentPane().get(0);
		titlePanel.setSize(engine.getWidth(), 60);
		
		LinkButton usernameText = (LinkButton) titlePanel.get(1);
		usernameText.setLocation(titlePanel.width - usernameText.width - 110, 10);
		
		Button logoffButton = (Button) titlePanel.get(2);
		logoffButton.setLocation(titlePanel.width - 100, 10);
		
		searchField.setLocation((titlePanel.getWidth() / 2 - (searchField.width + 30) / 2), 10);
		
		Button searchButton = (Button) titlePanel.get(4);
		searchButton.setLocation((titlePanel.getWidth() / 2 + (searchField.width - 20) / 2), 10);
		
		warningPanel.setLocation(engine.getWidth() / 2 - warningPanel.getWidth() / 2, 40);	
		
		LinkText text = (LinkText) this.getContentPane().get(3);
		text.setLocation((engine.getWidth() - text.getWidth()) / 2, engine.getHeight() - 10);
	}

	@Override
	public void render(Bitmap bitmap) {
		super.render(bitmap);
		
		bitmap.drawLine(0, 60, engine.getWidth(), 60, 0xffffffff, 1);
		bitmap.drawText("Pages " + (games.page + 1) + " / " + games.totalPages, engine.getWidth() / 2, engine.getHeight() - 20, true);
	}

	@Override
	public void keyDown(char eventCharacter, int eventKey) {
		if (this.searchField.isFocused() && eventKey == Keyboard.KEY_RETURN) {
			games.page = 0; 
			games.searchWithFilter(filter);
		}
		
		super.keyDown(eventCharacter, eventKey);
	}
}
