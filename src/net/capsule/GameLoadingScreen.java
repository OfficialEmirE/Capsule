package net.capsule;

import me.ramazanenescik04.diken.Timer;
import me.ramazanenescik04.diken.gui.screen.Screen;
import me.ramazanenescik04.diken.resource.Bitmap;
import net.capsule.game.CapsuleGame;

public class GameLoadingScreen extends Screen {
	
	@SuppressWarnings("unused")
	private Timer timer;
	private CapsuleGame gameData;

	public GameLoadingScreen(CapsuleGame game) {
		gameData = game;
		//TODO
	}
	
	@Override
	public void render(Bitmap bitmap) {
		super.render(bitmap);
		bitmap.fill(0, 0, bitmap.w, bitmap.h, 0xff000000);
		bitmap.drawText("Loading " + gameData.getGameName() + "...", bitmap.w / 2 - 50, bitmap.h / 2 - 8, 0xffffffff, false);
	}

}
