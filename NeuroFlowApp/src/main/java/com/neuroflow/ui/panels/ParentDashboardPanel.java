package com.neuroflow.ui.panels;

import com.neuroflow.AppState;
import com.neuroflow.model.Student;
import com.neuroflow.service.SessionService;
import com.neuroflow.ui.MainFrame;
import com.neuroflow.ui.components.*;
import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
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

        // Greeting
        addFull(label("Hi there 👋", 20, true, tm.tx())); 
        addGap(4);
        addFull(label("Here's how practice went today", 13, false, tm.sub())); 
        addGap(14);

        // Today's practice card
        if (s != null) {
            int dur = SessionService.get().sumDurationToday(s.getId());
            int att = SessionService.get().countTodayAttempts(s.getId());
            boolean hasPracticed = dur > 0 || att > 0;

            RoundedPanel todayCard = new RoundedPanel(18);
            todayCard.setLayout(new BoxLayout(todayCard, BoxLayout.Y_AXIS));
            todayCard.setBorder(new EmptyBorder(14, 16, 14, 16));
            todayCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

            JPanel topRow = new JPanel(new BorderLayout());
            topRow.setOpaque(false);
            topRow.add(label("📅 Today's practice", 15, true, tm.tx()), BorderLayout.WEST);
            BadgeLabel badge = new BadgeLabel(hasPracticed ? "Completed" : "No practice yet",
                    hasPracticed ? BadgeLabel.BadgeType.OK : BadgeLabel.BadgeType.WARN);
            topRow.add(badge, BorderLayout.EAST);
            
            todayCard.add(topRow); 
            todayCard.add(Box.createVerticalStrut(10));

            JPanel statsGrid = new JPanel(new GridLayout(2, 2, 8, 8));
            statsGrid.setOpaque(false);
            statsGrid.add(miniStat(formatDur(dur), "Time spent"));
            statsGrid.add(miniStat(att + " tries", "Attempts"));
            
            JPanel lettersCell = miniStatPanel(s.getCurrentLetter().toUpperCase() + " letters", "Practised today");
            lettersCell.setPreferredSize(new Dimension(200, 44));
            statsGrid.add(lettersCell); 
            statsGrid.add(miniStatPanel("", ""));
            
            todayCard.add(statsGrid);
            addFull(todayCard); 
            addGap(12);
        }

        // Common mix-ups card
        RoundedPanel mixCard = new RoundedPanel(18);
        mixCard.setLayout(new BoxLayout(mixCard, BoxLayout.Y_AXIS));
        mixCard.setBorder(new EmptyBorder(14, 16, 14, 16));
        mixCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        
        mixCard.add(label("🔄 Common mix-ups", 15, true, tm.tx())); 
        mixCard.add(Box.createVerticalStrut(4));
        mixCard.add(label("Totally normal — these letters look very similar!", 12, false, tm.sub()));
        mixCard.add(Box.createVerticalStrut(12));

        Map<String, Integer> mixups = new LinkedHashMap<>();
        mixups.put("b / d", 60); 
        mixups.put("p / q", 30);
        
        if (s != null) {
            Map<String, Integer> real = SessionService.get().weeklyErrorTotals();
            if (!real.isEmpty()) mixups.clear();
            real.forEach((k, v) -> { if (k.contains("reversal")) mixups.put(k.replace(" reversal", ""), v * 3); });
        }

        for (Map.Entry<String, Integer> e : mixups.entrySet()) {
            mixCard.add(trendRow(e.getKey(), e.getValue()));
            mixCard.add(Box.createVerticalStrut(8));
        }
        addFull(mixCard); 
        addGap(12);

        // Tips card
        RoundedPanel tipsCard = new RoundedPanel(18);
        tipsCard.setLayout(new BoxLayout(tipsCard, BoxLayout.Y_AXIS));
        tipsCard.setBorder(new EmptyBorder(14, 16, 14, 16));
        tipsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        
        tipsCard.add(label("💡 What you can do today", 15, true, tm.tx())); 
        tipsCard.add(Box.createVerticalStrut(10));

        String[] tips = {
            "Ask \"which way does the bump go?\" when reading together",
            "Point out b and d in books — no pressure, just notice them",
            "Celebrate the practice, not just the result ✨"
        };

        for (int i = 0; i < tips.length; i++) {
            JPanel step = stepRow(String.valueOf(i + 1), tips[i]);
            step.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            tipsCard.add(step); 
            tipsCard.add(Box.createVerticalStrut(6));
        }
        addFull(tipsCard); 
        addGap(12);

        // Settings link
        JButton setBtn = buildLinkRow("⚙️", "Comfort settings", "Theme, font size, sound & motion");
        setBtn.addActionListener(e -> frame.openSettings());
        addFull(setBtn);

        contentArea.revalidate(); 
        contentArea.repaint();
    }

    private JPanel miniStat(String val, String sub) { return miniStatPanel(val, sub); }

    private JPanel miniStatPanel(String val, String sub) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(ThemeManager.get().alt()); 
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
        };
        p.setOpaque(false); 
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(8, 10, 8, 10));
        
        JLabel vl = label(val, 17, true, ThemeManager.get().tx()); 
        vl.setAlignmentX(LEFT_ALIGNMENT); 
        p.add(vl);
        
        JLabel sl = label(sub, 10, false, ThemeManager.get().sub()); 
        sl.setAlignmentX(LEFT_ALIGNMENT); 
        p.add(sl);
        return p;
    }

    private JPanel trendRow(String letterPair, int pct) {
        ThemeManager tm = ThemeManager.get();
        JPanel row = new JPanel(); 
        row.setOpaque(false); 
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        
        JPanel top = new JPanel(new BorderLayout()); 
        top.setOpaque(false);
        top.add(label(letterPair, 18, true, tm.tx()), BorderLayout.WEST);
        top.add(label(pct + "%", 11, false, tm.sub()), BorderLayout.EAST);
        row.add(top);
        
        CustomProgressBar bar = new CustomProgressBar();
        bar.setValue(pct); 
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 7));
        bar.setFillColor(pct > 50 ? tm.warn() : tm.ac());
        row.add(bar);
        
        return row;
    }

    private JPanel stepRow(String num, String text) {
        ThemeManager tm = ThemeManager.get();
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4)); 
        row.setOpaque(false);
        
        JLabel numLbl = new JLabel(num, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.get().acl()); 
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose(); 
                super.paintComponent(g);
            }
        };
        numLbl.setFont(tm.bold(11)); 
        numLbl.setForeground(tm.ac());
        numLbl.setPreferredSize(new Dimension(24, 24)); 
        numLbl.setOpaque(false);
        row.add(numLbl);
        
        JLabel txt = label(text, 13, false, tm.tx());
        txt.setPreferredSize(new Dimension(380, 20));
        row.add(txt);
        
        return row;
    }

    private JButton buildLinkRow(String emoji, String title, String sub) {
        ThemeManager tm = ThemeManager.get();
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tm.sf()); 
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));
                g2.setColor(tm.bd()); 
                g2.setStroke(new java.awt.BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 18, 18));
                
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20)); 
                g2.setColor(tm.tx()); 
                g2.drawString(emoji, 14, 28);
                
                g2.setFont(tm.bold(13)); 
                g2.setColor(tm.tx()); 
                g2.drawString(title, 46, 22);
                
                g2.setFont(tm.regular(11)); 
                g2.setColor(tm.sub()); 
                g2.drawString(sub, 46, 36);
                
                g2.setFont(tm.bold(18)); 
                g2.setColor(tm.sub()); 
                g2.drawString("›", getWidth() - 24, 28);
                g2.dispose();
            }
        };
        btn.setOpaque(false); 
        btn.setContentAreaFilled(false); 
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(460, 56)); 
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private String formatDur(int sec) { 
        if (sec < 60) return sec + "s"; 
        return (sec / 60) + "m " + (sec % 60) + "s"; 
    }
}
