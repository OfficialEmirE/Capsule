package net.capsule.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.imageio.ImageIO;

public class ImageUtil {
    public BufferedImage getImageWeb(URL uri) {
        try {
            return ImageIO.read(uri);
        } catch (IOException e) {
            return getMissingIcon();
        }
    }

    public BufferedImage getImageWeb(URI uri) {
        try {
            return ImageIO.read(uri.toURL());
        } catch (IOException e) {
            return getMissingIcon();
        }
    }

    private BufferedImage getMissingIcon() {
        try {
            return ImageIO.read(net.capsule.Capsule.class.getResourceAsStream("/missingIcon.png"));
        } catch (IOException e) {
            return null;
        }
    }

    public BufferedImage scaleImage(BufferedImage img, int width, int height) {
        BufferedImage scaledImg = new BufferedImage(width, height, img.getType());
        Image resizedImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);

        Graphics2D g = scaledImg.createGraphics();
        g.drawImage(resizedImg, 0, 0, null);
        g.dispose();

        return scaledImg;
    }
}
