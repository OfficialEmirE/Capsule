package net.capsule.gui;

import java.net.URI;
import java.util.*;

import org.json.JSONObject;
import org.lwjgl.input.Keyboard;

import me.ramazanenescik04.diken.gui.compoment.Button;
import me.ramazanenescik04.diken.gui.compoment.LinkButton;
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
import net.capsule.game.CapsuleGame;
import net.capsule.util.Util;

public class GameSelectionScreen extends Screen {
	
	private List<CapsuleGame> games;
	
	private Bitmap capsuleLogoImage, gamesPanelBg;
	private int page = 0, totalPages = 0;
	
	private TextField searchField;
	
	public GameSelectionScreen() {
		games = new ArrayList<>();
		searchField = new TextField(0, 0, 170, 20);
		
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
		
		Button searchButton = new Button("Search", (titlePanel.getWidth() / 2 + (searchField.width - 20) / 2), 10, 42, 20).setRunnable(() -> {page = 0; refreshGamesGrid();});
		titlePanel.add(searchButton);
		
		this.getContentPane().add(titlePanel);
	
		Panel gamesPanel = new Panel(0, 60, width, engine.getHeight() - 60);
		gamesPanel.setBackground(new RandomPositionBg(gamesPanelBg));
		this.getContentPane().add(gamesPanel);
		
		loadGameList();
		refreshGamesGrid();

		// --- Butonlar ---

		Button pageBack = new Button("Page Back", 10, engine.getHeight() - 40, 100, 34).setRunnable(() -> {
		    if (page > 0) {
		        page--;
		        refreshGamesGrid();
		    }
		});

		Button pageForward = new Button("Page Forward", engine.getWidth() - 110, engine.getHeight() - 40, 100, 34).setRunnable(() -> {
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

	    // --- 1. ARAMA VE FİLTRELEME ---
	    // Ekranda gösterilecek oyunları tutacak geçici bir liste
	    List<CapsuleGame> visibleGames;
	    
	    String searchText = searchField.getText();

	    if (searchText.trim().isEmpty()) {
	        // Arama yoksa hepsini göster
	        visibleGames = games;
	    } else {
	        // Arama varsa filtrele
	        visibleGames = new ArrayList<>();
	        String searchLower = searchText.toLowerCase(); // Büyük/küçük harf duyarsız olması için

	        for (CapsuleGame game : games) {
	            // game.getName() veya game.name (senin değişken adın neyse onu yaz)
	            // contains ile içinde geçiyor mu diye bakıyoruz
	            if (game.getGameName().toLowerCase().contains(searchLower)) {
	                visibleGames.add(game);
	            }
	        }
	    }

	    // Eğer gösterilecek oyun yoksa (arama sonucu boşsa) çık
	    if (visibleGames.isEmpty()) {
	        this.totalPages = 0;
	        this.page = 0;
	        return; 
	    }

	    // --- 2. DİNAMİK HESAPLAMALAR (Artık visibleGames listesini kullanıyoruz) ---
	    // Referans boyutları visibleGames'in ilk elemanından alıyoruz
	    int gameW = visibleGames.get(0).width;
	    int gameH = visibleGames.get(0).height;
	    int gap = 20;

	    int gamesPerRow = Math.max(1, gamesPanel.width / (gameW + gap));
	    int rowsPerPage = Math.max(1, gamesPanel.height / (gameH + gap));
	    int maxItemsPerPage = gamesPerRow * rowsPerPage;

	    // Toplam sayfa sayısını GÖRÜNÜR oyunlara göre güncelle
	    this.totalPages = (int) Math.ceil((double) visibleGames.size() / maxItemsPerPage);
	    
	    // Sayfa sınırını kontrol et (Filtreleme sonrası sayfa sayısı çok azalabilir)
	    if (page >= totalPages) page = Math.max(0, totalPages - 1);

	    // --- 3. SAYFAYI ÇİZ ---
	    int startIndex = page * maxItemsPerPage;
	    int endIndex = Math.min(startIndex + maxItemsPerPage, visibleGames.size());

	    for (int i = startIndex; i < endIndex; i++) {
	        CapsuleGame game = visibleGames.get(i);
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
		
		searchField.setLocation((titlePanel.getWidth() / 2 - (searchField.width + 30) / 2), 10);
		
		Button searchButton = (Button) titlePanel.get(4);
		searchButton.setLocation((titlePanel.getWidth() / 2 + (searchField.width - 20) / 2), 10);
	}

	@Override
	public void render(Bitmap bitmap) {
		super.render(bitmap);
		
		bitmap.drawLine(0, 60, engine.getWidth(), 60, 0xffffffff, 1);
		
		bitmap.drawText("Pages " + (page + 1) + " / " + totalPages, engine.getWidth() / 2, engine.getHeight() - 20, true);
	}

	@Override
	public void keyDown(char eventCharacter, int eventKey) {
		if (this.searchField.isFocused() && eventKey == Keyboard.KEY_RETURN) {
			page = 0;
			refreshGamesGrid();
		}
		
		super.keyDown(eventCharacter, eventKey);
	}
}
