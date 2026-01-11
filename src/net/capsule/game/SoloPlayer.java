package net.capsule.game;

import org.json.JSONObject;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.Animation;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.entity.MovementPlayer;
import me.ramazanenescik04.diken.game.entity.Player;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.tools.PixelToColor;
import me.ramazanenescik04.diken.tools.Utils;
import net.capsule.Capsule;

public class SoloPlayer extends Player {
	private MovementPlayer movementPlayer = new MovementPlayer(this);
	
	public Bitmap body, hand, defaultFace;
	public int color = 0xffffffff;
	private Animation leftWalkAnim, rightWalkAnim, idleAnim;
	
	public SoloPlayer(int x, int y) {
		super(x, y, 57, 64);
		this.name = Capsule.instance.account.getUsername();
		this.setUserAvatar(Capsule.instance.account.getUsername());
		this.aabb.setBounds(12, 0, 32, 64);
	}

	@Override
	public Bitmap render() {
		Bitmap bitmap = new Bitmap(57, 64);
		bitmap.draw(this.body, 12, 0);
	    Animation currentAnim = this.getWalkAnimation();
	    if (currentAnim != null) {
	      Bitmap handFrame = currentAnim.getCurrentFrame();
	      bitmap.draw(handFrame, 0, 0);
	    }
	    bitmap.draw(movementPlayer.viewType == 0 ? this.defaultFace.opposite(false) : this.defaultFace, 12, 6);
		return bitmap;
	}

	@Override
	public void update(World world, DikenEngine engine) {
		movementPlayer.tick();
		if (movementPlayer.isMoving) {
			playWalkAnimation();
		} else {
			resetWalkAnimation();
		}
		super.update(world, engine);
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

	public void setUserAvatar(String username) {
	    ArrayBitmap default_avatar = (ArrayBitmap)ResourceLocator.getResource(new ResourceLocator.ResourceKey("capsule", "default_avatar"));
	    JSONObject userJSON = new JSONObject(Utils.getWebData("http://capsule.net.tr/api/v1/avatar/?type=get&username=" + username.trim()));
	    
	    this.body = default_avatar.getBitmap(0, 0);
	    this.hand = default_avatar.getBitmap(0, 1);
	    this.defaultFace = default_avatar.getBitmap(0, 2);
	    
	    if (!userJSON.getString("status").equals("success")) {
	    	DikenEngine.errorLog("Error: " + userJSON.getString("message"));
	    } 
	    JSONObject avatarData = new JSONObject(userJSON.getString("avatar"));
	    int value = Integer.parseInt(avatarData.getString("color").substring(1), 16);
	    this.color = PixelToColor.colorAddAlpha(value, 255);
	    this.body = this.body.replaceColor(-1, this.color);
	    this.hand = this.hand.replaceColor(-1, this.color);
	    
	    this.leftWalkAnim = (Animation)ResourceLocator.getResource(new ResourceLocator.ResourceKey("capsule", "leftWalkAnim"));
	    this.rightWalkAnim = (Animation)ResourceLocator.getResource(new ResourceLocator.ResourceKey("capsule", "rightWalkAnim"));
	}
}
