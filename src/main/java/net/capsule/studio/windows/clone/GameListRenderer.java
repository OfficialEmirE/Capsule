package net.capsule.studio.windows.clone;

import java.awt.Component;

import javax.swing.*;

class GameListRenderer extends DefaultListCellRenderer {
	private static final long serialVersionUID = 1L;

	@Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                                                  boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        if (value instanceof GameItem) {
            GameItem game = (GameItem) value;
            label.setText(game.getName());
            label.setIcon(game.getIcon());
            label.setIconTextGap(15); // İkon ve metin arası boşluk
            label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // İç padding
        }
        return label;
    }
}