package com.neuroflow.ui.components;

import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class CustomProgressBar extends JPanel implements ThemeManager.ThemeListener {
    private int value = 0; // 0-100
    private Color fillColor;

    public CustomProgressBar() {
        setOpaque(false);
        setPreferredSize(new Dimension(100, 8));
        ThemeManager.get().addListener(this);
    }

    public void setValue(int v) {
        value = Math.max(0, Math.min(100, v));
        repaint();
    }

    public void setFillColor(Color c) {
        fillColor = c;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        ThemeManager tm = ThemeManager.get();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int h = getHeight();
        int w = getWidth();
        
        // Track
        g2.setColor(tm.alt());
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
        
        // Fill
        int fillW = (int) (w * value / 100.0);
        if (fillW > 0) {
            g2.setColor(fillColor != null ? fillColor : tm.ac());
            g2.fill(new RoundRectangle2D.Float(0, 0, fillW, h, h, h));
        }
        g2.dispose();
    }

    @Override
    public void onThemeChanged() {
        repaint();
    }
}
