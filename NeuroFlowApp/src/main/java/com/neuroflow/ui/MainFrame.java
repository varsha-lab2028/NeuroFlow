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

    // Centre column — holds the actual panel, centred in the window
    private JPanel centreColumn;

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

        // Start at phone-like size, centred
        setSize(ScreenUtils.INIT_W, ScreenUtils.INIT_H);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initAppState() {
        AppState.get().setCurrentRole("child");
        try {
            List<Student> all = StudentService.get().getAllStudents();
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
            AppState.get().setSelectedLetter("b");
            System.out.println("[MainFrame] Could not load students: " + e.getMessage());
        }
    }

    private void initUI() {
        // Root fills the whole window with background colour
        JPanel root = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(ThemeManager.get().bg());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setOpaque(true);

        // Card container — constrained width, centred by GridBagLayout
        cardLayout    = new CardLayout();
        cardContainer = new JPanel(cardLayout) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(ThemeManager.get().bg());
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            @Override
            public Dimension getPreferredSize() {
                // Always phone-width at minimum, caps at 680 when maximised
                int w = ScreenUtils.SCREEN_W;
                return new Dimension(Math.min(680, Math.max(ScreenUtils.INIT_W, w / 3)), super.getPreferredSize().height);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(680, Integer.MAX_VALUE);
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

        cardContainer.add(homePanel,     HOME);
        cardContainer.add(watchPanel,    WATCH);
        cardContainer.add(tryPanel,      TRY);
        cardContainer.add(guidePanel,    GUIDE);
        cardContainer.add(winPanel,      WIN);
        cardContainer.add(parentPanel,   PARENT);
        cardContainer.add(educatorPanel, EDUCATOR);

        // GridBagConstraints centres the cardContainer vertically and horizontally
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;  // stretch height only, not width
        gbc.anchor = GridBagConstraints.CENTER;
        root.add(cardContainer, gbc);

        // Settings overlay as glass pane
        settingsOverlay = new SettingsOverlay(this);
        setGlassPane(settingsOverlay);

        setContentPane(root);
        cardLayout.show(cardContainer, HOME);
    }

    private void reflowCentreColumn() {
        int winW = getWidth();
        int maxContent = 680;

        if (winW <= maxContent + 40) {
            // Phone-like window — fill full width
            centreColumn.setPreferredSize(null);
            centreColumn.setMaximumSize(null);
        } else {
            // Wide window — cap at maxContent and centre
            centreColumn.setPreferredSize(new Dimension(maxContent, getHeight()));
            centreColumn.setMaximumSize(new Dimension(maxContent, Integer.MAX_VALUE));
        }
        revalidate();
        repaint();
    }

    private void repaintAllPanels() {
        homePanel.revalidate();     homePanel.repaint();
        watchPanel.revalidate();    watchPanel.repaint();
        tryPanel.revalidate();      tryPanel.repaint();
        guidePanel.revalidate();    guidePanel.repaint();
        winPanel.revalidate();      winPanel.repaint();
        parentPanel.revalidate();   parentPanel.repaint();
        educatorPanel.revalidate(); educatorPanel.repaint();
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