package com.neuroflow.ui.components;

import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Animates stroke-by-stroke drawing of letters b, d, p, q using Path2D + Timer.
 * Matches the SVG animation from the HTML prototype.
 */
public class AnimatedLetterPanel extends JPanel implements ThemeManager.ThemeListener {
    private String letter = "b";
    private float progress1 = 0f; // 0-1 stroke 1
    private float progress2 = 0f; // 0-1 stroke 2
    private int phase = 0;        // 0=idle,1=stroke1,2=stroke2,3=done
    private Timer animTimer;
    private Runnable onComplete;
    private static final int W = 130, H = 152;
    
    // Flattened path points for each stroke (pre-computed)
    private List<Point2D> stroke1pts;
    private List<Point2D> stroke2pts;

    public AnimatedLetterPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(W, H));
        ThemeManager.get().addListener(this);
        setLetter("b");
    }

    public void setLetter(String l) {
        this.letter = l;
        phase = 0; progress1 = 0; progress2 = 0;
        buildPaths();
        repaint();
    }

    private void buildPaths() {
        stroke1pts = new ArrayList<>();
        stroke2pts = new ArrayList<>();
        switch (letter) {
            case "b" -> {
                flattenLine(stroke1pts, 40, 12, 40, 148, 80);
                flattenCubic(stroke2pts, 40, 82, 40, 56, 108, 56, 108, 112, 80);
                flattenCubic(stroke2pts, 108, 112, 108, 158, 64, 163, 40, 148, 80);
            }
            case "d" -> {
                flattenLine(stroke1pts, 90, 12, 90, 148, 80);
                flattenCubic(stroke2pts, 90, 82, 90, 56, 22, 56, 22, 112, 80);
                flattenCubic(stroke2pts, 22, 112, 22, 158, 66, 163, 90, 148, 80);
            }
            case "p" -> {
                flattenLine(stroke1pts, 40, 30, 40, 168, 80);
                flattenCubic(stroke2pts, 40, 30, 40, 4, 108, 4, 108, 62, 80);
                flattenCubic(stroke2pts, 108, 62, 108, 106, 64, 110, 40, 96, 80);
            }
            case "q" -> {
                flattenLine(stroke1pts, 90, 30, 90, 168, 80);
                flattenCubic(stroke2pts, 90, 30, 90, 4, 22, 4, 22, 62, 80);
                flattenCubic(stroke2pts, 22, 62, 22, 106, 66, 110, 90, 96, 80);
            }
        }
    }

    private void flattenLine(List<Point2D> pts, double x1, double y1, double x2, double y2, int steps) {
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            pts.add(new Point2D.Double(x1 + (x2 - x1) * t, y1 + (y2 - y1) * t));
        }
    }

    private void flattenCubic(List<Point2D> pts, double x0, double y0, double cx1, double cy1, double cx2, double cy2, double x1, double y1, int steps) {
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps, u = 1 - t;
            double x = u * u * u * x0 + 3 * u * u * t * cx1 + 3 * u * t * t * cx2 + t * t * t * x1;
            double y = u * u * u * y0 + 3 * u * u * t * cy1 + 3 * u * t * t * cy2 + t * t * t * y1;
            pts.add(new Point2D.Double(x, y));
        }
    }

    public void play() {
        if (animTimer != null) animTimer.stop();
        phase = 1; progress1 = 0; progress2 = 0;
        animTimer = new Timer(16, e -> {
            if (phase == 1) {
                progress1 = Math.min(1f, progress1 + 0.012f);
                if (progress1 >= 1f) { phase = 2; }
            } else if (phase == 2) {
                progress2 = Math.min(1f, progress2 + 0.010f);
                if (progress2 >= 1f) { 
                    phase = 3; 
                    ((Timer) e.getSource()).stop(); 
                    if (onComplete != null) onComplete.run(); 
                }
            }
            repaint();
        });
        animTimer.start();
    }

    public void reset() {
        if (animTimer != null) animTimer.stop();
        phase = 0; progress1 = 0; progress2 = 0; repaint();
    }

    public boolean isDone() { return phase == 3; }

    public void setOnComplete(Runnable r) { onComplete = r; }

    @Override
    protected void paintComponent(Graphics g) {
        ThemeManager tm = ThemeManager.get();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(11, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // Ghost letter (full path in alt color)
        g2.setColor(tm.alt());
        drawPartialPath(g2, stroke1pts, 1.0f);
        drawPartialPath(g2, stroke2pts, 1.0f);
        
        // Animated strokes
        if (phase >= 1) {
            g2.setColor(phase == 3 ? tm.tx() : tm.ac());
            drawPartialPath(g2, stroke1pts, progress1);
        }
        if (phase >= 2) {
            g2.setColor(phase == 3 ? tm.tx() : tm.ac());
            drawPartialPath(g2, stroke2pts, progress2);
        }
        
        // Start dot when idle
        if (phase == 0 && !stroke1pts.isEmpty()) {
            Point2D p = stroke1pts.get(0);
            g2.setColor(new Color(tm.ac().getRed(), tm.ac().getGreen(), tm.ac().getBlue(), 80));
            g2.fill(new Ellipse2D.Double(p.getX() - 10, p.getY() - 10, 20, 20));
            g2.setColor(tm.ac());
            g2.fill(new Ellipse2D.Double(p.getX() - 5, p.getY() - 5, 10, 10));
        }
        g2.dispose();
    }

    private void drawPartialPath(Graphics2D g2, List<Point2D> pts, float prog) {
        if (pts.size() < 2) return;
        int end = Math.max(1, (int) (pts.size() * prog));
        GeneralPath path = new GeneralPath();
        path.moveTo(pts.get(0).getX(), pts.get(0).getY());
        for (int i = 1; i < end; i++) path.lineTo(pts.get(i).getX(), pts.get(i).getY());
        g2.draw(path);
    }

    @Override public void onThemeChanged() { repaint(); }
}
