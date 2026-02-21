package net.capsule.game;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.Animation;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import net.capsule.Capsule;

public class SoloPlayer extends BasePlayer {
	private static final long serialVersionUID = -8240969167111897631L;
	
	private transient Animation leftWalkAnim, rightWalkAnim, idleAnim;
	
	public SoloPlayer(int x, int y) {
		super(x, y);
		this.name = Capsule.instance.account.getUsername();
		this.setUserAvatar(Capsule.instance.account.getUsername());
	}
	
	@Override
	protected void playHandAnimation(Bitmap bitmap) {
		Animation currentAnim = (movementPlayer.isMoving) ? this.getWalkAnimation() : this.getIdleAnimation();
	    if (currentAnim != null) {
	      Bitmap handFrame = currentAnim.getCurrentFrame();
	      bitmap.draw(handFrame, 0, 0);
	    }
	}

	@Override
	public void update(World world, DikenEngine engine) {
		if (this.getSelectedTool() == null) {
			if (movementPlayer.isMoving) {
				playWalkAnimation();
				this.idleAnim.setCurrentFrame(0);
			} else {
				resetWalkAnimation();
				this.idleAnim.update(System.currentTimeMillis());
			}
		} else {
			resetWalkAnimation();
			this.idleAnim.setCurrentFrame(0);
		}
		movementPlayer.tick(engine);
		super.update(world, engine);
	}
	@Override
	public void setUserAvatar(String username) {
		super.setUserAvatar(username);
		
		this.leftWalkAnim = (Animation)ResourceLocator.getResource(new ResourceLocator.ResourceKey("capsule", "leftWalkAnim"));
	    this.rightWalkAnim = (Animation)ResourceLocator.getResource(new ResourceLocator.ResourceKey("capsule", "rightWalkAnim"));
	    this.idleAnim = (Animation)ResourceLocator.getResource(new ResourceLocator.ResourceKey("capsule", "idleAnim"));
	    this.idleAnim.setFPS(2);
	}

	@Override
	public Animation getIdleAnimation() {
		return this.idleAnim;
	}

	@Override
	public Animation getWalkAnimation() {
		return (movementPlayer.viewType == 0) ? this.leftWalkAnim : this.rightWalkAnim;
	}
	
	public void playWalkAnimation() {
	    if (this.movementPlayer.viewType == 0) {
	      this.leftWalkAnim.update(System.currentTimeMillis());
	    } else {
	      this.rightWalkAnim.update(System.currentTimeMillis());
	    } 
	}
	  
	public void resetWalkAnimation() {
	    this.leftWalkAnim.setCurrentFrame(0);
	    this.rightWalkAnim.setCurrentFrame(0);
	}
}
