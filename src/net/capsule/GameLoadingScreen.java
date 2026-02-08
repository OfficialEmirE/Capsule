package net.capsule;

import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.gui.compoment.ProgressBar;
import me.ramazanenescik04.diken.gui.screen.Screen;
import me.ramazanenescik04.diken.gui.window.OptionWindow;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import net.capsule.game.GameScreen;
import net.capsule.gui.GameSelectionScreen;
import net.capsule.util.Util;

public class GameLoadingScreen extends Screen {
	
	private ProgressBar loadingBar;
	private int gameData = -1;
	private String progressString = "Loading...";

	public GameLoadingScreen(int game) {
		gameData = game;
	}
	
	@Override
	public void openScreen() {
		this.loadingBar = new ProgressBar(2, this.engine.getScaledHeight() - 20, this.engine.getScaledWidth() - 4, 16);
		this.loadingBar.color = 0xff4fff4f;
		this.loadingBar.color2 = 0xff33a633;
		
		this.getContentPane().add(loadingBar);
		//End: Download Game
		Thread.startVirtualThread(() -> {
			World theWorld = Util.downloadGame(Capsule.instance.account.getApiKey().toString(), gameData, (progressData) -> {
				this.loadingBar.text = progressData.toString();
				this.loadingBar.value = progressData.percent();
				this.progressString = "Loading Game - " + progressData.progressName();
			});
			
			if (theWorld == null) {
				OptionWindow.showMessageNoWait(this.progressString, "Error", OptionWindow.ERROR_MESSAGE, 0, null);
				this.engine.setCurrentScreen(new GameSelectionScreen());
				return;
			}
			
			theWorld.addResource("materials", ResourceLocator.getResource(new ResourceLocator.ResourceKey("capsule", "materials")));
			this.engine.setCurrentScreen(new GameScreen(new GameSelectionScreen(), theWorld));
		});
	}

	@Override
	public void resized() {
		this.loadingBar.setBounds(2, this.engine.getScaledHeight() - 20, this.engine.getScaledWidth() - 4, 16);
	}
	
	@Override
	public void tick() {
		super.tick();
	}

	@Override
	public void render(Bitmap bitmap) {
		bitmap.fill(0, 0, bitmap.w, bitmap.h, 0xff000000);
		super.render(bitmap);
		bitmap.drawText(progressString, 2, this.engine.getScaledHeight() - 30, false);
	}

}
