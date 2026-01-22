package net.capsule.update;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private List<BufferedImage> images;
	private long startTimer, timer;
	
	@Override
	protected void paintBorder(Graphics g) {
		super.paintBorder(g);
		
		g.setColor(Color.black);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
	}
}
