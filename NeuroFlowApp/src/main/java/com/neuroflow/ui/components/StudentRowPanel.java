package com.neuroflow.ui.components;

import com.neuroflow.model.Student;
import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Ellipse2D;

public class StudentRowPanel extends JPanel implements ThemeManager.ThemeListener {
    private Student student;

    public StudentRowPanel(Student student) {
        this.student = student;
        setOpaque(false);
        setPreferredSize(new Dimension(400, 88));
        ThemeManager.get().addListener(this);
    }

    public void setStudent(Student s) {
        student = s;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        ThemeManager tm = ThemeManager.get();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = getWidth(), h = getHeight();
        
        // Card
        g2.setColor(tm.sf());
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));
        g2.setColor(tm.bd());
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 14, 14));
        
        // Avatar
        g2.setColor(tm.acl());
        g2.fill(new Ellipse2D.Float(14, 14, 38, 38));
        g2.setFont(tm.bold(13));
        g2.setColor(tm.ac());
        FontMetrics fm = g2.getFontMetrics();
        String init = student.getInitials();
        g2.drawString(init, 
                14 + (38 - fm.stringWidth(init)) / 2, 
                14 + (38 - fm.getHeight()) / 2 + fm.getAscent());
        
        // Name
        g2.setFont(tm.bold(14));
        g2.setColor(tm.tx());
        g2.drawString(student.getName(), 62, 28);
        
        // Issue badge (simple colored text)
        String issue = student.getPrimaryIssue();
        if (issue != null && !issue.isEmpty()) {
            Color bc;
            if (student.getWeeklyProgress() < 50) bc = tm.er();
            else if (student.getWeeklyProgress() < 75) bc = tm.warn();
            else bc = tm.ok();
            
            g2.setFont(tm.bold(10));
            g2.setColor(bc);
            g2.drawString(issue, 62, 42);
        }
        
        // Progress % (right side)
        g2.setFont(tm.bold(16));
        g2.setColor(tm.tx());
        String pct = student.getWeeklyProgress() + "%";
        g2.drawString(pct, w - 52, 30);
        
        // Trend arrow
        String trend = student.getTrend();
        Color tc = trend.equals("↑") ? tm.ok() : trend.equals("↓") ? tm.er() : tm.sub();
        g2.setFont(tm.bold(14));
        g2.setColor(tc);
        g2.drawString(trend, w - 28, 30);
        
        // Progress bar
        int barY = 55, barH = 7, barW = w - 28;
        g2.setColor(tm.alt());
        g2.fill(new RoundRectangle2D.Float(14, barY, barW, barH, barH, barH));
        
        int fillW = (int) (barW * student.getWeeklyProgress() / 100.0);
        Color fillC = student.getWeeklyProgress() >= 75 ? tm.ok() : 
                      student.getWeeklyProgress() >= 50 ? tm.ac() : tm.warn();
                      
        if (fillW > 0) {
            g2.setColor(fillC);
            g2.fill(new RoundRectangle2D.Float(14, barY, fillW, barH, barH, barH));
        }
        g2.dispose();
    }

    @Override public void onThemeChanged() { repaint(); }
}
