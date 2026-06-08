/*
package com.neuroflow.ui.panels;

import com.neuroflow.AppState;
import com.neuroflow.model.Student;
import com.neuroflow.model.User;
import com.neuroflow.service.AuthService;
import com.neuroflow.service.StudentService;
import com.neuroflow.ui.MainFrame;
import com.neuroflow.ui.components.RoundedButton;
import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class LoginPanel extends JPanel implements ThemeManager.ThemeListener {
    private final MainFrame frame;

    public LoginPanel(MainFrame frame) {
        this.frame = frame;
        ThemeManager.get().addListener(this);
        setOpaque(false);
        buildUI();
    }

    private void buildUI() {
        setLayout(new GridBagLayout());
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(new EmptyBorder(0, 40, 0, 40));

        // Logo / title
        JLabel logo = new JLabel("🧠", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("NeuroFlow", SwingConstants.CENTER);
        title.setFont(ThemeManager.get().bold(28));
        title.setForeground(ThemeManager.get().tx());
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Smart Pencil Gripper for Neurodivergent Children", SwingConstants.CENTER);
        sub.setFont(ThemeManager.get().regular(13));
        sub.setForeground(ThemeManager.get().sub());
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel choose = new JLabel("Choose your role to get started", SwingConstants.CENTER);
        choose.setFont(ThemeManager.get().regular(13));
        choose.setForeground(ThemeManager.get().sub());
        choose.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Role buttons
        RoundedButton childBtn = makeBtn("🧒  Child — Start Learning", RoundedButton.Style.PRIMARY);
        RoundedButton parentBtn = makeBtn("👨‍👩‍👧  Parent — View Progress", RoundedButton.Style.MUTED);
        RoundedButton eduBtn = makeBtn("🎓  Educator — Class Overview", RoundedButton.Style.MUTED);

        childBtn.addActionListener(e -> loginAs("child", null));
        parentBtn.addActionListener(e -> loginWithPin("parent"));
        eduBtn.addActionListener(e -> loginWithPin("educator"));

        // ML server status
        JLabel serverLbl = new JLabel("Checking ML server...", SwingConstants.CENTER);
        serverLbl.setFont(ThemeManager.get().regular(11));
        serverLbl.setForeground(ThemeManager.get().sub());
        serverLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Check server in background
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() { return com.neuroflow.service.MLService.checkServer(); }
            @Override protected void done() {
                try {
                    boolean ok = get();
                    serverLbl.setText(ok ? "✅ ML Server online — 192.168.1.9:5000" : "⚠️ ML Server offline — simulation mode active");
                    serverLbl.setForeground(ok ? ThemeManager.get().ok() : ThemeManager.get().warn());
                } catch (Exception ignored) {}
            }
        };
        worker.execute();

        center.add(logo); center.add(Box.createVerticalStrut(8));
        center.add(title); center.add(Box.createVerticalStrut(6));
        center.add(sub); center.add(Box.createVerticalStrut(28));
        center.add(choose); center.add(Box.createVerticalStrut(16));
        center.add(childBtn); center.add(Box.createVerticalStrut(10));
        center.add(parentBtn); center.add(Box.createVerticalStrut(10));
        center.add(eduBtn); center.add(Box.createVerticalStrut(20));
        center.add(serverLbl);
        add(center);
    }

    private RoundedButton makeBtn(String text, RoundedButton.Style style) {
        RoundedButton b = new RoundedButton(text, style);
        b.setPreferredSize(new Dimension(380, 52));
        b.setMaximumSize(new Dimension(380, 52));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        return b;
    }

    private void loginAs(String role, String pin) {
        User user = (pin == null) ? AuthService.get().loginAsRole(role)
                                  : AuthService.get().authenticateWithPin(role, pin);

        if (user == null && pin != null) {
            JOptionPane.showMessageDialog(this, "Incorrect PIN. Try again.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (user == null) user = new User(0, role.substring(0, 1).toUpperCase() + role.substring(1), role, null);
        AppState.get().setCurrentUser(user);
        AppState.get().setCurrentRole(role);

        if (role.equals("child")) {
            StudentService ss = StudentService.get();
            List<Student> all = ss.getAllStudents();
            Student s = all.isEmpty() ? null : all.get(0);
            AppState.get().setCurrentStudent(s);
        }

        frame.showPanel(role.equals("child") ? MainFrame.HOME 
                      : role.equals("parent") ? MainFrame.PARENT : MainFrame.EDUCATOR);
    }

    private void loginWithPin(String role) {
        String pin = JOptionPane.showInputDialog(this, "Enter 4-digit PIN for " + role + " access:", "PIN Required", JOptionPane.QUESTION_MESSAGE);
        if (pin == null) return;
        loginAs(role, pin.trim());
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(ThemeManager.get().bg());
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    @Override public void onThemeChanged() { repaint(); }
}
 */
