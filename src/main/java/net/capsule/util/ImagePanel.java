package net.capsule.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URI;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private BufferedImage image;
	
	public ImagePanel(BufferedImage image) {
		this.image = image;
		setPreferredSize(new java.awt.Dimension(image.getWidth(), image.getHeight()));
	}
	
	public ImagePanel(URI imageURI) {
		this.image = Util.getImageWeb(imageURI);
		setPreferredSize(new java.awt.Dimension(image.getWidth(), image.getHeight()));
	}
	
	public ImagePanel() {
		this.image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		setPreferredSize(new java.awt.Dimension(10, 10));
	}
	
	@Override
	public void paintComponent(java.awt.Graphics g) {
		super.paintComponent(g);
		
		if (image == null) return;

        Graphics2D g2d = (Graphics2D) g;

        // Kaliteli render ayarları
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                			 RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                             RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        int panelWidth = getWidth();
        
        if (panelWidth <= 0 || getHeight() <= 0) {
            return; // layout hazır değil
        }

        // Oran korunur
        double scale = (double) panelWidth / image.getWidth();
        int newHeight = (int) (image.getHeight() * scale);

        g2d.drawImage(
        		image,
                0, 0,
                panelWidth, newHeight,
                null
        );
	}

}
