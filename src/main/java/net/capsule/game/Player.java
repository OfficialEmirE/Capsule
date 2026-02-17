package net.capsule.game;

import org.json.JSONObject;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.entity.Humanoid;
import me.ramazanenescik04.diken.game.entity.MovementPlayer;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.tools.PixelToColor;
import me.ramazanenescik04.diken.tools.Utils;

public class Player extends Humanoid {
	private static final long serialVersionUID = -5395842731409825680L;
	protected transient MovementPlayer movementPlayer = new MovementPlayer(this);
	
	public transient Bitmap body, hand, defaultFace;

	public Player(int x, int y) {
		super(x, y, 57, 64);
		this.setName("Player");
		this.aabb.setBounds(12, 0, 32, 64);
		this.setUserAvatar("Player");
		this.canMove = false;
	}

	@Override
	public Bitmap render() {
		Bitmap bitmap = new Bitmap(57, 64);
		bitmap.draw(this.body, 12, 0);
	    playHandAnimation(bitmap);
	    if (movementPlayer != null) {
	    	bitmap.draw(movementPlayer.viewType == 0 ? this.defaultFace.opposite(false) : this.defaultFace, 12, 6);
	    } else {
	    	bitmap.draw(this.defaultFace, 12, 6);
	    }
		return bitmap;
	}
	
	protected void playHandAnimation(Bitmap bitmap) {
		bitmap.draw(hand, 0, 34);
		bitmap.draw(hand, 49, 34);
	}

	public void setUserAvatar(String username) {
	    ArrayBitmap default_avatar = (ArrayBitmap)ResourceLocator.getResource(new ResourceLocator.ResourceKey("capsule", "default_avatar"));
	    JSONObject userJSON = new JSONObject(Utils.getWebData("http://capsule.net.tr/api/v1/avatar/?type=get&username=" + username.trim()));
	    
	    this.body = default_avatar.getBitmap(0, 0);
	    this.hand = default_avatar.getBitmap(0, 1);
	    this.defaultFace = default_avatar.getBitmap(0, 2);
	    
	    if (userJSON.getString("status").equals("error")) {
	    	DikenEngine.errorLog("Error: " + userJSON.getString("message"));
	    	return;
	    } 
	    JSONObject avatarData = new JSONObject(userJSON.getString("avatar"));
	    int value = Integer.parseInt(avatarData.getString("color").substring(1), 16);
	    this.color = PixelToColor.colorAddAlpha(value, 255);
	    this.body = this.body.replaceColor(0xffffffff, this.color);
	}

	@Override
	protected void reloadNode() {
		 ArrayBitmap default_avatar = (ArrayBitmap)ResourceLocator.getResource(new ResourceLocator.ResourceKey("capsule", "default_avatar"));
		 
		 this.body = default_avatar.getBitmap(0, 0);
		 this.hand = default_avatar.getBitmap(0, 1);
		 this.defaultFace = default_avatar.getBitmap(0, 2);
		 
		 this.body = this.body.replaceColor(0xffffffff, this.color);
	}

}
