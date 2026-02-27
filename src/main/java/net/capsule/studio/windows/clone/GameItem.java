package net.capsule.studio.windows.clone;

import javax.swing.*;
import net.capsule.util.Util;

import java.awt.Image;
import java.net.URI;

class GameItem {
    private String name;
    private Icon icon;

    public GameItem(String name, URI iconPath) {
        this.name = name;
        // İkonu yükle (Dosya yolu belirtilmeli)
        Image scaledImage = Util.getImageWeb(iconPath).getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        this.icon = new ImageIcon(scaledImage);
    }

    public String getName() { return name; }
    public Icon getIcon() { return icon; }
}