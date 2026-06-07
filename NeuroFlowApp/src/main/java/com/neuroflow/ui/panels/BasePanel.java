package com.neuroflow.ui.panels;

import com.neuroflow.AppState;
import com.neuroflow.ui.MainFrame;
import com.neuroflow.ui.components.RoundedButton;
import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Base class for all main panels. Provides top-bar, role-bar, scrollable content area.
 */
public abstract class BasePanel extends JPanel implements ThemeManager.ThemeListener {
    protected final MainFrame frame;
    protected JPanel contentArea;
    private JLabel titleLabel;
    private JPanel topBar;

    public BasePanel(MainFrame frame, String title, boolean showRoleBar, boolean showBackButton, String backTarget) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new BorderLayout());
        ThemeManager.get().addListener(this);

        // TOP BAR
        topBar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(ThemeManager.get().sf()); 
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(ThemeManager.get().bd());  
                g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        topBar.setOpaque(false); 
        topBar.setPreferredSize(new Dimension(0, 52));
        topBar.setBorder(new EmptyBorder(0, 16, 0, 16));

        if (showBackButton && backTarget != null) {
            JButton backBtn = new JButton("← Back");
            backBtn.setFont(ThemeManager.get().bold(13));
            backBtn.setForeground(ThemeManager.get().ac());
            backBtn.setOpaque(false); 
            backBtn.setContentAreaFilled(false); 
            backBtn.setBorderPainted(false);
            backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            backBtn.addActionListener(e -> frame.showPanel(backTarget));
            topBar.add(backBtn, BorderLayout.WEST);
        } else {
            topBar.add(Box.createHorizontalStrut(60), BorderLayout.WEST);
        }

        titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(ThemeManager.get().bold(16));
        titleLabel.setForeground(ThemeManager.get().tx());
        topBar.add(titleLabel, BorderLayout.CENTER);

        JButton cogBtn = new JButton("⚙");
        cogBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        cogBtn.setForeground(ThemeManager.get().sub());
        cogBtn.setOpaque(false); 
        cogBtn.setContentAreaFilled(false); 
        cogBtn.setBorderPainted(false);
        cogBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cogBtn.addActionListener(e -> frame.openSettings());
        topBar.add(cogBtn, BorderLayout.EAST);

        JPanel north = new JPanel(); 
        north.setOpaque(false); 
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(topBar);
        if (showRoleBar) north.add(buildRoleBar());
        add(north, BorderLayout.NORTH);

        // CONTENT (scrollable)
        contentArea = new JPanel();
        contentArea.setOpaque(false);
        contentArea.setLayout(new BoxLayout(contentArea, BoxLayout.Y_AXIS));
        contentArea.setBorder(new EmptyBorder(16, 20, 28, 20));

        JScrollPane scroll = new JScrollPane(contentArea);
        scroll.setOpaque(false); 
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null); 
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildRoleBar() {
        JPanel bar = new JPanel(new GridLayout(1, 3, 4, 0)) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(ThemeManager.get().alt()); 
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        bar.setOpaque(false); 
        bar.setBorder(new EmptyBorder(6, 16, 6, 16));
        bar.setPreferredSize(new Dimension(0, 46));

        String[][] roles = {{"🧒 Child","child","home"}, {"👨‍👩‍👧 Parent","parent","parent"}, {"🎓 Educator","educator","educator"}};
        for (String[] r : roles) {
            JButton btn = new JButton(r[0]);
            btn.setFont(ThemeManager.get().bold(12));
            btn.setOpaque(false); 
            btn.setContentAreaFilled(false); 
            btn.setBorderPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            String targetRole = r[1]; 
            
            btn.addActionListener(e -> frame.switchRole(targetRole));
            btn.putClientProperty("role", r[1]);
            bar.add(btn);
        }
        return bar;
    }

    protected void setTitle(String t) { titleLabel.setText(t); }

    /** Add a fixed-width filler so each child stretches full width */
    protected void addFull(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentArea.add(c);
    }

    protected void addGap(int h) { contentArea.add(Box.createVerticalStrut(h)); }

    protected JPanel hRow(JComponent... comps) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        for (JComponent c : comps) p.add(c);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    protected JLabel label(String text, int size, boolean bold, Color col) {
        JLabel l = new JLabel(text); 
        l.setFont(bold ? ThemeManager.get().bold(size) : ThemeManager.get().regular(size));
        l.setForeground(col); 
        return l;
    }

    public abstract void refresh();

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(ThemeManager.get().bg()); 
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    @Override public void onThemeChanged() { repaint(); }
}
