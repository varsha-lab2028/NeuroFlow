package com.neuroflow.ui.components;

import com.neuroflow.ui.ScreenUtils;
import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class RoundedButton extends JButton implements ThemeManager.ThemeListener {
    public enum Style { PRIMARY, SUCCESS, GHOST, MUTED, DANGER }
    private Style style;
    private boolean hovered = false;
    private boolean pressed = false;

    public RoundedButton(String text, Style style) {
        super(text);
        this.style = style;
        setOpaque(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        updateFont();
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            @Override public void mousePressed(MouseEvent e) { pressed = true;  repaint(); }
            @Override public void mouseReleased(MouseEvent e){ pressed = false; repaint(); }
        });
        ThemeManager.get().addListener(this);
    }

    // ── Font and size update — called on theme change and on paint ─
    private void updateFont() {
        setFont(ThemeManager.get().bold(ScreenUtils.fontSize(this, 14)));
    }

    @Override
    public Dimension getPreferredSize() {
        // Height scales with window, width is whatever the layout gives
        return new Dimension(
                super.getPreferredSize().width,
                ScreenUtils.buttonH(this));
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Refresh font size every paint so it updates on window resize
        updateFont();

        ThemeManager tm = ThemeManager.get();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Radius scales with button height — always looks proportional
        int radius = Math.max(10, getHeight() / 3);

        Color bg, fg;
        switch (style) {
            case PRIMARY -> { bg = tm.ac();  fg = Color.WHITE; }
            case SUCCESS -> { bg = tm.ok();  fg = Color.WHITE; }
            case DANGER  -> { bg = tm.er();  fg = Color.WHITE; }
            case MUTED   -> { bg = tm.alt(); fg = tm.tx(); }
            default      -> { bg = new Color(0, 0, 0, 0); fg = tm.tx(); }
        }

        float scale = pressed ? 0.97f : 1.0f;
        int w = (int)(getWidth()  * scale);
        int h = (int)(getHeight() * scale);
        int x = (getWidth()  - w) / 2;
        int y = (getHeight() - h) / 2;

        if (style == Style.GHOST) {
            if (hovered) {
                g2.setColor(tm.alt());
                g2.fill(new RoundRectangle2D.Float(x, y, w, h, radius, radius));
            }
            g2.setColor(tm.bd());
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1, h - 1, radius, radius));
        } else {
            g2.setColor(hovered ? bg.darker() : bg);
            g2.fill(new RoundRectangle2D.Float(x, y, w, h, radius, radius));
        }

        // Draw text centred in the button
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
        updateFont();
        repaint();
    }

    public void setStyle(Style s) {
        style = s;
        repaint();
    }
}