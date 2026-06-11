package com.neuroflow.ui.panels;

import com.neuroflow.AppState;
import com.neuroflow.model.Student;
import com.neuroflow.model.User;
import com.neuroflow.service.StudentService;
import com.neuroflow.ui.MainFrame;
import com.neuroflow.ui.ScreenUtils;
import com.neuroflow.ui.components.RoundedButton;
import com.neuroflow.ui.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class LoginPanel extends JPanel implements ThemeManager.ThemeListener {

    private final MainFrame frame;

    public LoginPanel(MainFrame frame) {
        this.frame = frame;
        ThemeManager.get().addListener(this);
        setOpaque(false);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        // Scrollable content column
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(40, 32, 40, 32));

        ThemeManager tm = ThemeManager.get();

        // ── Brain emoji + title ───────────────────────────────────
        JLabel logo = new JLabel("🧠", SwingConstants.CENTER);
        logo.setFont(ThemeManager.emojiFont(56));
        logo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("NeuroFlow", SwingConstants.CENTER);
        title.setFont(tm.bold(26));
        title.setForeground(tm.tx());
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("Smart Pencil Gripper", SwingConstants.CENTER);
        tagline.setFont(tm.regular(13));
        tagline.setForeground(tm.sub());
        tagline.setAlignmentX(CENTER_ALIGNMENT);

        content.add(logo);
        content.add(Box.createVerticalStrut(10));
        content.add(title);
        content.add(Box.createVerticalStrut(4));
        content.add(tagline);
        content.add(Box.createVerticalStrut(28));

        // ── "Who's learning today?" heading ───────────────────────
        JLabel heading = new JLabel("Who's learning today?", SwingConstants.CENTER);
        heading.setFont(tm.bold(20));
        heading.setForeground(tm.tx());
        heading.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Tap your name to start", SwingConstants.CENTER);
        sub.setFont(tm.regular(13));
        sub.setForeground(tm.sub());
        sub.setAlignmentX(CENTER_ALIGNMENT);

        content.add(heading);
        content.add(Box.createVerticalStrut(4));
        content.add(sub);
        content.add(Box.createVerticalStrut(20));

        // ── Child picker cards ────────────────────────────────────
        List<Student> students = StudentService.get().getAllStudents();
        for (Student s : students) {
            content.add(buildChildCard(s));
            content.add(Box.createVerticalStrut(8));
        }

        content.add(Box.createVerticalStrut(20));

        // ── Divider label ─────────────────────────────────────────
        JLabel divider = new JLabel("Parent or educator?", SwingConstants.CENTER);
        divider.setFont(tm.regular(12));
        divider.setForeground(tm.sub());
        divider.setAlignmentX(CENTER_ALIGNMENT);
        content.add(divider);
        content.add(Box.createVerticalStrut(10));

        // ── Parent / Educator buttons ─────────────────────────────
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        RoundedButton parentBtn   = new RoundedButton("👨‍👩‍👧  Parent",   RoundedButton.Style.MUTED);
        RoundedButton educatorBtn = new RoundedButton("🎓  Educator", RoundedButton.Style.MUTED);

        parentBtn.addActionListener(e   -> loginWithPin("parent"));
        educatorBtn.addActionListener(e -> loginWithPin("educator"));

        btnRow.add(parentBtn);
        btnRow.add(educatorBtn);
        content.add(btnRow);
        content.add(Box.createVerticalStrut(24));

        // ── ML server status ──────────────────────────────────────
        JLabel serverLbl = new JLabel("Checking ML server...", SwingConstants.CENTER);
        serverLbl.setFont(tm.regular(11));
        serverLbl.setForeground(tm.sub());
        serverLbl.setAlignmentX(CENTER_ALIGNMENT);
        content.add(serverLbl);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() {
                return com.neuroflow.service.MLService.checkServer();
            }
            @Override protected void done() {
                try {
                    boolean ok = get();
                    serverLbl.setText(ok
                            ? "✅  ML Server online — 192.168.1.9:5000"
                            : "⚠️  ML Server offline — simulation mode active");
                    serverLbl.setForeground(ok ? tm.ok() : tm.warn());
                } catch (Exception ignored) {}
            }
        };
        worker.execute();

        // Wrap in scroll pane so it works on small screens too
        JScrollPane scroll = new JScrollPane(content);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    // ── Child card ────────────────────────────────────────────────

    private JPanel buildChildCard(Student s) {
        ThemeManager tm = ThemeManager.get();

        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tm.sf());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));
                g2.setColor(tm.bd());
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f,
                        getWidth() - 1, getHeight() - 1, 18, 18));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 14, 12, 14));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Avatar circle with initials
        String initials = s.getInitials() != null ? s.getInitials()
                : (s.getName().length() >= 2 ? s.getName().substring(0, 2).toUpperCase() : "?");
        JLabel avatar = new JLabel(initials, SwingConstants.CENTER) {
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
        avatar.setFont(tm.bold(14));
        avatar.setForeground(tm.ac());
        avatar.setPreferredSize(new Dimension(44, 44));
        avatar.setOpaque(false);

        // Name + subtitle
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel nameLbl = new JLabel(s.getName());
        nameLbl.setFont(tm.bold(15));
        nameLbl.setForeground(tm.tx());

        String letter = s.getCurrentLetter() != null ? s.getCurrentLetter() : "b";
        int streak    = s.getStreakDays();
        JLabel subLbl = new JLabel("Practising \"" + letter + "\"  ·  " + streak + " day streak");
        subLbl.setFont(tm.regular(12));
        subLbl.setForeground(tm.sub());

        info.add(nameLbl);
        info.add(Box.createVerticalStrut(2));
        info.add(subLbl);

        // Arrow
        JLabel arrow = new JLabel("›");
        arrow.setFont(tm.bold(22));
        arrow.setForeground(tm.sub());

        card.add(avatar, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);
        card.add(arrow, BorderLayout.EAST);

        // Click → login as child
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                loginAsChild(s);
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBorder(new EmptyBorder(11, 13, 11, 13));
                card.repaint();
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBorder(new EmptyBorder(12, 14, 12, 14));
                card.repaint();
            }
        });

        return card;
    }

    // ── Login logic ───────────────────────────────────────────────

    private void loginAsChild(Student s) {
        User user = new User(s.getUserId(),
                s.getName(), "child", null, null);
        AppState.get().setCurrentUser(user);
        AppState.get().setCurrentRole("child");
        AppState.get().setCurrentStudent(s);
        AppState.get().setSelectedLetter(
                s.getCurrentLetter() != null ? s.getCurrentLetter() : "b");
        frame.showPanel(MainFrame.HOME);
    }

    private void loginWithPin(String role) {
        String label  = role.substring(0, 1).toUpperCase() + role.substring(1);
        String demo   = role.equals("parent") ? "1234" : "5678";
        String pin = JOptionPane.showInputDialog(this,
                "Enter 4-digit PIN for " + label + " access:\n(Demo PIN: " + demo + ")",
                "PIN Required", JOptionPane.QUESTION_MESSAGE);
        if (pin == null) return;
        pin = pin.trim();

        com.neuroflow.service.UserDao uDao =
                new com.neuroflow.service.UserDao();
        com.neuroflow.model.User user =
                uDao.authenticate(role, pin);

        if (user == null) {
            JOptionPane.showMessageDialog(this,
                    "Incorrect PIN. Try again.",
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        AppState.get().setCurrentUser(user);
        AppState.get().setCurrentRole(role);

        // Set first student as current child context for parent/educator
        List<Student> all = StudentService.get().getAllStudents();
        if (!all.isEmpty()) AppState.get().setCurrentStudent(all.get(0));

        frame.showPanel(role.equals("parent") ? MainFrame.PARENT : MainFrame.EDUCATOR);
    }

    // ── Painting ──────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(ThemeManager.get().bg());
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    @Override
    public void onThemeChanged() { repaint(); }
}
