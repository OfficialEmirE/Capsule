package net.capsule.game.node;

import me.ramazanenescik04.diken.game.nodes.Part;
import me.ramazanenescik04.diken.resource.Bitmap;

public class MaterialPart extends Part {
	private static final long serialVersionUID = -1617591267251336899L;
	
	public Material material = Material.Smooth_Stone;
	private static transient Bitmap[][] materialTextures;
	
	public static void setMaterialTexture(Bitmap[][] texture) {
		materialTextures = texture;
	}
	
	public MaterialPart(int x, int y, Material material) {
		super(x, y);
		if (material != null) {
			this.material = material;
		}
		this.name = "MaterialPart-" + material.name();
	}

	public MaterialPart(int x, int y, int width, int height, Material material) {
		super(x, y, width, height);
		if (material != null) {
			this.material = material;
		}
		this.name = "MaterialPart-" + material.name();
	}

	@Override
	public Bitmap render() {
		Bitmap bitmap = new Bitmap(this.aabb.getWidth(), this.aabb.getHeight());
		for (var y = 0; y < (bitmap.h / 16) + 1; y++) {
			for (var x = 0; x < (bitmap.w / 16) + 1; x++) {
				bitmap.blendDraw(material.getTexture(), x * 16, y * 16, this.color);
			}
		}
		return bitmap;
	}
	
	public static enum Material {
		Grass(0, 0),
		Stone(1, 0),
		Dirt(2, 0),
		Planks(3, 0),
		Smooth_Stone(4, 0),
		Smooth_Stone_Double(5, 0),
		Smooth_Stone_Back(6, 0),
		Brick(7, 0),
		TNT(8, 0),
		TNT_Back(9, 0),
		Web(11, 0),
		Rose(12, 0),
		Flower(13, 0),
		Sapling(15, 0),
		Smooth_Stud(0, 5);
		
		public int texX, texY;
		
		Material(int texX, int texY) {
			this.texX = texX;
			this.texY = texY;
		}
		
		public Bitmap getTexture() {
			return MaterialPart.materialTextures[texX][texY];
		}
	}
}
