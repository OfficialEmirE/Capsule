package net.capsule.update;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void paintBorder(Graphics g) {
		super.paintBorder(g);
		
		g.setColor(Color.black);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		g.setColor(Color.white);
		g.drawString("Hello!", 2, 22);
	}
}
