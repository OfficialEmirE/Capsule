package net.capsule.gui;

import java.net.URI;
import java.util.*;

import javax.swing.JOptionPane;

import org.json.JSONObject;

import me.ramazanenescik04.diken.gui.compoment.Button;
import me.ramazanenescik04.diken.gui.compoment.LinkButton;
import me.ramazanenescik04.diken.gui.compoment.Panel;
import me.ramazanenescik04.diken.gui.compoment.RenderImage;
import me.ramazanenescik04.diken.gui.screen.Screen;
import me.ramazanenescik04.diken.gui.screen.StaticBackground;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import net.capsule.Capsule;
import net.capsule.game.CapsuleGame;
import net.capsule.util.Util;

public class GameSelectionScreen extends Screen {
	
	private List<CapsuleGame> games;
	
	private Bitmap capsuleLogoImage, gamesPanelBg;
	private int page = 0, totalPages = 0;
	
	public GameSelectionScreen() {
		games = new ArrayList<>();
		
		capsuleLogoImage = ((Bitmap) ResourceLocator.getResource(new ResourceLocator.ResourceKey("capsule", "logo"))).resize(627 / 4, 205 / 4);
		gamesPanelBg = ((ArrayBitmap) ResourceLocator.getResource("bgd-tiles")).getBitmap(0, 0);
	}
	
	public void openScreen() {
		int width = engine.getWidth();
		this.getContentPane().clear();
		
		Panel titlePanel = new Panel(0, 0, width, 60);
		titlePanel.setBackground(new StaticBackground(Bitmap.createClearedBitmap(64, 64, 0xffffffff)));
		
		RenderImage capsuleLogo = new RenderImage(capsuleLogoImage, 10, 5);
		titlePanel.add(capsuleLogo);
		
		String username = Capsule.instance.account.getUsername();
		LinkButton linkableText = new LinkButton(username, 0, 10, username.length() * 7, 16).setURI(URI.create("http://capsule.net.tr/profile/?username=" + username));
		linkableText.setLocation(width - linkableText.width - 110, 10);
		titlePanel.add(linkableText);
		
		Button logoffButton = new Button("Logoff", titlePanel.width - 100, 10, 80, 16).setRunnable(() -> {
			int quitting = JOptionPane.showConfirmDialog(Capsule.instance.frame, "Are you sure you want to logoff?", "Logoff Confirmation", 
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			
			if (quitting == JOptionPane.YES_OPTION) {
				Capsule.instance.account.logoff();
				Capsule.instance.account = null;
				Capsule.instance.close();				
			}
		});
		
		titlePanel.add(logoffButton);
		
		this.getContentPane().add(titlePanel);
	
		Panel gamesPanel = new Panel(0, 60, width, engine.getHeight() - 60);
		gamesPanel.setBackground(new RandomPositionBg(gamesPanelBg));
		this.getContentPane().add(gamesPanel);
		
		loadGameList();
		
		refreshGamesGrid();

		// --- Butonlar ---

		Button pageBack = new Button("Page Back", 10, engine.getHeight() - 40, 100, 30).setRunnable(() -> {
		    if (page > 0) {
		        page--;
		        refreshGamesGrid();
		    }
		});

		Button pageForward = new Button("Page Forward", engine.getWidth() - 110, engine.getHeight() - 40, 100, 30).setRunnable(() -> {
		    // Eğer bir sonraki sayfa mevcutsa (index sınırını aşmıyorsak)
		    if (page < totalPages - 1) {
		        page++;
		        refreshGamesGrid();
		    }
		});
		
		this.getContentPane().add(pageBack);
		this.getContentPane().add(pageForward);
	}
	
	// Bu metodu hem butonlarda hem de resize kısmında çağıracağız
	private void refreshGamesGrid() {
	    Panel gamesPanel = (Panel) this.getContentPane().get(1);
	    gamesPanel.clear(); // Paneli temizle

	    if (games.isEmpty()) return;

	    // --- 1. Dinamik Hesaplamalar (Her çağrıldığında güncel panel boyutunu kullanır) ---
	    int gameW = games.get(0).width;
	    int gameH = games.get(0).height;
	    int gap = 20;

	    int gamesPerRow = Math.max(1, gamesPanel.width / (gameW + gap));
	    int rowsPerPage = Math.max(1, gamesPanel.height / (gameH + gap));
	    int maxItemsPerPage = gamesPerRow * rowsPerPage;

	    // Toplam sayfa sayısını güncelle
	    this.totalPages = (int) Math.ceil((double) games.size() / maxItemsPerPage);
	    
	    // Sayfa sınırını kontrol et (Resize sonrası sayfa sayısı azalmış olabilir)
	    if (page >= totalPages) page = Math.max(0, totalPages - 1);

	    // --- 2. Sayfayı Çiz ---
	    int startIndex = page * maxItemsPerPage;
	    int endIndex = Math.min(startIndex + maxItemsPerPage, games.size());

	    for (int i = startIndex; i < endIndex; i++) {
	        CapsuleGame game = games.get(i);
	        int visualIndex = i - startIndex; 

	        int x = 10 + (visualIndex % gamesPerRow) * (gameW + gap);
	        int y = 10 + (visualIndex / gamesPerRow) * (gameH + gap);
	        
	        game.setLocation(x, y);
	        gamesPanel.add(game);
	    }
	}
	
	private synchronized void loadGameList() {
		new Thread(() -> {
			JSONObject gamesData = new JSONObject(Util.getWebData("http://capsule.net.tr/api/v1/games/"));
			
			if (!gamesData.getString("status").equals("success")) {
				System.err.println("Failed to fetch games data: " + gamesData.getString("message"));
				return;
			}
			
			for (Object gameObj : gamesData.getJSONArray("data")) {
				JSONObject gameJson = (JSONObject) gameObj;
				
				int gameId = gameJson.getInt("id");
				String gameName = gameJson.getString("title");
				String iconUrl = gameJson.getString("image_url");
				String authorUsername = gameJson.optString("username", "Anonymouns");
				
				CapsuleGame game = new CapsuleGame(Util.getImageWeb(URI.create(iconUrl)), gameId, gameName, authorUsername);
				games.add(game);
				refreshGamesGrid();
			}			
		}, "Game Load Thread").start();
	}
	
	public void resized() {
		try {
			Panel gamesPanel = (Panel) this.getContentPane().get(1);
			gamesPanel.setSize(engine.getWidth(), engine.getHeight() - 60);

			refreshGamesGrid();
		} catch (Throwable e) {
		}
		
		Panel titlePanel = (Panel) this.getContentPane().get(0);
		titlePanel.setSize(engine.getWidth(), 60);
		
		LinkButton usernameText = (LinkButton) titlePanel.get(1);
		usernameText.setLocation(titlePanel.width - usernameText.width - 110, 10);
		
		Button logoffButton = (Button) titlePanel.get(2);
		logoffButton.setLocation(titlePanel.width - 100, 10);
		
		Button pageBack = (Button) this.getContentPane().get(2);
		pageBack.setLocation(10, engine.getHeight() - 40);
		
		Button pageForward = (Button) this.getContentPane().get(3);
		pageForward.setLocation(engine.getWidth() - 110, engine.getHeight() - 40);
	}

	@Override
	public void render(Bitmap bitmap) {
		super.render(bitmap);
		
		bitmap.drawLine(0, 60, engine.getWidth(), 60, 0xffffffff, 1);
		
		bitmap.drawText("Pages " + (page + 1) + " / " + totalPages, engine.getWidth() / 2, engine.getHeight() - 20, true);
	}
}
