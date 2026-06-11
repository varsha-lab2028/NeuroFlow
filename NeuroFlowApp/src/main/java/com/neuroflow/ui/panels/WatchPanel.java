package com.neuroflow.ui.panels;

import com.neuroflow.AppState;
import com.neuroflow.ui.MainFrame;
import com.neuroflow.ui.ScreenUtils;
import com.neuroflow.ui.components.AnimatedLetterPanel;
import com.neuroflow.ui.components.RoundedButton;
import com.neuroflow.ui.components.RoundedPanel;
import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class WatchPanel extends BasePanel {
    private AnimatedLetterPanel animPanel;
    private JLabel statusLabel;
    private JLabel[] stepDots;
    private RoundedButton playBtn, nextBtn, replayBtn;
    private JLabel letterTitle;

    private static final String[][] STEPS = {
            {"b", "Start at the top — place your pencil high up",
                    "Draw a straight line all the way down",
                    "Add a round bump to the RIGHT side"},
            {"d", "Start on the right — your pencil starts high on the right",
                    "Draw the bowl first — curve round to the left",
                    "Add a straight line going DOWN on the RIGHT"},
            {"p", "Start mid-height — place your pencil in the middle",
                    "Draw a straight line going DOWN past the line",
                    "Add a round bump to the RIGHT above the line"},
            {"q", "Start mid-height on the right side",
                    "Draw the bowl — curve round to the LEFT",
                    "Add a straight line going DOWN on the LEFT side"},
    };

    public WatchPanel(MainFrame frame) {
        super(frame, "Watch & Learn", false, true, MainFrame.HOME);
        buildUI();
    }

    private void buildUI() {
        ThemeManager tm = ThemeManager.get();

        addFull(label("Watch how to write the letter",
                ScreenUtils.fontSize(this, 13), false, tm.sub()));
        addGap(ScreenUtils.gap(this));

        // ── Animation card ────────────────────────────────────────
        RoundedPanel animCard = new RoundedPanel(18);
        animCard.setLayout(new BoxLayout(animCard, BoxLayout.Y_AXIS));
        int cp = ScreenUtils.cardPad(this);
        animCard.setBorder(new EmptyBorder(cp, cp, cp, cp));
        animCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                Math.max(240, (int)(ScreenUtils.windowH(this) * 0.34))));
        animCard.setAlignmentX(LEFT_ALIGNMENT);

        // AnimatedLetterPanel scales its own preferred size
        animPanel = new AnimatedLetterPanel();
        animPanel.setAlignmentX(CENTER_ALIGNMENT);
        animPanel.setOnComplete(() -> {
            playBtn.setVisible(false);
            nextBtn.setVisible(true);
            replayBtn.setVisible(true);
            stepDots[2].setForeground(tm.ac());
        });
        animCard.add(animPanel);
        animCard.add(Box.createVerticalStrut(ScreenUtils.gap(this)));

        // Step progress dots
        JPanel dotsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,
                Math.max(4, (int)(ScreenUtils.windowW(this) * 0.014)), 4));
        dotsPanel.setOpaque(false);
        stepDots = new JLabel[3];
        for (int i = 0; i < 3; i++) {
            stepDots[i] = new JLabel("●");
            stepDots[i].setFont(new Font("Segoe UI", Font.PLAIN,
                    ScreenUtils.fontSize(this, 12)));
            stepDots[i].setForeground(tm.alt());
            dotsPanel.add(stepDots[i]);
        }
        animCard.add(dotsPanel);
        animCard.add(Box.createVerticalStrut(ScreenUtils.gap(this) / 2));

        statusLabel = new JLabel("Press play to see how", SwingConstants.CENTER);
        statusLabel.setFont(tm.bold(ScreenUtils.fontSize(this, 12)));
        statusLabel.setForeground(tm.sub());
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);
        animCard.add(statusLabel);

        addFull(animCard);
        addGap(ScreenUtils.gap(this));

        // ── Step instructions card ────────────────────────────────
        RoundedPanel stepsCard = new RoundedPanel(18);
        stepsCard.setLayout(new BoxLayout(stepsCard, BoxLayout.Y_AXIS));
        stepsCard.setBorder(new EmptyBorder(cp, cp + 2, cp, cp + 2));
        stepsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                Math.max(140, (int)(ScreenUtils.windowH(this) * 0.22))));
        stepsCard.setAlignmentX(LEFT_ALIGNMENT);
        stepsCard.setName("stepsCard");

        letterTitle = label("How to write \"b\"",
                ScreenUtils.fontSize(this, 14), true, tm.tx());
        letterTitle.setAlignmentX(LEFT_ALIGNMENT);
        stepsCard.add(letterTitle);
        stepsCard.add(Box.createVerticalStrut(ScreenUtils.gap(this)));

        // Step rows — populated in refresh()
        stepsCard.setName("stepsCard");

        addFull(stepsCard);
        addGap(ScreenUtils.gap(this));

        // ── Buttons ───────────────────────────────────────────────
        playBtn = new RoundedButton("▶  Watch the animation",
                RoundedButton.Style.PRIMARY);
        playBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                ScreenUtils.buttonH(this)));
        playBtn.addActionListener(e -> {
            animPanel.play();
            statusLabel.setText("Drawing stroke 1...");
            statusLabel.setForeground(tm.ac());
            stepDots[0].setForeground(tm.ac());
        });

        nextBtn = new RoundedButton("✏️  Now I'll try it!",
                RoundedButton.Style.SUCCESS);
        nextBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                ScreenUtils.buttonH(this)));
        nextBtn.setVisible(false);
        nextBtn.addActionListener(e -> frame.showPanel(MainFrame.TRY));

        replayBtn = new RoundedButton("↩  Watch again",
                RoundedButton.Style.GHOST);
        replayBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                ScreenUtils.buttonH(this)));
        replayBtn.setVisible(false);
        replayBtn.addActionListener(e -> {
            animPanel.reset();
            for (JLabel d : stepDots) d.setForeground(tm.alt());
            statusLabel.setText("Press play to see how");
            statusLabel.setForeground(tm.sub());
            nextBtn.setVisible(false);
            replayBtn.setVisible(false);
            playBtn.setVisible(true);
        });

        addFull(playBtn);
        addFull(nextBtn);
        addFull(replayBtn);
        addGap(ScreenUtils.gap(this) / 2);

        // ── Sound button ──────────────────────────────────────────
        JButton soundBtn = new JButton("🔊 Hear the letter sound");
        soundBtn.setFont(tm.bold(ScreenUtils.fontSize(this, 13)));
        soundBtn.setForeground(tm.ac());
        soundBtn.setOpaque(false);
        soundBtn.setContentAreaFilled(false);
        soundBtn.setBorderPainted(false);
        soundBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        soundBtn.setAlignmentX(CENTER_ALIGNMENT);
        soundBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "🔊 Playing phoneme for \""
                        + AppState.get().getSelectedLetter()
                        + "\"\n(Sound playback requires device audio)",
                "Letter Sound", JOptionPane.INFORMATION_MESSAGE));

        addFull(soundBtn);
    }

    @Override
    public void refresh() {
        String letter = AppState.get().getSelectedLetter();
        if (letter == null) letter = "b";

        animPanel.reset();
        animPanel.setLetter(letter);

        ThemeManager tm = ThemeManager.get();
        for (JLabel d : stepDots) d.setForeground(tm.alt());

        statusLabel.setText("Press play to see how");
        statusLabel.setForeground(tm.sub());
        statusLabel.setFont(tm.bold(ScreenUtils.fontSize(this, 12)));

        playBtn.setVisible(true);
        nextBtn.setVisible(false);
        replayBtn.setVisible(false);

        letterTitle.setText("How to write \"" + letter + "\"");
        letterTitle.setFont(tm.bold(ScreenUtils.fontSize(this, 14)));

        // Rebuild step rows inside stepsCard on every refresh
        // so font sizes update on window resize
        rebuildStepRows(letter);

        contentArea.revalidate();
        contentArea.repaint();
    }

    // ── Step rows ─────────────────────────────────────────────────

    private void rebuildStepRows(String letter) {
        // Find the stepsCard by name and rebuild its step content
        for (Component c : contentArea.getComponents()) {
            if (c instanceof RoundedPanel rp && "stepsCard".equals(rp.getName())) {
                // Remove everything after the title and gap
                Component[] children = rp.getComponents();
                // Keep only index 0 (letterTitle) and index 1 (gap)
                for (int i = children.length - 1; i > 1; i--) {
                    rp.remove(children[i]);
                }
                // Add step rows
                String[][] steps = getSteps(letter);
                for (int i = 0; i < steps.length; i++) {
                    rp.add(buildStepRow(i + 1, steps[i][0]));
                    rp.add(Box.createVerticalStrut(
                            Math.max(6, ScreenUtils.gap(this) / 2)));
                }
                rp.revalidate();
                rp.repaint();
                break;
            }
        }
    }

    private JPanel buildStepRow(int num, String text) {
        ThemeManager tm = ThemeManager.get();
        int stepSize = Math.max(22, (int)(ScreenUtils.windowW(this) * 0.052));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT,
                Math.max(8, (int)(ScreenUtils.windowW(this) * 0.018)), 2));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);

        // Numbered circle
        JLabel numLbl = new JLabel(String.valueOf(num), SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tm.acl());
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        numLbl.setFont(tm.bold(ScreenUtils.fontSize(this, 11)));
        numLbl.setForeground(tm.ac());
        numLbl.setPreferredSize(new Dimension(stepSize, stepSize));
        numLbl.setOpaque(false);
        row.add(numLbl);

        JLabel txt = label(text, ScreenUtils.fontSize(this, 13), false, tm.tx());
        row.add(txt);

        return row;
    }

    private String[][] getSteps(String letter) {
        for (String[] s : STEPS)
            if (s[0].equals(letter))
                return new String[][]{{s[1]}, {s[2]}, {s[3]}};
        return new String[][]{{"Step 1"}, {"Step 2"}, {"Step 3"}};
    }
}