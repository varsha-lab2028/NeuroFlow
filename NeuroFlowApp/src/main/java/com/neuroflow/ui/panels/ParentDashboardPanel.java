package com.neuroflow.ui.panels;

import com.neuroflow.AppState;
import com.neuroflow.model.Student;
import com.neuroflow.service.PracticeSessionService;
import com.neuroflow.ui.MainFrame;
import com.neuroflow.ui.ScreenUtils;
import com.neuroflow.ui.components.*;
import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;
import java.util.LinkedHashMap;

public class ParentDashboardPanel extends BasePanel {

    public ParentDashboardPanel(MainFrame frame) {
        super(frame, "Parent Dashboard", true, false, null);
        refresh();
    }

    @Override
    public void refresh() {
        contentArea.removeAll();
        ThemeManager tm = ThemeManager.get();
        Student s = AppState.get().getCurrentStudent();

        // ── Greeting ──────────────────────────────────────────────
        addFull(label("Hi there 👋",
                ScreenUtils.fontSize(this, 20), true, tm.tx()));
        addGap(ScreenUtils.gap(this) / 3);
        addFull(label("Here's how practice went today",
                ScreenUtils.fontSize(this, 13), false, tm.sub()));
        addGap(ScreenUtils.gap(this));

        // ── Today's practice card ─────────────────────────────────
        if (s != null) {
            int dur = PracticeSessionService.get().sumDurationToday(s.getStudentId());
            int att = PracticeSessionService.get().countTodayAttempts(s.getStudentId());
            boolean hasPracticed = dur > 0 || att > 0;

            RoundedPanel todayCard = new RoundedPanel(18);
            todayCard.setLayout(new BoxLayout(todayCard, BoxLayout.Y_AXIS));
            int cp = ScreenUtils.cardPad(this);
            todayCard.setBorder(new EmptyBorder(cp, cp, cp, cp));
            todayCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                    Math.max(140, (int)(ScreenUtils.windowH(this) * 0.21))));
            todayCard.setAlignmentX(LEFT_ALIGNMENT);

            JPanel topRow = new JPanel(new BorderLayout());
            topRow.setOpaque(false);
            topRow.add(label("📅 Today's practice",
                            ScreenUtils.fontSize(this, 15), true, tm.tx()),
                    BorderLayout.WEST);
            BadgeLabel badge = new BadgeLabel(
                    hasPracticed ? "Completed" : "No practice yet",
                    hasPracticed ? BadgeLabel.BadgeType.OK
                            : BadgeLabel.BadgeType.WARN);
            topRow.add(badge, BorderLayout.EAST);
            todayCard.add(topRow);
            todayCard.add(Box.createVerticalStrut(ScreenUtils.gap(this)));

