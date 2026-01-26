package net.capsule.studio;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.gui.compoment.Button;
import me.ramazanenescik04.diken.gui.compoment.CheckBox;
import me.ramazanenescik04.diken.gui.compoment.TextField;
import me.ramazanenescik04.diken.gui.window.Window;

public class PartPropertiesWindow extends Window {
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	private Button typeBack, typeFront, type;
	@SuppressWarnings("unused")
	private TextField name;
	@SuppressWarnings("unused")
	private CheckBox hitbox;
	@SuppressWarnings("unused")
	private int color;
	
	@SuppressWarnings("unused")
	private Node modifiedNode;

	public PartPropertiesWindow(int x, int y, Node selectedNode) {
		super(x, y, 150, 250);
		this.modifiedNode = selectedNode;
	}
	
	public void open() {
		
	}
	
}
