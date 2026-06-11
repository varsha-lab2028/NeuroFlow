package com.neuroflow.ui.components;

import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedPanel extends JPanel implements ThemeManager.ThemeListener {
    private int radius;
    private boolean useSurface; // true = sf color, false = alt color

    public RoundedPanel(int radius) {
        this(radius, true);
    }

    public RoundedPanel(int radius, boolean useSurface) {
        this.radius = radius;
        this.useSurface = useSurface;
        setOpaque(false);
        ThemeManager.get().addListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        ThemeManager tm = ThemeManager.get();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(useSurface ? tm.sf() : tm.alt());
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
        // Border
        g2.setColor(tm.bd());
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, radius, radius));
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public void onThemeChanged() {
        repaint();
    }

    public void setRadius(int r) {
        this.radius = r;
        repaint();
    }
}
