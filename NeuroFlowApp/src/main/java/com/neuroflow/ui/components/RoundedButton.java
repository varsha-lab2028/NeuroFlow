package com.neuroflow.ui.components;

import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class RoundedButton extends JButton implements ThemeManager.ThemeListener {
    public enum Style { PRIMARY, SUCCESS, GHOST, MUTED, DANGER }
    private Style style;
    private int radius = 14;
    private boolean hovered = false;
    private boolean pressed = false;

    public RoundedButton(String text, Style style) {
        super(text);
        this.style = style;
        setOpaque(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFont(ThemeManager.get().bold(14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            @Override public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
            @Override public void mouseReleased(MouseEvent e){ pressed = false; repaint(); }
        });
        ThemeManager.get().addListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        ThemeManager tm = ThemeManager.get();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color bg; Color fg;
        switch (style) {
            case PRIMARY -> { bg = tm.ac();   fg = Color.WHITE; }
            case SUCCESS -> { bg = tm.ok();   fg = Color.WHITE; }
            case DANGER  -> { bg = tm.er();   fg = Color.WHITE; }
            case MUTED   -> { bg = tm.alt();  fg = tm.tx(); }
            default      -> { bg = new Color(0,0,0,0); fg = tm.tx(); }
        }
        float scale = pressed ? 0.97f : 1.0f;
        int w = (int)(getWidth() * scale), h = (int)(getHeight() * scale);
        int x = (getWidth() - w) / 2, y = (getHeight() - h) / 2;

        if (style == Style.GHOST) {
            if (hovered) {
                g2.setColor(tm.alt());
                g2.fill(new RoundRectangle2D.Float(x, y, w, h, radius, radius));
            }
            g2.setColor(tm.bd());
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1, h - 1, radius, radius));
        } else {
            Color fill = hovered ? bg.darker() : bg;
            g2.setColor(fill);
            g2.fill(new RoundRectangle2D.Float(x, y, w, h, radius, radius));
        }
        g2.setFont(getFont());
        g2.setColor(fg);
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (w - fm.stringWidth(getText())) / 2;
        int ty = y + (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), tx, ty);
        g2.dispose();
    }

    @Override
    public void onThemeChanged() {
        repaint();
    }

    public void setStyle(Style s) {
        style = s;
        repaint();
    }
}
