package net.capsule.gui;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.json.JSONObject;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.Hitbox;
import me.ramazanenescik04.diken.gui.compoment.Button;
import me.ramazanenescik04.diken.gui.compoment.GuiComponent;
import me.ramazanenescik04.diken.gui.compoment.Panel;
import net.capsule.game.CapsuleGame;
import net.capsule.util.Util;

public class GameListPanel extends Panel {
	private static final long serialVersionUID = -7745332991415050985L;
	private Hitbox prevRect;
	
	private Consumer<CapsuleGame> consumer;
	private List<CapsuleGame> games;
	
	public int page, totalPages;
	
	private Button pageBack, pageForward;
	private Filter<CapsuleGame> filter;

	public GameListPanel() {
		prevRect = this.getBounds();
		games = new ArrayList<>();
	}
	
	public void searchWithFilter(Filter<CapsuleGame> f) {
		this.filter = f;
		this.refreshGamesGrid();
	}
	
	public void setFilter(Filter<CapsuleGame> f) {
		this.filter = f;
	}

	public GameListPanel(int x, int y, int width, int height) {
		super(x, y, width, height);
		prevRect = this.getBounds();
		games = new ArrayList<>();
	}
	
	@Override
	public void init(DikenEngine engine) {
		prevRect = this.getBounds();
		
		loadGameList();
		
		pageBack = new Button("Page Back", 10, getHeight() - 40, 100, 34).setRunnable(() -> {
		    if (page > 0) {
		        page--;
		        refreshGamesGrid();
		    }
		});

		pageForward = new Button("Page Forward", getWidth() - 110, getHeight() - 40, 100, 34).setRunnable(() -> {
		    // Eğer bir sonraki sayfa mevcutsa (index sınırını aşmıyorsak)
		    if (page < totalPages - 1) {
		        page++;
		        refreshGamesGrid();
		    }
		});
		
		this.add(pageBack);
		this.add(pageForward);
	}

	public void setPlayPressedConsumer(Consumer<CapsuleGame> consumer) {
		this.consumer = consumer;
	}

	@Override
	public void tick(DikenEngine engine) {
		super.tick(engine);
		if (this.width != this.prevRect.width || this.height != this.prevRect.height) {
			this.prevRect = this.getBounds();
			
			refreshGamesGrid();
			
			GuiComponent compoment1 = this.get(this.count() - 2);
			GuiComponent compoment2 = this.get(this.count() - 1);
			
			compoment1.setLocation(10, getHeight() - 40);
			compoment2.setLocation(getWidth() - 110, getHeight() - 40);
		}
	}
	
	// Bu metodu hem butonlarda hem de resize kısmında çağıracağız
	public void refreshGamesGrid() {
		this.clear();
		
		// --- 1. ARAMA VE FİLTRELEME ---
		// Ekranda gösterilecek oyunları tutacak geçici bir liste
		List<CapsuleGame> visibleGames;
		
		if (filter == null) {
		    // Arama yoksa hepsini göster
		    visibleGames = games;
		} else {
		    // Arama varsa filtrele
		    visibleGames = new ArrayList<>();
		
		    for (CapsuleGame game : games) {
		        // game.getName() veya game.name (senin değişken adın neyse onu yaz)
		        // contains ile içinde geçiyor mu diye bakıyoruz
		        if (filter.accept(game)) {
		            visibleGames.add(game);
		        }
		    }
		}
		
		// Eğer gösterilecek oyun yoksa (arama sonucu boşsa) çık
		if (visibleGames.isEmpty()) {
		    this.totalPages = 0;
		    this.page = 0;
		    
		    this.add(this.pageBack);
			this.add(this.pageForward);
		    return; 
		}
		
		// --- 2. DİNAMİK HESAPLAMALAR (Artık visibleGames listesini kullanıyoruz) ---
		// Referans boyutları visibleGames'in ilk elemanından alıyoruz
		int gameW = visibleGames.get(0).width;
		int gameH = visibleGames.get(0).height;
		int gap = 20;
		
		int gamesPerRow = Math.max(1, this.width / (gameW + gap));
		int rowsPerPage = Math.max(1, this.height / (gameH + gap));
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
		    this.add(game);
		}
		
		this.add(this.pageBack);
		this.add(this.pageForward);
	}
	
	public void addGame(CapsuleGame game) {
		this.games.add(game);
	}
	
	// Source - https://stackoverflow.com/a
	// Posted by Harshal Parekh
	// Retrieved 2026-01-24, License - CC BY-SA 4.0

	@SuppressWarnings("deprecation")
	static URI getValidURL(String invalidURLString){
	    try {
	        // Convert the String and decode the URL into the URL class
	        URL url = new URL(URLDecoder.decode(invalidURLString, StandardCharsets.UTF_8.toString()));

	        // Use the methods of the URL class to achieve a generic solution
	        URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
	        // return String or
	        // uri.toURL() to return URL object
	        return uri;
	    } catch (URISyntaxException | UnsupportedEncodingException | MalformedURLException ignored) {
	        return URI.create("about:blank");
	    }
	}
	
	protected void loadGameList() {
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
			
			CapsuleGame game = new CapsuleGame(getValidURL(iconUrl), gameId, gameName, authorUsername);
			game.setConsumer(consumer);
			games.add(game);
			
			refreshGamesGrid();
		}
	}
}