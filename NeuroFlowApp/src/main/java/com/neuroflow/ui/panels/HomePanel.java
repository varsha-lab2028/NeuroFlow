package com.neuroflow.ui.panels;

import com.neuroflow.AppState;
import com.neuroflow.model.Student;
import com.neuroflow.service.PracticeSessionService;
import com.neuroflow.ui.MainFrame;
import com.neuroflow.ui.components.*;
import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class HomePanel extends BasePanel {
    public HomePanel(MainFrame frame) {
        super(frame, "Learning Time", true, false, null);
        refresh();
    }

    @Override
    public void refresh() {
        contentArea.removeAll();
        ThemeManager tm = ThemeManager.get();
        Student s = AppState.get().getCurrentStudent();

        // Greeting
        String name = s != null ? s.getName().split(" ")[0] : "there";
        addFull(label("Good morning, " + name + "! 👋", 22, true, tm.tx()));
        addGap(4);
        addFull(label("Ready to practise today?", 13, false, tm.sub()));
        addGap(16);

        // Gripper status bar
        JPanel gripRow = buildGripperRow();
        gripRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        addFull(gripRow); 
        addGap(14);

        // Module label
        addFull(sectionLabel("CHOOSE A MODULE")); 
        addGap(8);

        // Module tiles
        String[][] modules = {
                {"✏️", "Literacy",       "Letters & reading",       "b, d, p, q  •  phonics",      "4E8FC5","21"},
                {"🔢", "Numeracy",       "Numbers & counting",      "1–10  •  number shapes",       "3A9462","13"},
                {"🧩", "Thinking Skills","Patterns & sequences",    "shapes  •  visual match",      "9B7ED4","12"},
        };

        for (String[] m : modules) {
            Color iconBg = new Color(Integer.parseInt(m[4], 16) | 0x20000000, true);
            ModuleTile tile = new ModuleTile(m[0], m[1], m[2], m[3], iconBg);
            tile.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
            tile.setPreferredSize(new Dimension(460, 82));
            tile.setOnClick(() -> {
                AppState.get().setSelectedLetter(s != null ? s.getCurrentLetter() : "b");
                frame.showPanel(MainFrame.WATCH);
            });
            addFull(tile); 
            addGap(10);
        }

        // Yesterday's practice chips
        addGap(4);
        RoundedPanel chipCard = new RoundedPanel(18);
        chipCard.setLayout(new BoxLayout(chipCard, BoxLayout.Y_AXIS));
        chipCard.setBorder(new EmptyBorder(14, 16, 14, 16));
        chipCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel chipTitle = label("Yesterday's practice", 14, true, tm.tx());
        chipTitle.setAlignmentX(LEFT_ALIGNMENT);
        chipCard.add(chipTitle); 
        chipCard.add(Box.createVerticalStrut(10));

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chips.setOpaque(false);
        String[] practicedLetters = s != null ? getPracticedLetters(s) : new String[]{"b","d","p"};
        String[] allLetters = {"b","d","p","q"};

        for (String ltr : allLetters) {
            boolean done = contains(practicedLetters, ltr);
            chips.add(letterChip(ltr, done));
        }
        chips.setAlignmentX(LEFT_ALIGNMENT); 
        chipCard.add(chips);
        chipCard.add(Box.createVerticalStrut(8));
        chipCard.add(label(practicedLetters.length + " of 4 letters practised ✓", 12, false, tm.sub()));
        
        addFull(chipCard);
        contentArea.revalidate(); 
        contentArea.repaint();
    }

    private JPanel buildGripperRow() {
        boolean conn = AppState.get().isGripperConnected();
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)) {
            @Override protected void paintComponent(Graphics g) {
                ThemeManager tm = ThemeManager.get();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tm.acl());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12); 
                g2.dispose();
            }
        };
        row.setOpaque(false);

        // Dot indicator
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dot.setForeground(conn ? ThemeManager.get().ok() : ThemeManager.get().er());
        row.add(dot);

        JPanel txt = new JPanel(); 
        txt.setOpaque(false); 
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        txt.add(label(conn ? "Smart Gripper connected" : "Smart Gripper disconnected", 13, true, ThemeManager.get().tx()));
        txt.add(label(conn ? "Ready to start writing" : "Please connect your gripper", 11, false, ThemeManager.get().sub()));
        row.add(txt);

        // Toggle button for demo
        JButton toggle = new JButton(conn ? "Disconnect (demo)" : "Simulate Connect");
        toggle.setFont(ThemeManager.get().regular(11));
        toggle.setForeground(ThemeManager.get().ac());
        toggle.setOpaque(false); 
        toggle.setContentAreaFilled(false); 
        toggle.setBorderPainted(false);
        toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggle.addActionListener(e -> {
            AppState.get().setGripperConnected(!AppState.get().isGripperConnected());
            refresh();
        });
        row.add(toggle);
        return row;
    }

    private String[] getPracticedLetters(Student s) {
        List<com.neuroflow.model.PracticeSession> sessions = PracticeSessionService.get().getTodaySessions(s.getStudentId());
        return sessions.stream().map(ps -> ps.getTargetLetter()).distinct().toArray(String[]::new);
    }

    private boolean contains(String[] arr, String val) {
        for (String a : arr) if (a.equals(val)) return true; 
        return false;
    }

    private JLabel letterChip(String letter, boolean done) {
        ThemeManager tm = ThemeManager.get();
        JLabel chip = new JLabel(letter, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(done ? tm.withAlpha(tm.ok(), 30) : tm.alt());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(done ? tm.withAlpha(tm.ok(), 70) : tm.bd());
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose(); 
                super.paintComponent(g);
            }
        };
        chip.setFont(tm.serif(18)); 
        chip.setForeground(done ? tm.ok() : tm.sub());
        chip.setPreferredSize(new Dimension(40, 40)); 
        chip.setOpaque(false);
        return chip;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = label(text, 11, true, ThemeManager.get().sub());
        l.setBorder(new EmptyBorder(0, 0, 0, 0)); 
        return l;
    }
}
