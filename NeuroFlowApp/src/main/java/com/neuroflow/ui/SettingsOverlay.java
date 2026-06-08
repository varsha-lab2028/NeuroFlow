package com.neuroflow.ui;

import com.neuroflow.ui.components.ToggleSwitch;
import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class SettingsOverlay extends JPanel implements ThemeManager.ThemeListener {
    private final MainFrame frame;
    private JPanel slidePanel;

    public SettingsOverlay(MainFrame frame) {
        this.frame = frame;
        setOpaque(false);
        setLayout(null);
        setVisible(false);
        ThemeManager.get().addListener(this);
        buildUI();

        // Click on dim background to close
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!slidePanel.getBounds().contains(e.getPoint())) {
                    frame.closeSettings();
                }
            }
        });
    }

    private void buildUI() {
        slidePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.get().sf());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 28, 28));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        slidePanel.setOpaque(false);
        slidePanel.setLayout(new BoxLayout(slidePanel, BoxLayout.Y_AXIS));
        slidePanel.setBorder(new EmptyBorder(20, 24, 28, 24));
        add(slidePanel);
        rebuildContent();
    }

    private void rebuildContent() {
        slidePanel.removeAll();
        ThemeManager tm = ThemeManager.get();

        // ── Header ───────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel title = new JLabel("Comfort Settings");
        title.setFont(tm.bold(18));
        title.setForeground(tm.tx());
        header.add(title, BorderLayout.WEST);

        JButton closeBtn = new JButton("×");
        closeBtn.setFont(tm.bold(22));
        closeBtn.setForeground(tm.sub());
        closeBtn.setOpaque(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> frame.closeSettings());
        header.add(closeBtn, BorderLayout.EAST);

        slidePanel.add(header);
        slidePanel.add(Box.createVerticalStrut(4));

        JLabel sub = new JLabel("Adjust so the app feels just right for this child");
        sub.setFont(tm.regular(13));
        sub.setForeground(tm.sub());
        sub.setAlignmentX(LEFT_ALIGNMENT);
        slidePanel.add(sub);
        slidePanel.add(Box.createVerticalStrut(20));

        // ── Reading Mode ─────────────────────────────────────────
        slidePanel.add(sectionLabel("READING MODE"));
        slidePanel.add(Box.createVerticalStrut(8));

        JLabel modeNote = new JLabel("Choose a preset — colours, spacing & font adjust automatically");
        modeNote.setFont(tm.regular(12));
        modeNote.setForeground(tm.sub());
        modeNote.setAlignmentX(LEFT_ALIGNMENT);
        slidePanel.add(modeNote);
        slidePanel.add(Box.createVerticalStrut(10));

        // Reading mode cards in a horizontal scroll row
        JPanel modeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        modeRow.setOpaque(false);
        modeRow.setAlignmentX(LEFT_ALIGNMENT);

        String[][] modes = {
                {"🌟", "Default",          "Standard settings"},
                {"📖", "Dyslexia",         "Wider spacing · warm background"},
                {"🎯", "ADHD Focus",       "Calm colours · minimal layout"},
                {"🌙", "Sensory-Sensitive","Dark · no motion · gentle haptic"},
                {"🔎", "Low Vision",       "Large text · high contrast"},
        };

        ThemeManager.ReadingMode current = tm.getReadingMode();
        ThemeManager.ReadingMode[] modeValues = ThemeManager.ReadingMode.values();

        for (int i = 0; i < modes.length; i++) {
            final ThemeManager.ReadingMode mode = modeValues[i];
            boolean selected = current == mode;
            modeRow.add(buildModeCard(modes[i][0], modes[i][1], modes[i][2], selected, mode));
        }
        slidePanel.add(modeRow);
        slidePanel.add(Box.createVerticalStrut(20));

        // ── Colour Theme ─────────────────────────────────────────
        slidePanel.add(sectionLabel("COLOUR THEME"));
        slidePanel.add(Box.createVerticalStrut(10));

        JPanel themeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        themeRow.setOpaque(false);
        themeRow.setAlignmentX(LEFT_ALIGNMENT);

        String[][] themes = {
                {"Soft Cream",  "0xF6F1E8", "0x4E8FC5"},
                {"Blue Mist",   "0xEEF4FA", "0x5B8FB9"},
                {"Warm Paper",  "0xF7EFE3", "0xC98F6B"},
                {"Night",       "0x2A2A2A", "0x7BA8F0"},
        };
        ThemeManager.Preset[] presets = ThemeManager.Preset.values();

        for (int i = 0; i < themes.length; i++) {
            final ThemeManager.Preset preset = presets[i];
            boolean sel = tm.getPreset() == preset;
            Color bg  = new Color(Integer.decode(themes[i][1]));
            Color dot = new Color(Integer.decode(themes[i][2]));
            themeRow.add(buildThemeChip(themes[i][0], bg, dot, sel, preset));
        }
        slidePanel.add(themeRow);
        slidePanel.add(Box.createVerticalStrut(20));

        // ── Text Size ────────────────────────────────────────────
        slidePanel.add(sectionLabel("TEXT SIZE"));
        slidePanel.add(Box.createVerticalStrut(10));

        JPanel sizeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        sizeRow.setOpaque(false);
        sizeRow.setAlignmentX(LEFT_ALIGNMENT);

        String[] sizeLabels = {"A small", "A medium", "A large"};
        float[]  sizeScales = {0.85f, 1.0f, 1.25f};
        int[]    sizePts    = {12, 14, 18};

        for (int i = 0; i < sizeLabels.length; i++) {
            final float scale = sizeScales[i];
            boolean sel = Math.abs(tm.getFontScale() - scale) < 0.05f;
            sizeRow.add(buildSizeBtn(sizeLabels[i], sizePts[i], sel, scale));
        }
        slidePanel.add(sizeRow);
        slidePanel.add(Box.createVerticalStrut(20));

        // ── Accessibility toggles ────────────────────────────────
        slidePanel.add(sectionLabel("ACCESSIBILITY"));
        slidePanel.add(Box.createVerticalStrut(10));

        slidePanel.add(buildToggleRow("Motion animations", tm.isMotionEnabled(), v -> {
            tm.setMotionEnabled(v);
        }));
        slidePanel.add(Box.createVerticalStrut(8));
        slidePanel.add(buildToggleRow("Sound effects", tm.isSoundEnabled(), v -> {
            tm.setSoundEnabled(v);
        }));
        slidePanel.add(Box.createVerticalStrut(8));

        // Haptic intensity
        JPanel hapticRow = new JPanel(new BorderLayout(10, 0));
        hapticRow.setOpaque(false);
        hapticRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel hapticLbl = new JLabel("Haptic intensity");
        hapticLbl.setFont(tm.regular(14));
        hapticLbl.setForeground(tm.tx());
        hapticRow.add(hapticLbl, BorderLayout.WEST);

        JPanel hapticBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        hapticBtns.setOpaque(false);

        boolean isLow = tm.getHapticLevel() == ThemeManager.HapticLevel.LOW;
        JButton lowBtn = buildHapticBtn("Low",    isLow);
        JButton medBtn = buildHapticBtn("Medium", !isLow);

        lowBtn.addActionListener(e -> {
            tm.setHapticLevel(ThemeManager.HapticLevel.LOW);
            rebuildContent();
            revalidate();
            repaint();
        });
        medBtn.addActionListener(e -> {
            tm.setHapticLevel(ThemeManager.HapticLevel.MEDIUM);
            rebuildContent();
            revalidate();
            repaint();
        });

        hapticBtns.add(lowBtn);
        hapticBtns.add(medBtn);
        hapticRow.add(hapticBtns, BorderLayout.EAST);
        hapticRow.setAlignmentX(LEFT_ALIGNMENT);
        slidePanel.add(hapticRow);
        slidePanel.add(Box.createVerticalStrut(20));

        // ── Confetti toggle ──────────────────────────────────────
        slidePanel.add(sectionLabel("CONFETTI ON WIN"));
        slidePanel.add(Box.createVerticalStrut(10));
        slidePanel.add(buildToggleRow("Show celebration animation", true, v -> {}));

        slidePanel.revalidate();
        slidePanel.repaint();
    }

    // ── Component builders ────────────────────────────────────────

    private JLabel sectionLabel(String text) {
        ThemeManager tm = ThemeManager.get();
        JLabel l = new JLabel(text);
        l.setFont(tm.bold(11));
        l.setForeground(tm.sub());
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JPanel buildModeCard(String emoji, String name, String tag,
                                 boolean selected, ThemeManager.ReadingMode mode) {
        ThemeManager tm = ThemeManager.get();
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(selected ? tm.acl() : tm.alt());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                if (selected) {
                    g2.setColor(tm.ac());
                    g2.setStroke(new BasicStroke(2f));
                    g2.draw(new RoundRectangle2D.Float(1, 1,
                            getWidth() - 2, getHeight() - 2, 14, 14));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(10, 12, 10, 12));
        card.setPreferredSize(new Dimension(110, 80));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel emojiL = new JLabel(emoji, SwingConstants.CENTER);
        emojiL.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        emojiL.setAlignmentX(CENTER_ALIGNMENT);

        JLabel nameL = new JLabel(name, SwingConstants.CENTER);
        nameL.setFont(tm.bold(11));
        nameL.setForeground(tm.tx());
        nameL.setAlignmentX(CENTER_ALIGNMENT);

        JLabel tagL = new JLabel("<html><center>" + tag + "</center></html>",
                SwingConstants.CENTER);
        tagL.setFont(tm.regular(9));
        tagL.setForeground(tm.sub());
        tagL.setAlignmentX(CENTER_ALIGNMENT);

        card.add(emojiL);
        card.add(Box.createVerticalStrut(2));
        card.add(nameL);
        card.add(tagL);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ThemeManager.get().setReadingMode(mode);
                rebuildContent();
                revalidate();
                repaint();
            }
        });
        return card;
    }

    private JPanel buildThemeChip(String name, Color bg, Color dot,
                                  boolean selected, ThemeManager.Preset preset) {
        ThemeManager tm = ThemeManager.get();
        JPanel chip = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                if (selected) {
                    g2.setColor(dot);
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.draw(new RoundRectangle2D.Float(1, 1,
                            getWidth() - 2, getHeight() - 2, 12, 12));
                }
                // Dot
                g2.setColor(dot);
                g2.fillOval(8, (getHeight() - 10) / 2, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setOpaque(false);
        chip.setLayout(new FlowLayout(FlowLayout.LEFT, 22, 0));
        chip.setPreferredSize(new Dimension(90, 34));
        chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel(name);
        lbl.setFont(tm.regular(11));
        lbl.setForeground(bg.getRed() < 80 ? Color.WHITE : new Color(0x1E1E1E));
        chip.add(lbl);

        chip.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ThemeManager.get().setPreset(preset);
                rebuildContent();
                revalidate();
                repaint();
            }
        });
        return chip;
    }

    private JButton buildSizeBtn(String text, int fontSize, boolean selected, float scale) {
        ThemeManager tm = ThemeManager.get();
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(selected ? tm.ac() : tm.alt());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setFont(getFont());
                g2.setColor(selected ? Color.WHITE : tm.tx());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, fontSize));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(86, 36));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            tm.setFontScale(scale);
            rebuildContent();
            revalidate();
            repaint();
        });
        return btn;
    }

    private JPanel buildToggleRow(String text, boolean initialOn,
                                  java.util.function.Consumer<Boolean> onChange) {
        ThemeManager tm = ThemeManager.get();
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(text);
        lbl.setFont(tm.regular(14));
        lbl.setForeground(tm.tx());
        row.add(lbl, BorderLayout.WEST);

        ToggleSwitch toggle = new ToggleSwitch(initialOn);
        toggle.setOnChange(() -> onChange.accept(toggle.isOn()));
        row.add(toggle, BorderLayout.EAST);
        return row;
    }

    private JButton buildHapticBtn(String text, boolean active) {
        ThemeManager tm = ThemeManager.get();
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(active ? tm.ac() : tm.alt());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setFont(getFont());
                g2.setColor(active ? Color.WHITE : tm.sub());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        btn.setFont(tm.bold(12));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(76, 30));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Layout + painting ─────────────────────────────────────────

    @Override
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        if (slidePanel != null) {
            int panelH = Math.min(h - 60, 580);
            slidePanel.setBounds(0, h - panelH, w, panelH);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Dim background
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public void onThemeChanged() {
        rebuildContent();
        revalidate();
        repaint();
    }
}
