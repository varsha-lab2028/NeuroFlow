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

        // ── Greeting ──────────────────────────────────────────────
        String name = s != null ? s.getName().split(" ")[0] : "there";
        addFull(label("Good morning, " + name + "! 👋",
                ScreenUtils.fontSize(this, 22), true, tm.tx()));
        addGap(ScreenUtils.gap(this) / 2);
        addFull(label("Ready to practise today?",
                ScreenUtils.fontSize(this, 13), false, tm.sub()));
        addGap(ScreenUtils.gap(this));

        // ── Gripper status row ────────────────────────────────────
        JPanel gripRow = buildGripperRow();
        gripRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                Math.max(52, (int)(ScreenUtils.windowH(this) * 0.072))));
        addFull(gripRow);
        addGap(ScreenUtils.gap(this));

        // ── Module section label ──────────────────────────────────
        addFull(sectionLabel("CHOOSE A MODULE"));
        addGap(ScreenUtils.gap(this) / 2);

        // ── Module tiles ──────────────────────────────────────────
        String[][] modules = {
                {"✏️", "Literacy",        "Letters & reading",
                        "b, d, p, q  •  phonics",   "4E8FC5"},
                {"🔢", "Numeracy",        "Numbers & counting",
                        "1–10  •  number shapes",    "3A9462"},
                {"🧩", "Thinking Skills", "Patterns & sequences",
                        "shapes  •  visual match",   "9B7ED4"},
        };

        for (String[] m : modules) {
            Color iconBg = new Color(Integer.parseInt(m[4], 16) | 0x20000000, true);
            ModuleTile tile = new ModuleTile(m[0], m[1], m[2], m[3], iconBg);
            // Let ModuleTile.getPreferredSize() decide the height — no override here
            tile.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                    ScreenUtils.moduleH(this)));
            tile.setAlignmentX(LEFT_ALIGNMENT);
            tile.setOnClick(() -> {
                AppState.get().setSelectedLetter(
                        s != null ? s.getCurrentLetter() : "b");
                frame.showPanel(MainFrame.WATCH);
            });
            addFull(tile);
            addGap(ScreenUtils.gap(this));
        }

        // ── Yesterday's practice chips ────────────────────────────
        addGap(ScreenUtils.gap(this) / 2);

        RoundedPanel chipCard = new RoundedPanel(18);
        chipCard.setLayout(new BoxLayout(chipCard, BoxLayout.Y_AXIS));

        int cp = ScreenUtils.cardPad(this);
        chipCard.setBorder(new EmptyBorder(cp, cp, cp, cp));
        chipCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                Math.max(100, (int)(ScreenUtils.windowH(this) * 0.145))));
        chipCard.setAlignmentX(LEFT_ALIGNMENT);

        JLabel chipTitle = label("Yesterday's practice",
                ScreenUtils.fontSize(this, 14), true, tm.tx());
        chipTitle.setAlignmentX(LEFT_ALIGNMENT);
        chipCard.add(chipTitle);
        chipCard.add(Box.createVerticalStrut(ScreenUtils.gap(this)));

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT,
                Math.max(6, (int)(ScreenUtils.windowW(this) * 0.018)), 0));
        chips.setOpaque(false);

        String[] practicedLetters = s != null
                ? getPracticedLetters(s) : new String[]{"b", "d", "p"};
        String[] allLetters = {"b", "d", "p", "q"};

        for (String ltr : allLetters) {
            chips.add(letterChip(ltr, contains(practicedLetters, ltr)));
        }
        chips.setAlignmentX(LEFT_ALIGNMENT);
        chipCard.add(chips);
        chipCard.add(Box.createVerticalStrut(ScreenUtils.gap(this) / 2));
        chipCard.add(label(
                practicedLetters.length + " of 4 letters practised ✓",
                ScreenUtils.fontSize(this, 12), false, tm.sub()));

        addFull(chipCard);
        contentArea.revalidate();
        contentArea.repaint();
    }

    // ── Gripper status row ────────────────────────────────────────

    private JPanel buildGripperRow() {
        boolean conn = AppState.get().isGripperConnected();
        ThemeManager tm = ThemeManager.get();

        int hPad = ScreenUtils.pad(this);
        int vPad = Math.max(8, (int)(ScreenUtils.windowH(this) * 0.012));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, hPad / 2, vPad)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.get().acl());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        row.setOpaque(false);

        // Dot indicator
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.BOLD,
                ScreenUtils.fontSize(this, 12)));
        dot.setForeground(conn ? tm.ok() : tm.er());
        row.add(dot);

        // Status text
        JPanel txt = new JPanel();
        txt.setOpaque(false);
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        txt.add(label(
                conn ? "Smart Gripper connected" : "Smart Gripper disconnected",
                ScreenUtils.fontSize(this, 13), true, tm.tx()));
        txt.add(label(
                conn ? "Ready to start writing" : "Please connect your gripper",
                ScreenUtils.fontSize(this, 11), false, tm.sub()));
        row.add(txt);

        // Demo toggle button
        JButton toggle = new JButton(conn ? "Disconnect (demo)" : "Simulate Connect");
        toggle.setFont(tm.regular(ScreenUtils.fontSize(this, 11)));
        toggle.setForeground(tm.ac());
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

    // ── Letter chip ───────────────────────────────────────────────

    private JLabel letterChip(String letter, boolean done) {
        ThemeManager tm = ThemeManager.get();

        // Chip size scales with window
        int chipSize = Math.max(36, (int)(ScreenUtils.windowW(this) * 0.088));

        JLabel chip = new JLabel(letter, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(done ? tm.withAlpha(tm.ok(), 30) : tm.alt());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(done ? tm.withAlpha(tm.ok(), 70) : tm.bd());
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setFont(tm.serif(ScreenUtils.fontSize(this, 18)));
        chip.setForeground(done ? tm.ok() : tm.sub());
        chip.setPreferredSize(new Dimension(chipSize, chipSize));
        chip.setOpaque(false);
        return chip;
    }

    // ── Helpers ───────────────────────────────────────────────────

    private String[] getPracticedLetters(Student s) {
        List<com.neuroflow.model.PracticeSession> sessions =
                PracticeSessionService.get().getTodaySessions(s.getStudentId());
        return sessions.stream()
                .map(ps -> ps.getTargetLetter())
                .distinct()
                .toArray(String[]::new);
    }

    private boolean contains(String[] arr, String val) {
        for (String a : arr) if (a.equals(val)) return true;
        return false;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = label(text, ScreenUtils.fontSize(this, 11),
                true, ThemeManager.get().sub());
        l.setBorder(new EmptyBorder(0, 0, 0, 0));
        return l;
    }
}