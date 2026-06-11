package com.neuroflow;

import com.neuroflow.AppState;
import com.neuroflow.service.StudentService;
import com.neuroflow.model.Student;
import com.neuroflow.ui.panels.*;
import com.neuroflow.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame implements ThemeManager.ThemeListener {

    public static final String LOGIN    = "login";
    public static final String HOME     = "home";
    public static final String WATCH    = "watch";
    public static final String TRY      = "try";
    public static final String GUIDE    = "guide";
    public static final String WIN      = "win";
    public static final String PARENT   = "parent";
    public static final String EDUCATOR = "educator";

    private CardLayout cardLayout;
    private JPanel cardContainer;

    private LoginPanel            loginPanel;
    private HomePanel             homePanel;
    private WatchPanel            watchPanel;
    private TryPanel              tryPanel;
    private GuidePanel            guidePanel;
    private WinPanel              winPanel;
    private ParentDashboardPanel  parentPanel;
    private EducatorPanel         educatorPanel;
    private SettingsOverlay       settingsOverlay;

    private String currentScreen = LOGIN;

    public MainFrame() {
        super("NeuroFlow — Smart Pencil Gripper");
        ThemeManager.get().addListener(this);
        initUI();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(ScreenUtils.INIT_W, ScreenUtils.INIT_H);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(380, 600));
        setVisible(true);
    }

    private void initUI() {
        JPanel root = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(ThemeManager.get().bg());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setOpaque(true);

        cardLayout    = new CardLayout();
        cardContainer = new JPanel(cardLayout) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(ThemeManager.get().bg());
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            @Override
            public Dimension getPreferredSize() {
                int w = ScreenUtils.SCREEN_W;
                return new Dimension(
                        Math.min(680, Math.max(ScreenUtils.INIT_W, w / 3)),
                        super.getPreferredSize().height);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(680, Integer.MAX_VALUE);
            }
        };
        cardContainer.setOpaque(true);

        // Build all panels
        loginPanel    = new LoginPanel(this);
        homePanel     = new HomePanel(this);
        watchPanel    = new WatchPanel(this);
        tryPanel      = new TryPanel(this);
        guidePanel    = new GuidePanel(this);
        winPanel      = new WinPanel(this);
        parentPanel   = new ParentDashboardPanel(this);
        educatorPanel = new EducatorPanel(this);

        cardContainer.add(loginPanel,    LOGIN);
        cardContainer.add(homePanel,     HOME);
        cardContainer.add(watchPanel,    WATCH);
        cardContainer.add(tryPanel,      TRY);
        cardContainer.add(guidePanel,    GUIDE);
        cardContainer.add(winPanel,      WIN);
        cardContainer.add(parentPanel,   PARENT);
        cardContainer.add(educatorPanel, EDUCATOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.CENTER;
        root.add(cardContainer, gbc);

        settingsOverlay = new SettingsOverlay(this);
        setGlassPane(settingsOverlay);

        setContentPane(root);
        cardLayout.show(cardContainer, LOGIN);
    }

    public void showPanel(String name) {
        currentScreen = name;
        switch (name) {
            case HOME     -> homePanel.refresh();
            case WATCH    -> watchPanel.refresh();
            case TRY      -> tryPanel.refresh();
            case GUIDE    -> guidePanel.refresh();
            case WIN      -> winPanel.refresh();
            case PARENT   -> parentPanel.refresh();
            case EDUCATOR -> educatorPanel.refresh();
        }
        cardLayout.show(cardContainer, name);
        cardContainer.repaint();
    }

    public void openSettings() {
        settingsOverlay.setVisible(true);
        settingsOverlay.repaint();
    }

    public void closeSettings() {
        settingsOverlay.setVisible(false);
    }

    public String getCurrentScreen() { return currentScreen; }

    /**
     * Role-bar switch.
     * Child → just navigate.
     * Parent / Educator → prompt for PIN first.
     */
    public void switchRole(String role) {
        if ("child".equals(role)) {
            AppState.get().setCurrentRole("child");
            // Restore first student
            List<Student> all = StudentService.get().getAllStudents();
            if (!all.isEmpty()) {
                AppState.get().setCurrentStudent(all.get(0));
                AppState.get().setSelectedLetter(
                        all.get(0).getCurrentLetter() != null
                                ? all.get(0).getCurrentLetter() : "b");
            }
            showPanel(HOME);
            return;
        }

        // Parent or educator — need PIN
        String demo = role.equals("parent") ? "1234" : "5678";
        String pin  = JOptionPane.showInputDialog(this,
                "Enter 4-digit PIN for "
                        + role.substring(0, 1).toUpperCase()
                        + role.substring(1) + " access:\n(Demo PIN: " + demo + ")",
                "PIN Required", JOptionPane.QUESTION_MESSAGE);
        if (pin == null) return;

        com.neuroflow.service.UserDao uDao = new com.neuroflow.service.UserDao();
        com.neuroflow.model.User user = uDao.authenticate(role, pin.trim());
        if (user == null) {
            JOptionPane.showMessageDialog(this,
                    "Incorrect PIN.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        AppState.get().setCurrentUser(user);
        AppState.get().setCurrentRole(role);
        showPanel(role.equals("parent") ? PARENT : EDUCATOR);
    }

    @Override
    public void onThemeChanged() {
        cardContainer.repaint();
        repaint();
    }
}