            // Stats mini grid
            int gridGap = Math.max(6, (int)(ScreenUtils.windowW(this) * 0.016));
            JPanel statsGrid = new JPanel(new GridLayout(2, 2, gridGap, gridGap));
            statsGrid.setOpaque(false);
            statsGrid.add(miniStatPanel(formatDur(dur), "Time spent"));
            statsGrid.add(miniStatPanel(att + " tries", "Attempts"));
            statsGrid.add(miniStatPanel(
                    s.getCurrentLetter().toUpperCase() + " letters",
                    "Practised today"));
            statsGrid.add(miniStatPanel("", ""));
            todayCard.add(statsGrid);
            addFull(todayCard);
            addGap(ScreenUtils.gap(this));
        }

        // ── Common mix-ups card ───────────────────────────────────
        RoundedPanel mixCard = new RoundedPanel(18);
        mixCard.setLayout(new BoxLayout(mixCard, BoxLayout.Y_AXIS));
        int cp = ScreenUtils.cardPad(this);
        mixCard.setBorder(new EmptyBorder(cp, cp, cp, cp));
        mixCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                Math.max(160, (int)(ScreenUtils.windowH(this) * 0.24))));
        mixCard.setAlignmentX(LEFT_ALIGNMENT);

        mixCard.add(label("🔄 Common mix-ups",
                ScreenUtils.fontSize(this, 15), true, tm.tx()));
        mixCard.add(Box.createVerticalStrut(ScreenUtils.gap(this) / 3));
        mixCard.add(label("Totally normal — these letters look very similar!",
                ScreenUtils.fontSize(this, 12), false, tm.sub()));
        mixCard.add(Box.createVerticalStrut(ScreenUtils.gap(this)));

        Map<String, Integer> mixups = new LinkedHashMap<>();
        mixups.put("b / d", 60);
        mixups.put("p / q", 30);

        if (s != null) {
            Map<String, Integer> real =
                    PracticeSessionService.get().weeklyErrorTotals();
            if (!real.isEmpty()) mixups.clear();
            real.forEach((k, v) -> {
                if (k.contains("reversal"))
                    mixups.put(k.replace(" reversal", ""), v * 3);
            });
        }

        for (Map.Entry<String, Integer> e : mixups.entrySet()) {
            mixCard.add(trendRow(e.getKey(), e.getValue()));
            mixCard.add(Box.createVerticalStrut(ScreenUtils.gap(this) / 2));
        }
        addFull(mixCard);
        addGap(ScreenUtils.gap(this));

        // ── Tips card ─────────────────────────────────────────────
        RoundedPanel tipsCard = new RoundedPanel(18);
        tipsCard.setLayout(new BoxLayout(tipsCard, BoxLayout.Y_AXIS));
        tipsCard.setBorder(new EmptyBorder(cp, cp, cp, cp));
        tipsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                Math.max(180, (int)(ScreenUtils.windowH(this) * 0.28))));
        tipsCard.setAlignmentX(LEFT_ALIGNMENT);

        tipsCard.add(label("💡 What you can do today",
                ScreenUtils.fontSize(this, 15), true, tm.tx()));
        tipsCard.add(Box.createVerticalStrut(ScreenUtils.gap(this)));

        String[] tips = {
                "Ask \"which way does the bump go?\" when reading together",
                "Point out b and d in books — no pressure, just notice them",
                "Celebrate the practice, not just the result ✨"
        };

        for (int i = 0; i < tips.length; i++) {
            JPanel step = stepRow(String.valueOf(i + 1), tips[i]);
            step.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                    Math.max(40, (int)(ScreenUtils.windowH(this) * 0.058))));
            tipsCard.add(step);
            tipsCard.add(Box.createVerticalStrut(ScreenUtils.gap(this) / 2));
        }
        addFull(tipsCard);
        addGap(ScreenUtils.gap(this));

        // ── Settings link row ─────────────────────────────────────
        JButton setBtn = buildLinkRow(
                "⚙️", "Comfort settings",
                "Theme, font size, sound & motion");
        setBtn.addActionListener(e -> frame.openSettings());
        addFull(setBtn);

        contentArea.revalidate();
        contentArea.repaint();
    }

    // ── Mini stat cell ────────────────────────────────────────────

    private JPanel miniStatPanel(String val, String sub) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(ThemeManager.get().alt());
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
        };
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        int pad = Math.max(6, (int)(ScreenUtils.windowW(this) * 0.022));
        p.setBorder(new EmptyBorder(pad, pad, pad, pad));

        JLabel vl = label(val,
                ScreenUtils.fontSize(this, 17), true,
                ThemeManager.get().tx());
        vl.setAlignmentX(LEFT_ALIGNMENT);
        p.add(vl);

        JLabel sl = label(sub,
                ScreenUtils.fontSize(this, 10), false,
                ThemeManager.get().sub());
        sl.setAlignmentX(LEFT_ALIGNMENT);
        p.add(sl);
        return p;
    }

    // ── Mix-up trend row ──────────────────────────────────────────

    private JPanel trendRow(String letterPair, int pct) {
        ThemeManager tm = ThemeManager.get();
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(label(letterPair,
                        ScreenUtils.fontSize(this, 18), true, tm.tx()),
                BorderLayout.WEST);
        top.add(label(pct + "%",
                        ScreenUtils.fontSize(this, 11), false, tm.sub()),
                BorderLayout.EAST);
        row.add(top);
        row.add(Box.createVerticalStrut(
                Math.max(4, (int)(ScreenUtils.windowH(this) * 0.006))));

        CustomProgressBar bar = new CustomProgressBar();
        bar.setValue(pct);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                Math.max(6, (int)(ScreenUtils.windowH(this) * 0.010))));
        bar.setFillColor(pct > 50 ? tm.warn() : tm.ac());
        bar.setAlignmentX(LEFT_ALIGNMENT);
        row.add(bar);

        return row;
    }

    // ── Numbered tip row ──────────────────────────────────────────

    private JPanel stepRow(String num, String text) {
        ThemeManager tm = ThemeManager.get();
        int circleSize = Math.max(22,
                (int)(ScreenUtils.windowW(this) * 0.052));
        int rowGap = Math.max(6,
                (int)(ScreenUtils.windowW(this) * 0.018));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, rowGap, 4));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel numLbl = new JLabel(num, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.get().acl());
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        numLbl.setFont(tm.bold(ScreenUtils.fontSize(this, 11)));
        numLbl.setForeground(tm.ac());
        numLbl.setPreferredSize(new Dimension(circleSize, circleSize));
        numLbl.setOpaque(false);
        row.add(numLbl);

        JLabel txt = label(text, ScreenUtils.fontSize(this, 13),
                false, tm.tx());
        // Width fills content area minus circle and gaps
        txt.setPreferredSize(new Dimension(
                ScreenUtils.contentW(this) - circleSize - rowGap * 3,
                Math.max(20, (int)(ScreenUtils.windowH(this) * 0.028))));
        row.add(txt);
        return row;
    }

    // ── Settings link row ─────────────────────────────────────────

    private JButton buildLinkRow(String emoji, String title, String sub) {
        ThemeManager tm = ThemeManager.get();
        int rowH = Math.max(52,
                (int)(ScreenUtils.windowH(this) * 0.072));

        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                ThemeManager t = ThemeManager.get();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Card background
                g2.setColor(t.sf());
                g2.fill(new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(), 18, 18));
                g2.setColor(t.bd());
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(
                        0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 18, 18));

                int h = getHeight();
                int emojiSize = Math.max(16,
                        (int)(h * 0.40));
                int leftPad   = (int)(getWidth() * 0.038);
                int textX     = leftPad + emojiSize + (int)(getWidth() * 0.028);

                // Emoji
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, emojiSize));
                g2.setColor(t.tx());
                g2.drawString(emoji, leftPad, h / 2 + emojiSize / 3);

                // Title
                g2.setFont(t.bold(ScreenUtils.fontSize(this, 13)));
                g2.setColor(t.tx());
                g2.drawString(title, textX, h / 2 - 2);

                // Subtitle
                g2.setFont(t.regular(ScreenUtils.fontSize(this, 11)));
                g2.setColor(t.sub());
                g2.drawString(sub, textX,
                        h / 2 + ScreenUtils.fontSize(this, 13));

                // Arrow
                g2.setFont(t.bold(ScreenUtils.fontSize(this, 18)));
                g2.setColor(t.sub());
                FontMetrics afm = g2.getFontMetrics();
                g2.drawString("›",
                        getWidth() - leftPad - afm.stringWidth("›"),
                        h / 2 + afm.getAscent() / 2);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowH));
        btn.setPreferredSize(new Dimension(
                ScreenUtils.contentW(this), rowH));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Helpers ───────────────────────────────────────────────────

    private String formatDur(int sec) {
        if (sec < 60) return sec + "s";
        return (sec / 60) + "m " + (sec % 60) + "s";
    }
}