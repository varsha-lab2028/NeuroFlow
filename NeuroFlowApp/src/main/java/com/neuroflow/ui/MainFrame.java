package com.neuroflow.ui;

import com.neuroflow.AppState;
import com.neuroflow.ui.panels.*;
import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame implements ThemeManager.ThemeListener {
    public static final String HOME      = "home";
    public static final String WATCH     = "watch";
    public static final String TRY       = "try";
    public static final String GUIDE     = "guide";
    public static final String WIN       = "win";
    public static final String PARENT    = "parent";
    public static final String EDUCATOR  = "educator";
    public static final String LOGIN     = "login";

    private CardLayout cardLayout;
    private JPanel cardContainer;
    
    private HomePanel homePanel;
    private WatchPanel watchPanel;
    private TryPanel tryPanel;
    private GuidePanel guidePanel;
    private WinPanel winPanel;
    private ParentDashboardPanel parentPanel;
    private EducatorPanel educatorPanel;
    private LoginPanel loginPanel;
    private SettingsOverlay settingsOverlay;

    private String currentScreen = LOGIN;

    public MainFrame() {
        super("NeuroFlow — Smart Pencil Gripper");
        ThemeManager.get().addListener(this);
        initUI();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(520, 720));
        setSize(540, 780);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initUI() {
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(ThemeManager.get().bg());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        cardContainer.setOpaque(true);

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

        // Settings overlay as glass pane
        settingsOverlay = new SettingsOverlay(this);
        setGlassPane(settingsOverlay);

        setContentPane(cardContainer);
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

    // Called by role bar switches
    public void switchRole(String role) {
        AppState.get().setCurrentRole(role);
        switch (role) {
            case "child"    -> showPanel(HOME);
            case "parent"   -> showPanel(PARENT);
            case "educator" -> showPanel(EDUCATOR);
        }
    }

    @Override public void onThemeChanged() {
        cardContainer.repaint();
        repaint();
    }
}
