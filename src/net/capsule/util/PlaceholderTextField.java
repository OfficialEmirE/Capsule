package net.capsule.util;

import javax.swing.*;
import java.awt.*;
 
public class PlaceholderTextField extends JTextField {
	private static final long serialVersionUID = 1L;
	private String placeholder;
    private Color placeholderColor = Color.GRAY; // Default placeholder color
 
    // Constructor
    public PlaceholderTextField(int columns) {
        super(columns);
    }
 
    // Getters and Setters
    public String getPlaceholder() {
        return placeholder;
    }
 
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint(); // Redraw component when placeholder changes
    }
 
    public Color getPlaceholderColor() {
        return placeholderColor;
    }
 
    public void setPlaceholderColor(Color placeholderColor) {
        this.placeholderColor = placeholderColor;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Draw the default text field (background, border, etc.)
     
        // Draw placeholder only if conditions are met
        if (placeholder != null && !placeholder.isEmpty() && getText().isEmpty() && !hasFocus()) {
            Graphics2D g2 = (Graphics2D) g.create(); // Create a copy of the graphics context
            g2.setColor(placeholderColor); // Set placeholder color
     
            // Get font metrics to position the text correctly
            FontMetrics fm = g2.getFontMetrics();
            int x = getInsets().left; // Left padding
            int y = fm.getAscent() + getInsets().top; // Vertical position (top padding + font ascent)
     
            // Draw the placeholder text
            g2.drawString(placeholder, x, y);
            g2.dispose(); // Clean up the graphics context
        }
    }
}
