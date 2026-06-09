package com.neuroflow.ui.components;

import com.neuroflow.ui.ScreenUtils;
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
        this.emoji    = emoji;
        this.title    = title;
        this.subtitle = subtitle;
        this.tag      = tag;
        this.iconBg   = iconBg;

        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            @Override public void mouseClicked(MouseEvent e) { if (onClick != null) onClick.run(); }
        });
        ThemeManager.get().addListener(this);
    }

    public void setOnClick(Runnable r) { onClick = r; }

    @Override
    public Dimension getPreferredSize() {
        // Height scales with window size — width fills whatever the layout gives
        return new Dimension(
                Math.max(300, ScreenUtils.contentW(this)),
                ScreenUtils.moduleH(this));
    }

    @Override
    protected void paintComponent(Graphics g) {
        ThemeManager tm = ThemeManager.get();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // All sizes derived from actual component height — scales automatically
        int iconSize  = (int)(h * 0.65);   // icon box is 65% of tile height
        int iconX     = (int)(w * 0.038);  // left padding proportional to width
        int iconY     = (h - iconSize) / 2;
        int iconR     = iconSize / 4;      // corner radius scales with icon
        int emojiSize = (int)(iconSize * 0.50);
        int textX     = iconX + iconSize + (int)(w * 0.032);
        int arrowX    = w - (int)(w * 0.07);

        // ── Card background ───────────────────────────────────────
        g2.setColor(tm.sf());
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 18, 18));
        g2.setColor(tm.bd());
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 18, 18));

        if (hovered) {
            g2.setColor(new Color(0, 0, 0, 10));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 18, 18));
        }

        // ── Icon box ──────────────────────────────────────────────
        g2.setColor(iconBg);
        g2.fill(new RoundRectangle2D.Float(iconX, iconY, iconSize, iconSize, iconR, iconR));

        // ── Emoji ─────────────────────────────────────────────────
        g2.setFont(new Font("Apple Color Emoji", Font.PLAIN, emojiSize));
        FontMetrics efm = g2.getFontMetrics();
        g2.setColor(Color.BLACK);
        int ex = iconX + (iconSize - efm.stringWidth(emoji)) / 2;
        int ey = iconY + (iconSize - efm.getHeight()) / 2 + efm.getAscent();
        g2.drawString(emoji, ex, ey);

        // ── Text block — vertically centred in the tile ───────────
        int titleSize    = ScreenUtils.fontSize(this, 16);
        int subtitleSize = ScreenUtils.fontSize(this, 12);
        int tagSize      = ScreenUtils.fontSize(this, 11);

        // Calculate total text block height to centre it
        Font titleFont    = tm.bold(titleSize);
        Font subtitleFont = tm.regular(subtitleSize);
        Font tagFont      = tm.bold(tagSize);

        FontMetrics tfm  = g2.getFontMetrics(titleFont);
        FontMetrics sfm  = g2.getFontMetrics(subtitleFont);
        FontMetrics gfm  = g2.getFontMetrics(tagFont);

        int lineGap    = (int)(h * 0.045);
        int blockH     = tfm.getHeight() + lineGap + sfm.getHeight() + lineGap + gfm.getHeight();
        int blockTop   = (h - blockH) / 2;

        int titleY    = blockTop + tfm.getAscent();
        int subtitleY = titleY + tfm.getDescent() + lineGap + sfm.getAscent();
        int tagY      = subtitleY + sfm.getDescent() + lineGap + gfm.getAscent();

        g2.setFont(titleFont);
        g2.setColor(tm.tx());
        g2.drawString(title, textX, titleY);

        g2.setFont(subtitleFont);
        g2.setColor(tm.sub());
        g2.drawString(subtitle, textX, subtitleY);

        g2.setFont(tagFont);
        g2.setColor(tm.ac());
        g2.drawString(tag, textX, tagY);

        // ── Arrow ─────────────────────────────────────────────────
        int arrowSize = ScreenUtils.fontSize(this, 20);
        g2.setFont(tm.bold(arrowSize));
        g2.setColor(tm.sub());
        FontMetrics afm = g2.getFontMetrics();
        g2.drawString("›", arrowX, h / 2 + afm.getAscent() / 2);

        g2.dispose();
    }

    @Override public void onThemeChanged() { repaint(); }
}