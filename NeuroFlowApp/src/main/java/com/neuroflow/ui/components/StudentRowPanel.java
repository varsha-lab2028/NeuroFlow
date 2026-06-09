package com.neuroflow.ui.components;

import com.neuroflow.model.Student;
import com.neuroflow.ui.ScreenUtils;
import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Ellipse2D;

public class StudentRowPanel extends JPanel implements ThemeManager.ThemeListener {
    private Student student;

    public StudentRowPanel(Student student) {
        this.student = student;
        setOpaque(false);
        ThemeManager.get().addListener(this);
    }

    public void setStudent(Student s) {
        student = s;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        // Height scales with window — width fills the layout
        int h = Math.max(80, (int)(ScreenUtils.windowH(this) * 0.11));
        return new Dimension(Math.max(300, ScreenUtils.contentW(this)), h);
    }

    @Override
    protected void paintComponent(Graphics g) {
        ThemeManager tm = ThemeManager.get();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // All measurements derived from w and h — nothing hardcoded
        int padX       = (int)(w * 0.038);   // left/right inner padding
        int avatarSize = ScreenUtils.avatarSize(this);
        int avatarX    = padX;
        int avatarY    = (int)(h * 0.16);
        int textX      = avatarX + avatarSize + (int)(w * 0.028);
        int rightPad   = (int)(w * 0.07);

        int barH       = Math.max(6, (int)(h * 0.085));
        int barY       = h - barH - (int)(h * 0.14);
        int barW       = w - padX - rightPad;

        // ── Card ─────────────────────────────────────────────────
        g2.setColor(tm.sf());
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));
        g2.setColor(tm.bd());
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 14, 14));

        // ── Avatar circle ─────────────────────────────────────────
        g2.setColor(tm.acl());
        g2.fill(new Ellipse2D.Float(avatarX, avatarY, avatarSize, avatarSize));

        int initFontSize = ScreenUtils.fontSize(this, 13);
        g2.setFont(tm.bold(initFontSize));
        g2.setColor(tm.ac());
        FontMetrics ifm = g2.getFontMetrics();
        String init = student.getInitials();
        int initX = avatarX + (avatarSize - ifm.stringWidth(init)) / 2;
        int initY = avatarY + (avatarSize - ifm.getHeight()) / 2 + ifm.getAscent();
        g2.drawString(init, initX, initY);

        // ── Name ──────────────────────────────────────────────────
        int nameFontSize = ScreenUtils.fontSize(this, 14);
        g2.setFont(tm.bold(nameFontSize));
        g2.setColor(tm.tx());
        FontMetrics nfm = g2.getFontMetrics();
        int nameY = avatarY + nfm.getAscent();
        g2.drawString(student.getName(), textX, nameY);

        // ── Issue badge ───────────────────────────────────────────
        String issue = student.getPrimaryIssue();
        if (issue != null && !issue.isEmpty()) {
            Color bc = student.getWeeklyProgress() < 50 ? tm.er()
                    : student.getWeeklyProgress() < 75 ? tm.warn()
                    : tm.ok();
            int issueFontSize = ScreenUtils.fontSize(this, 10);
            g2.setFont(tm.bold(issueFontSize));
            g2.setColor(bc);
            FontMetrics bfm = g2.getFontMetrics();
            int issueY = nameY + nfm.getDescent() + bfm.getAscent() + (int)(h * 0.02);
            g2.drawString(issue, textX, issueY);
        }

        // ── Progress % — right side ───────────────────────────────
        int pctFontSize = ScreenUtils.fontSize(this, 16);
        g2.setFont(tm.bold(pctFontSize));
        g2.setColor(tm.tx());
        FontMetrics pfm = g2.getFontMetrics();
        String pct = student.getWeeklyProgress() + "%";
        int pctX = w - rightPad - pfm.stringWidth(pct) - (int)(w * 0.04);
        int pctY = avatarY + (avatarSize / 2) + pfm.getAscent() / 2;
        g2.drawString(pct, pctX, pctY);

        // ── Trend arrow ───────────────────────────────────────────
        String trend = student.getTrend();
        if (trend == null) trend = "→";
        Color tc = trend.equals("↑") ? tm.ok()
                : trend.equals("↓") ? tm.er()
                : tm.sub();
        int trendFontSize = ScreenUtils.fontSize(this, 14);
        g2.setFont(tm.bold(trendFontSize));
        g2.setColor(tc);
        FontMetrics tfm = g2.getFontMetrics();
        int trendX = w - rightPad - tfm.stringWidth(trend) + (int)(w * 0.01);
        g2.drawString(trend, trendX, pctY);

        // ── Progress bar ──────────────────────────────────────────
        g2.setColor(tm.alt());
        g2.fill(new RoundRectangle2D.Float(padX, barY, barW, barH, barH, barH));

        int fillW = (int)(barW * student.getWeeklyProgress() / 100.0);
        Color fillC = student.getWeeklyProgress() >= 75 ? tm.ok()
                : student.getWeeklyProgress() >= 50 ? tm.ac()
                : tm.warn();
        if (fillW > 0) {
            g2.setColor(fillC);
            g2.fill(new RoundRectangle2D.Float(padX, barY, fillW, barH, barH, barH));
        }

        g2.dispose();
    }

    @Override public void onThemeChanged() { repaint(); }
}