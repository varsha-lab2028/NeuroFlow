package com.neuroflow.ui.panels;

import com.neuroflow.AppState;
import com.neuroflow.model.PracticeSession;
import com.neuroflow.model.Student;
import com.neuroflow.service.MLService;
import com.neuroflow.service.SessionService;
import com.neuroflow.ui.MainFrame;
import com.neuroflow.ui.components.CustomProgressBar;
import com.neuroflow.ui.components.RoundedButton;
import com.neuroflow.ui.components.RoundedPanel;
import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class TryPanel extends BasePanel {
    private CustomProgressBar progressBar;
    private JLabel stepLabel;
    private JPanel paperArea;
    private JLabel paperState;
    private JLabel gripperStatus;
    private JLabel resultLabel;
    private RoundedButton startBtn, pauseBtn, resumeBtn, errorDemoBtn;

    private int currentStep = 1;
    private static final int TOTAL_STEPS = 5;
    private boolean listening = false;
    private boolean paused = false;
    private Timer listenTimer;
    private long startMs;

    public TryPanel(MainFrame frame) {
        super(frame, "Your Turn", false, true, MainFrame.WATCH);
        buildUI();
    }

    private void buildUI() {
        ThemeManager tm = ThemeManager.get();

        // Progress row
        JPanel progRow = new JPanel(new BorderLayout(10, 0));
        progRow.setOpaque(false); 
        progRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        
        progressBar = new CustomProgressBar();
        stepLabel = label("1 of " + TOTAL_STEPS, 12, true, tm.sub());
        progRow.add(progressBar, BorderLayout.CENTER);
        progRow.add(stepLabel, BorderLayout.EAST);
        addFull(progRow); 
        addGap(18);

        // Instruction
        addFull(label("Now you try writing the letter", 19, true, tm.tx())); 
        addGap(4);
        addFull(label("Use your gripper on paper", 13, false, tm.sub())); 
        addGap(16);

        // Paper area
        paperArea = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                ThemeManager t = ThemeManager.get();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(t.sf());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));

                // Dashed border
                g2.setColor(listening ? t.ac() : t.bd());
                float[] dash = {8, 6}; 
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dash, 0));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 20, 20));

                // Ghost reference letter
                g2.setFont(new Font("Georgia", Font.BOLD, 56)); 
                g2.setColor(t.alt());
                String ltr = AppState.get().getSelectedLetter();
                FontMetrics fm = g2.getFontMetrics(); 
                int tw = fm.stringWidth(ltr);
                g2.drawString(ltr, getWidth() - tw - 16, 60);
                
                g2.dispose(); 
                super.paintComponent(g);
            }
        };
        paperArea.setOpaque(false); 
        paperArea.setLayout(new BoxLayout(paperArea, BoxLayout.Y_AXIS));
        paperArea.setPreferredSize(new Dimension(460, 175)); 
        paperArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 175));
        paperArea.setBorder(new EmptyBorder(30, 20, 20, 20));

        paperState = label("📝  Press Start, then write on paper", 13, false, tm.sub());
        paperState.setAlignmentX(CENTER_ALIGNMENT); 
        paperArea.add(paperState);
        addFull(paperArea); 
        addGap(12);

        // Gripper row
        RoundedPanel gripRow = new RoundedPanel(12) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(ThemeManager.get().acl()); 
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        gripRow.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        gripRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        gripRow.setOpaque(false);

        JLabel dot2 = new JLabel("●"); 
        dot2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        dot2.setForeground(AppState.get().isGripperConnected() ? tm.ok() : tm.er());
        gripRow.add(dot2);

        gripperStatus = label(AppState.get().isGripperConnected() ? "Gripper connected" : "Gripper disconnected", 12, true, tm.tx());
        gripRow.add(gripperStatus);

        JLabel haptic = label("Haptic: gentle", 11, false, tm.sub()); 
        gripRow.add(haptic);
        addFull(gripRow); 
        addGap(14);

        // Result label
        resultLabel = label("", 13, true, tm.sub());
        resultLabel.setAlignmentX(LEFT_ALIGNMENT); 
        addFull(resultLabel); 
        addGap(4);

        // Buttons
        startBtn = new RoundedButton("▶  Start writing", RoundedButton.Style.PRIMARY);
        startBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        startBtn.addActionListener(e -> startListening());

        pauseBtn = new RoundedButton("⏸  Pause", RoundedButton.Style.MUTED);
        pauseBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); 
        pauseBtn.setVisible(false);
        pauseBtn.addActionListener(e -> pauseListening());

        resumeBtn = new RoundedButton("▶  Continue", RoundedButton.Style.PRIMARY);
        resumeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); 
        resumeBtn.setVisible(false);
        resumeBtn.addActionListener(e -> resumeListening());

        errorDemoBtn = new RoundedButton("⚡  Simulate stroke — demo classify", RoundedButton.Style.GHOST);
        errorDemoBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44)); 
        errorDemoBtn.setVisible(false);
        errorDemoBtn.addActionListener(e -> doClassify());

        addFull(startBtn); 
        addFull(pauseBtn); 
        addFull(resumeBtn); 
        addFull(errorDemoBtn);
    }

    private void startListening() {
        listening = true; 
        paused = false; 
        startMs = System.currentTimeMillis();
        AppState.get().startSession();
        paperArea.repaint();
        
        paperState.setText("🟢  Gripper is tracking your writing...");
        paperState.setForeground(ThemeManager.get().ok());
        
        startBtn.setVisible(false); 
        pauseBtn.setVisible(true); 
        errorDemoBtn.setVisible(true);
        resultLabel.setText(""); 
        resultLabel.setForeground(ThemeManager.get().sub());

        listenTimer = new Timer(2500, e -> { if (!paused) doClassify(); ((Timer) e.getSource()).stop(); });
        listenTimer.setRepeats(false); 
        listenTimer.start();
    }

    private void pauseListening() {
        paused = true; 
        if (listenTimer != null) listenTimer.stop();
        paperState.setText("⏸  Paused"); 
        paperState.setForeground(ThemeManager.get().sub());
        pauseBtn.setVisible(false); 
        resumeBtn.setVisible(true); 
        errorDemoBtn.setVisible(false);
        paperArea.repaint();
    }

    private void resumeListening() {
        paused = false;
        paperState.setText("🟢  Gripper is tracking your writing...");
        paperState.setForeground(ThemeManager.get().ok());
        resumeBtn.setVisible(false); 
        pauseBtn.setVisible(true); 
        errorDemoBtn.setVisible(true);
        
        listenTimer = new Timer(2000, e -> { doClassify(); ((Timer) e.getSource()).stop(); });
        listenTimer.setRepeats(false); 
        listenTimer.start();
        paperArea.repaint();
    }

    private void doClassify() {
        String letter = AppState.get().getSelectedLetter();
        if (letter == null) letter = "b";
        
        double[][] window = MLService.syntheticWindow(letter);
        MLService.ClassifyResult res = MLService.classify(window, letter);
        
        AppState.get().incrementAttempts();
        int dur = (int) ((System.currentTimeMillis() - startMs) / 1000);

        Student st = AppState.get().getCurrentStudent();
        PracticeSession ps = new PracticeSession();
        ps.setStudentId(st != null ? st.getId() : 1);
        ps.setTargetLetter(letter); 
        ps.setDetectedLetter(res.detectedLetter);
        ps.setCorrect(res.isCorrect); 
        ps.setConfidence(res.confidence);
        ps.setAttempts(AppState.get().getSessionAttempts()); 
        ps.setDurationSeconds(dur);
        SessionService.get().save(ps);

        listening = false; 
        paperArea.repaint();
        String src = res.simulated ? " (sim)" : " (ML server)";

        if (res.isCorrect && res.confidence >= 0.75) {
            AppState.get().incrementCorrect();
            resultLabel.setText("✅ Correct! Detected: " + res.detectedLetter + "  conf=" + String.format("%.0f", res.confidence * 100) + "%" + src);
            resultLabel.setForeground(ThemeManager.get().ok());
            advanceStep();
        } else if (res.confidence < 0.60) {
            resultLabel.setText("⚠️ Signal unclear — try again (conf=" + String.format("%.0f", res.confidence * 100) + "%)");
            resultLabel.setForeground(ThemeManager.get().warn());
            resetForRetry();
        } else {
            resultLabel.setText("❌ Detected: " + res.detectedLetter + " — expected " + letter + src);
            resultLabel.setForeground(ThemeManager.get().er());
            if (res.buzz) {
                resultLabel.setText(resultLabel.getText() + "  📳 Haptic feedback sent!");
                Timer buzz = new Timer(500, ev -> frame.showPanel(MainFrame.GUIDE));
                buzz.setRepeats(false); 
                buzz.start();
            } else {
                resetForRetry();
            }
        }
        pauseBtn.setVisible(false); 
        errorDemoBtn.setVisible(false);
        startBtn.setText("▶  Try again"); 
        startBtn.setVisible(true);
    }

    private void advanceStep() {
        currentStep = Math.min(currentStep + 1, TOTAL_STEPS);
        progressBar.setValue((currentStep - 1) * 100 / TOTAL_STEPS);
        stepLabel.setText(currentStep + " of " + TOTAL_STEPS);
        
        if (currentStep >= TOTAL_STEPS) {
            Timer t = new Timer(600, e -> frame.showPanel(MainFrame.WIN)); 
            t.setRepeats(false); 
            t.start();
        }
    }

    private void resetForRetry() {
        paperState.setText("📝  Press Start, then write on paper"); 
        paperState.setForeground(ThemeManager.get().sub());
    }

    @Override
    public void refresh() {
        currentStep = 1; 
        listening = false; 
        paused = false;
        if (listenTimer != null) listenTimer.stop();
        
        progressBar.setValue(0); 
        stepLabel.setText("1 of " + TOTAL_STEPS);
        paperState.setText("📝  Press Start, then write on paper"); 
        paperState.setForeground(ThemeManager.get().sub());
        
        startBtn.setText("▶  Start writing"); 
        startBtn.setVisible(true);
        pauseBtn.setVisible(false); 
        resumeBtn.setVisible(false); 
        errorDemoBtn.setVisible(false);
        resultLabel.setText("");
        
        paperArea.repaint(); 
        contentArea.revalidate(); 
        contentArea.repaint();
    }
}
