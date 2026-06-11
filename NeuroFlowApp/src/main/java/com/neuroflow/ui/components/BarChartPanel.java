package com.neuroflow.ui.components;

import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class BarChartPanel extends JPanel implements ThemeManager.ThemeListener {
    private int[] values = new int[7];
    private String[] labels = {"M","T","W","T","F","S","S"};

    public BarChartPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(400, 100));
        ThemeManager.get().addListener(this);
    }

    public void setValues(int[] vals) {
        this.values = vals;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        ThemeManager tm = ThemeManager.get();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = getWidth(), h = getHeight();
        int max = 1;
        for (int v : values) max = Math.max(max, v);
        
        int barH = h - 22, gap = 4;
        int barW = (w - gap * (values.length + 1)) / values.length;
        
        for (int i = 0; i < values.length; i++) {
            int x = gap + (barW + gap) * i;
            int bh = values[i] == 0 ? 4 : (int) (barH * (double) values[i] / max);
            int by = barH - bh;
            
            g2.setColor(values[i] == 0 ? tm.alt() : tm.ac());
            g2.fill(new RoundRectangle2D.Float(x, by, barW, bh, 4, 4));
            
            g2.setFont(tm.regular(10));
            g2.setColor(tm.sub());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(labels[i], 
                    x + (barW - fm.stringWidth(labels[i])) / 2, 
                    h - 4);
        }
        g2.dispose();
    }

    @Override public void onThemeChanged() { repaint(); }
}
