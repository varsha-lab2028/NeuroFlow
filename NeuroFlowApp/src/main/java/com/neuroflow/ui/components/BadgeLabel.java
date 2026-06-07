package com.neuroflow.ui.components;

import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class BadgeLabel extends JLabel implements ThemeManager.ThemeListener {
    public enum BadgeType { OK, WARN, ERROR, ACCENT }
    private BadgeType type;

    public BadgeLabel(String text, BadgeType type) {
        super(text);
        this.type = type;
        setOpaque(false);
        setFont(ThemeManager.get().bold(11));
        ThemeManager.get().addListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        ThemeManager tm = ThemeManager.get();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Color bg, fg;
        switch (type) {
            case OK     -> { bg = tm.withAlpha(tm.ok(), 36);   fg = tm.ok(); }
            case WARN   -> { bg = tm.withAlpha(tm.warn(), 36); fg = tm.warn(); }
            case ERROR  -> { bg = tm.withAlpha(tm.er(), 36);   fg = tm.er(); }
            default     -> { bg = tm.acl();                    fg = tm.ac(); }
        }
        
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
        
        g2.setFont(getFont());
        g2.setColor(fg);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(getText(), 
                (getWidth() - fm.stringWidth(getText())) / 2,
                (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        return new Dimension(fm.stringWidth(getText()) + 18, fm.getHeight() + 6);
    }

    @Override
    public void onThemeChanged() {
        repaint();
    }

    public void setType(BadgeType t) {
        type = t;
        repaint();
    }
}
