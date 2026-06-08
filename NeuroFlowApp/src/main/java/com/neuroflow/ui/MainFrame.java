package com.neuroflow.ui;

import com.neuroflow.AppState;
import com.neuroflow.service.StudentService;
import com.neuroflow.model.Student;
import com.neuroflow.ui.panels.*;
import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame implements ThemeManager.ThemeListener {

    public static final String HOME     = "home";
    public static final String WATCH    = "watch";
    public static final String TRY      = "try";
    public static final String GUIDE    = "guide";
    public static final String WIN      = "win";
    public static final String PARENT   = "parent";
    public static final String EDUCATOR = "educator";

    private CardLayout cardLayout;
    private JPanel cardContainer;

    private HomePanel             homePanel;
    private WatchPanel            watchPanel;
    private TryPanel              tryPanel;
    private GuidePanel            guidePanel;
    private WinPanel              winPanel;
    private ParentDashboardPanel  parentPanel;
    private EducatorPanel         educatorPanel;
    private SettingsOverlay       settingsOverlay;

    private String currentScreen = HOME;

    public MainFrame() {
        super("NeuroFlow — Smart Pencil Gripper");
        ThemeManager.get().addListener(this);
        initAppState();
        initUI();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(440, 720));
        setSize(460, 780);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Load default demo data into AppState before any panel renders.
     * Sets the first available student as the current student.
     */
    private void initAppState() {
        AppState.get().setCurrentRole("child");
        try {
            StudentService ss = StudentService.get();
            List<Student> all = ss.getAllStudents();
            if (!all.isEmpty()) {
                Student first = all.get(0);
                AppState.get().setCurrentStudent(first);
                AppState.get().setSelectedLetter(
                        first.getCurrentLetter() != null
                                ? first.getCurrentLetter() : "b");
            } else {
                AppState.get().setSelectedLetter("b");
            }
        } catch (Exception e) {
            // DB not ready yet — panels will handle null student gracefully
            AppState.get().setSelectedLetter("b");
            System.out.println("[MainFrame] Could not load students: " + e.getMessage());
        }
    }

    private void initUI() {
        cardLayout     = new CardLayout();
        cardContainer  = new JPanel(cardLayout) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(ThemeManager.get().bg());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        cardContainer.setOpaque(true);

        // Build all panels
        homePanel     = new HomePanel(this);
        watchPanel    = new WatchPanel(this);
        tryPanel      = new TryPanel(this);
        guidePanel    = new GuidePanel(this);
        winPanel      = new WinPanel(this);
        parentPanel   = new ParentDashboardPanel(this);
        educatorPanel = new EducatorPanel(this);

        // Register panels with CardLayout
        cardContainer.add(homePanel,     HOME);
        cardContainer.add(watchPanel,    WATCH);
        cardContainer.add(tryPanel,      TRY);
        cardContainer.add(guidePanel,    GUIDE);
        cardContainer.add(winPanel,      WIN);
        cardContainer.add(parentPanel,   PARENT);
        cardContainer.add(educatorPanel, EDUCATOR);

        // Settings overlay sits on the glass pane
        settingsOverlay = new SettingsOverlay(this);
        setGlassPane(settingsOverlay);

        setContentPane(cardContainer);

        // Start on Home — no login screen
        cardLayout.show(cardContainer, HOME);
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
     * Called by the role switcher bar in BasePanel.
     * Switches the visible dashboard and updates AppState.
     */
    public void switchRole(String role) {
        AppState.get().setCurrentRole(role);
        switch (role) {
            case "child"    -> showPanel(HOME);
            case "parent"   -> showPanel(PARENT);
            case "educator" -> showPanel(EDUCATOR);
        }
    }

    @Override
    public void onThemeChanged() {
        cardContainer.repaint();
        repaint();
    }
}