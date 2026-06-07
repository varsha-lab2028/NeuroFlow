package com.neuroflow.ui.panels;

import com.neuroflow.AppState;
import com.neuroflow.model.Student;
import com.neuroflow.service.SessionService;
import com.neuroflow.ui.MainFrame;
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

        // Star + heading
        JLabel star = new JLabel("🌟", SwingConstants.CENTER);
        star.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 72));
        star.setAlignmentX(CENTER_ALIGNMENT); 
        addFull(star); 
        addGap(10);

        JLabel done = label("Done for today!", 26, true, tm.tx());
        done.setAlignmentX(CENTER_ALIGNMENT); 
        addFull(done); 
        addGap(6);

        JLabel sub = label("You practised — keep it up!", 15, false, tm.sub());
        sub.setAlignmentX(CENTER_ALIGNMENT); 
        addFull(sub); 
        addGap(22);

        // Stat grid
        JPanel grid = new JPanel(new GridLayout(2, 2, 10, 10));
        grid.setOpaque(false); 
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        
        timeStat    = buildStatCard("⏱", "--",  "Time practised");
        triesStat   = buildStatCard("✏️", "--", "Attempts made");
        lettersStat = buildStatCard("🎯", "--", "This week");
        streakStat  = buildStatCard("🔥", "--", "Streak");
        
        grid.add(timeStat.getParent()); 
        grid.add(triesStat.getParent());
        grid.add(lettersStat.getParent()); 
        grid.add(streakStat.getParent());
        
        addFull(grid); 
        addGap(22);

        // Buttons
        nextLetterHint = label("", 13, false, tm.sub());
        nextLetterHint.setAlignmentX(CENTER_ALIGNMENT); 
        addFull(nextLetterHint); 
        addGap(10);

        RoundedButton nextBtn = new RoundedButton("One more? →", RoundedButton.Style.PRIMARY);
        nextBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        nextBtn.addActionListener(e -> {
            String next = nextLetter(AppState.get().getSelectedLetter());
            AppState.get().setSelectedLetter(next);
            frame.showPanel(MainFrame.WATCH);
        });
        addFull(nextBtn); 
        addGap(10);

        RoundedButton homeBtn = new RoundedButton("Back to home", RoundedButton.Style.GHOST);
        homeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        homeBtn.addActionListener(e -> frame.showPanel(MainFrame.HOME));
        addFull(homeBtn); 
        addGap(14);

        JLabel auto = label("Your parent can see your progress automatically", 11, false, tm.sub());
        auto.setAlignmentX(CENTER_ALIGNMENT); 
        addFull(auto);
    }

    private JLabel buildStatCard(String emoji, String value, String sublabel) {
        RoundedPanel card = new RoundedPanel(16);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(12, 8, 12, 8));
        card.setMaximumSize(new Dimension(180, 80));

        JLabel emojiL = new JLabel(emoji, SwingConstants.CENTER);
        emojiL.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        emojiL.setAlignmentX(CENTER_ALIGNMENT);

        JLabel valL = new JLabel(value, SwingConstants.CENTER);
        valL.setFont(ThemeManager.get().bold(22)); 
        valL.setForeground(ThemeManager.get().tx());
        valL.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel(sublabel, SwingConstants.CENTER);
        sub.setFont(ThemeManager.get().regular(10)); 
        sub.setForeground(ThemeManager.get().sub());
        sub.setAlignmentX(CENTER_ALIGNMENT);

        card.add(emojiL); 
        card.add(valL); 
        card.add(sub);
        return valL;
    }

    @Override
    public void refresh() {
        Student s = AppState.get().getCurrentStudent();
        if (s == null) { timeStat.setText("--"); triesStat.setText("--"); return; }
        
        int dur = SessionService.get().sumDurationToday(s.getId());
        int att = SessionService.get().countTodayAttempts(s.getId());
        long lettersThisWeek = SessionService.get().getTodaySessions(s.getId()).stream()
                .map(ps -> ps.getTargetLetter()).distinct().count();
                
        timeStat.setText(formatDur(dur));
        triesStat.setText(att + " tries");
        lettersStat.setText(lettersThisWeek + " letters");
        streakStat.setText(s.getStreakDays() + " days");

        String next = nextLetter(AppState.get().getSelectedLetter());
        nextLetterHint.setText("Next up: letter \"" + next + "\"");
        
        contentArea.revalidate(); 
        contentArea.repaint();
    }

    private String formatDur(int sec) {
        if (sec < 60) return sec + "s"; 
        return (sec / 60) + "m " + (sec % 60) + "s";
    }

    private String nextLetter(String current) {
        return switch (current == null ? "b" : current) {
            case "b" -> "d"; case "d" -> "p"; case "p" -> "q"; default -> "b";
        };
    }
}
