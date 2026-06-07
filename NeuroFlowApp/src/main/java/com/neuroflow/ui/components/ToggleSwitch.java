package com.neuroflow.ui.components;

import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Ellipse2D;

public class ToggleSwitch extends JPanel implements ThemeManager.ThemeListener {
    private boolean on;
    private Runnable onChange;

    public ToggleSwitch(boolean initialOn) {
        this.on = initialOn;
        setOpaque(false);
        setPreferredSize(new Dimension(50, 28));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                on = !on; 
                repaint();
                if (onChange != null) onChange.run();
            }
        });
        ThemeManager.get().addListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        ThemeManager tm = ThemeManager.get();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = getWidth(), h = getHeight();
        g2.setColor(on ? tm.ok() : tm.alt());
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
        
        float kx = on ? w - h + 3 : 3;
        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Float(kx, 3, h - 6, h - 6));
        g2.dispose();
    }

    public boolean isOn() { return on; }
    public void setOn(boolean v) { on = v; repaint(); }
    public void setOnChange(Runnable r) { onChange = r; }

    @Override public void onThemeChanged() { repaint(); }
}
