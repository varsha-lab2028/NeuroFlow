package com.neuroflow.ui.components;

import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class ModuleTile extends JPanel implements ThemeManager.ThemeListener {
    private String emoji, title, subtitle, tag;
    private Color iconBg;
    private boolean hovered;
    private Runnable onClick;

    public ModuleTile(String emoji, String title, String subtitle, String tag, Color iconBg) {
        this.emoji = emoji; 
        this.title = title; 
        this.subtitle = subtitle;
        this.tag = tag; 
        this.iconBg = iconBg;
        
        setOpaque(false); 
        setPreferredSize(new Dimension(400, 82));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            @Override public void mouseClicked(MouseEvent e) { if (onClick != null) onClick.run(); }
        });
        ThemeManager.get().addListener(this);
    }

    public void setOnClick(Runnable r) { onClick = r; }

    @Override
    protected void paintComponent(Graphics g) {
        ThemeManager tm = ThemeManager.get();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();

        // Card background
        g2.setColor(tm.sf());
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 18, 18));
        g2.setColor(tm.bd()); 
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 18, 18));
        
        if (hovered) {
            g2.setColor(new Color(0, 0, 0, 10));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 18, 18));
        }

        // Icon circle
        g2.setColor(iconBg);
        g2.fill(new RoundRectangle2D.Float(16, (h - 52) / 2, 52, 52, 16, 16));

        // Emoji
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        FontMetrics efm = g2.getFontMetrics();
        g2.setColor(Color.BLACK);
        int ey = (h - efm.getHeight()) / 2 + efm.getAscent();
        g2.drawString(emoji, 16 + (52 - efm.stringWidth(emoji)) / 2, ey);

        int tx2 = 84;
        
        // Title
        g2.setFont(tm.bold(16)); 
        g2.setColor(tm.tx());
        g2.drawString(title, tx2, h / 2 - 12);
        
        // Subtitle
        g2.setFont(tm.regular(12)); 
        g2.setColor(tm.sub());
        g2.drawString(subtitle, tx2, h / 2 + 4);
        
        // Tag
        g2.setFont(tm.bold(11)); 
        g2.setColor(tm.ac());
        g2.drawString(tag, tx2, h / 2 + 18);
        
        // Arrow
        g2.setFont(tm.bold(20)); 
        g2.setColor(tm.sub());
        g2.drawString("›", w - 28, h / 2 + 8);
        
        g2.dispose();
    }

    @Override public void onThemeChanged() { repaint(); }
}
