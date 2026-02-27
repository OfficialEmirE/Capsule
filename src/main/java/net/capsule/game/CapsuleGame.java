package net.capsule.game;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.util.function.Consumer;

import me.ramazanenescik04.diken.gui.compoment.Button;
import me.ramazanenescik04.diken.gui.compoment.LinkButton;
import me.ramazanenescik04.diken.gui.compoment.LinkText;
import me.ramazanenescik04.diken.gui.compoment.Panel;
import me.ramazanenescik04.diken.resource.Bitmap;
import net.capsule.Capsule;

public class CapsuleGame extends Panel {
	
	private static final long serialVersionUID = -8498008438844114420L;
	private final int gameId;
	private int activePlayers;
	
	private String gameName;
	private String authorUsername;
	
	private Bitmap icon;
	
	private Consumer<CapsuleGame> consumer;
	
	public CapsuleGame(Bitmap logo, int gameId, String gameName, String authorUsername) {
		super(0, 0, 256, 256);
		
		this.gameId = gameId;
		this.gameName = gameName;
		this.authorUsername = authorUsername;
		
		if (logo != null) {
			this.icon = logo;
		} else {
			this.icon = new Bitmap(64, 64);
		}
		
		Button playButton = new Button("Play", 10, 256 - 42, 96, 32).setRunnable(() -> {
			if (consumer != null) {
				consumer.accept(this);
			}
		}).setTextColor(0xffffffff);
		
		playButton.bColor = 0xff00ff00;
		this.add(playButton);
		
		LinkButton gamePageButton = new LinkButton("See Game Page", 256 - 110, 256 - 42, 96, 32).setURI(URI.create("http://capsule.net.tr/game.php?id=" + gameId));	
		this.add(gamePageButton);
		
		LinkText author = new LinkText("By: " + authorUsername, 20, 170).setURI(URI.create("http://capsule.net.tr/profile/?username=" + authorUsername));
		this.add(author);
	}
	
	public CapsuleGame(int gameId, String gameName, String authorUsername) {
		this(new Bitmap(64, 64), gameId, gameName, authorUsername);
	}
	
	public CapsuleGame(BufferedImage logo, int gameId, String gameName, String authorUsername) {
		this(Bitmap.toBitmap(logo), gameId, gameName, authorUsername);
	}
	
	public CapsuleGame(URI logoURI, int gameId, String gameName, String authorUsername) {
		this(new Bitmap(64, 64), gameId, gameName, authorUsername);
		
		Thread.startVirtualThread(() -> {
			icon = Bitmap.toBitmap(Capsule.instance.getImageUtil().getImageWeb(logoURI));
		});
	}
	
	public CapsuleGame(URL logoURL, int gameId, String gameName, String authorUsername) {
		this(new Bitmap(64, 64), gameId, gameName, authorUsername);
		
		Thread.startVirtualThread(() -> {
			icon = Bitmap.toBitmap(Capsule.instance.getImageUtil().getImageWeb(logoURL));
		});
	}
	
	public void setConsumer(Consumer<CapsuleGame> a) {
		this.consumer = a;
	}
	
	public int getGameId() {
		return gameId;
	}
	
	public String getGameName() {
		return gameName;
	}
	
	public String toString() {
		return "" + gameName + " (" + gameId + ")";
	}
	
	public String getAuthorUsername() {
		return authorUsername;
	}
	
	public int getActivePlayers() {
		return activePlayers;
	}
	
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		CapsuleGame other = (CapsuleGame) obj;
		return gameId == other.gameId;
	}
	
	public Bitmap getGameIcon() {
		return icon;
	}
	
	public Bitmap render() {
		Bitmap rendered = new Bitmap(width, height);
		rendered.clear(0xff3b3b3b);
		rendered.box(0, 0, width - 1, height - 1, 0xFFe9e9e9);
		
		rendered.draw(icon.resize(128, 128), (width - 128) / 2, 20);
		
		rendered.drawLine(0, 155, width - 1, 155, 0xffffffff, 1);
		
		rendered.drawText(gameName, 20, 160, false);
		rendered.drawText("Active: " + "0", 20, 180, false);
		
		rendered.draw(super.render(), 0, 0);
		return rendered;
	}
}
