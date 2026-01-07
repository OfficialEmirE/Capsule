package net.capsule.game;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.entity.MovementPlayer;
import me.ramazanenescik04.diken.game.entity.Player;
import me.ramazanenescik04.diken.resource.Bitmap;
import net.capsule.Capsule;

public class SoloPlayer extends Player {
	
	private MovementPlayer movementPlayer = new MovementPlayer(this);
	
	public SoloPlayer(int x, int y) {
		super(x, y, 57, 64);
		this.name = Capsule.instance.account.getUsername();
	}

	@Override
	public Bitmap render() {
		return super.render();
	}

	@Override
	public void update(World world, DikenEngine engine) {
		movementPlayer.tick();
		super.update(world, engine);
	}

}
