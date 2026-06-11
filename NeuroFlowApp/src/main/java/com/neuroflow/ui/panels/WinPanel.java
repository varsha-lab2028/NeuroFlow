package com.neuroflow.ui.panels;

import com.neuroflow.AppState;
import com.neuroflow.model.Student;
import com.neuroflow.service.PracticeSessionService;
import com.neuroflow.ui.MainFrame;
import com.neuroflow.ui.ScreenUtils;
import com.neuroflow.ui.components.RoundedButton;
import com.neuroflow.ui.components.RoundedPanel;
import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class WinPanel extends BasePanel {
    private JLabel timeStat, triesStat, lettersStat, streakStat;
    private JLabel nextLetterHint;

    public WinPanel(MainFrame frame) {
        super(frame, "Great work!", false, false, null);
        buildUI();
    }

    private void buildUI() {
        ThemeManager tm = ThemeManager.get();

        // ── Star emoji ────────────────────────────────────────────
        JLabel star = new JLabel("🌟", SwingConstants.CENTER);
        star.setFont(new Font("Segoe UI Emoji", Font.PLAIN,
                Math.max(56, (int)(ScreenUtils.windowH(this) * 0.082))));
        star.setAlignmentX(CENTER_ALIGNMENT);
        addFull(star);
        addGap(ScreenUtils.gap(this));

        // ── Heading ───────────────────────────────────────────────
        JLabel done = label("Done for today!",
                ScreenUtils.fontSize(this, 26), true, tm.tx());
        done.setAlignmentX(CENTER_ALIGNMENT);
        addFull(done);
        addGap(ScreenUtils.gap(this) / 3);

        JLabel sub = label("You practised — keep it up!",
                ScreenUtils.fontSize(this, 15), false, tm.sub());
        sub.setAlignmentX(CENTER_ALIGNMENT);
        addFull(sub);
        addGap(ScreenUtils.gap(this) * 2);

        // ── Stat grid ─────────────────────────────────────────────
        JPanel grid = new JPanel(new GridLayout(2, 2,
                Math.max(8, (int)(ScreenUtils.windowW(this) * 0.022)),
                Math.max(8, (int)(ScreenUtils.windowH(this) * 0.014))));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                Math.max(140, (int)(ScreenUtils.windowH(this) * 0.22))));
        grid.setAlignmentX(LEFT_ALIGNMENT);

        timeStat    = buildStatCard("⏱",  "--", "Time practised");
        triesStat   = buildStatCard("✏️", "--", "Attempts made");
        lettersStat = buildStatCard("🎯", "--", "This week");
        streakStat  = buildStatCard("🔥", "--", "Streak");

        grid.add(timeStat.getParent());
        grid.add(triesStat.getParent());
        grid.add(lettersStat.getParent());
        grid.add(streakStat.getParent());

        addFull(grid);
        addGap(ScreenUtils.gap(this) * 2);

        // ── Next letter hint ──────────────────────────────────────
        nextLetterHint = label("",
                ScreenUtils.fontSize(this, 13), false, tm.sub());
        nextLetterHint.setAlignmentX(CENTER_ALIGNMENT);
        addFull(nextLetterHint);
        addGap(ScreenUtils.gap(this));

        // ── Buttons ───────────────────────────────────────────────
        int btnH = ScreenUtils.buttonH(this);

        RoundedButton nextBtn = new RoundedButton(
                "One more? →", RoundedButton.Style.PRIMARY);
        nextBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnH));
        nextBtn.addActionListener(e -> {
            String next = nextLetter(AppState.get().getSelectedLetter());
            AppState.get().setSelectedLetter(next);
            frame.showPanel(MainFrame.WATCH);
        });
        addFull(nextBtn);
        addGap(ScreenUtils.gap(this) / 2);

        RoundedButton homeBtn = new RoundedButton(
                "Back to home", RoundedButton.Style.GHOST);
        homeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnH));
        homeBtn.addActionListener(e -> frame.showPanel(MainFrame.HOME));
        addFull(homeBtn);
        addGap(ScreenUtils.gap(this));

        JLabel auto = label(
                "Your parent can see your progress automatically",
                ScreenUtils.fontSize(this, 11), false, tm.sub());
        auto.setAlignmentX(CENTER_ALIGNMENT);
        addFull(auto);
    }

    // ── Stat card builder ─────────────────────────────────────────

    private JLabel buildStatCard(String emoji, String value, String sublabel) {
        ThemeManager tm = ThemeManager.get();

        int cp = ScreenUtils.cardPad(this);
        int cardGap = Math.max(6, (int)(ScreenUtils.windowW(this) * 0.016));

        RoundedPanel card = new RoundedPanel(16);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(cp, cardGap, cp, cardGap));

        // Emoji
        JLabel emojiL = new JLabel(emoji, SwingConstants.CENTER);
        emojiL.setFont(new Font("Segoe UI Emoji", Font.PLAIN,
                Math.max(18, (int)(ScreenUtils.windowH(this) * 0.028))));
        emojiL.setAlignmentX(CENTER_ALIGNMENT);

        // Value
        JLabel valL = new JLabel(value, SwingConstants.CENTER);
        valL.setFont(tm.bold(Math.max(18,
                (int)(ScreenUtils.windowH(this) * 0.030))));
        valL.setForeground(tm.tx());
        valL.setAlignmentX(CENTER_ALIGNMENT);

        // Sub label
        JLabel subL = new JLabel(sublabel, SwingConstants.CENTER);
        subL.setFont(tm.regular(ScreenUtils.fontSize(this, 10)));
        subL.setForeground(tm.sub());
        subL.setAlignmentX(CENTER_ALIGNMENT);

        card.add(emojiL);
        card.add(Box.createVerticalStrut(
                Math.max(2, (int)(ScreenUtils.windowH(this) * 0.005))));
        card.add(valL);
        card.add(Box.createVerticalStrut(
                Math.max(2, (int)(ScreenUtils.windowH(this) * 0.004))));
        card.add(subL);
        return valL;
    }

    // ── Refresh ───────────────────────────────────────────────────

    @Override
    public void refresh() {
        Student s = AppState.get().getCurrentStudent();
        if (s == null) {
            timeStat.setText("--");
            triesStat.setText("--");
            lettersStat.setText("--");
            streakStat.setText("--");
            return;
        }

        int dur = PracticeSessionService.get().sumDurationToday(s.getStudentId());
        int att = PracticeSessionService.get().countTodayAttempts(s.getStudentId());
        long lettersThisWeek = PracticeSessionService.get()
                .getTodaySessions(s.getStudentId()).stream()
                .map(ps -> ps.getTargetLetter())
                .distinct()
                .count();

        timeStat.setText(formatDur(dur));
        triesStat.setText(att + " tries");
        lettersStat.setText(lettersThisWeek + " letters");
        streakStat.setText(s.getStreakDays() + " days");

        // Refresh font sizes on window resize
        ThemeManager tm = ThemeManager.get();
        timeStat.setFont(tm.bold(Math.max(18,
                (int)(ScreenUtils.windowH(this) * 0.030))));
        triesStat.setFont(timeStat.getFont());
        lettersStat.setFont(timeStat.getFont());
        streakStat.setFont(timeStat.getFont());

        String next = nextLetter(AppState.get().getSelectedLetter());
        nextLetterHint.setText("Next up: letter \"" + next + "\"");
        nextLetterHint.setFont(tm.regular(
                ScreenUtils.fontSize(this, 13)));

        contentArea.revalidate();
        contentArea.repaint();
    }

    // ── Helpers ───────────────────────────────────────────────────

    private String formatDur(int sec) {
        if (sec < 60) return sec + "s";
        return (sec / 60) + "m " + (sec % 60) + "s";
    }

    private String nextLetter(String current) {
        return switch (current == null ? "b" : current) {
            case "b" -> "d";
            case "d" -> "p";
            case "p" -> "q";
            default  -> "b";
        };
    }
}