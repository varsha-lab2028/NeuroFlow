package com.neuroflow.ui.panels;

import com.neuroflow.AppState;
import com.neuroflow.ui.MainFrame;
import com.neuroflow.ui.components.AnimatedLetterPanel;
import com.neuroflow.ui.components.RoundedButton;
import com.neuroflow.ui.components.RoundedPanel;
import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class WatchPanel extends BasePanel {
    private AnimatedLetterPanel animPanel;
    private JLabel statusLabel;
    private JLabel[] stepDots;
    private RoundedButton playBtn, nextBtn, replayBtn;
    private JLabel letterTitle;

    private static final String[][] STEPS = {
        {"b","Start at the top — place your pencil high up", "Draw a straight line all the way down", "Add a round bump to the RIGHT side"},
        {"d","Start on the right — your pencil starts high on the right", "Draw the bowl first — curve round to the left", "Add a straight line going DOWN on the RIGHT"},
        {"p","Start mid-height — place your pencil in the middle", "Draw a straight line going DOWN past the line", "Add a round bump to the RIGHT above the line"},
        {"q","Start mid-height on the right side", "Draw the bowl — curve round to the LEFT", "Add a straight line going DOWN on the LEFT side"},
    };

    public WatchPanel(MainFrame frame) {
        super(frame, "Watch & Learn", false, true, MainFrame.HOME);
        buildUI();
    }

    private void buildUI() {
        ThemeManager tm = ThemeManager.get();
        addFull(label("Watch how to write the letter", 13, false, tm.sub())); 
        addGap(12);

        // Animation card
        RoundedPanel animCard = new RoundedPanel(18);
        animCard.setLayout(new BoxLayout(animCard, BoxLayout.Y_AXIS));
        animCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        animCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        animPanel = new AnimatedLetterPanel();
        animPanel.setAlignmentX(CENTER_ALIGNMENT);
        animPanel.setOnComplete(() -> {
            playBtn.setVisible(false);
            nextBtn.setVisible(true); 
            replayBtn.setVisible(true);
            stepDots[2].setForeground(tm.ac());
        });
        animCard.add(animPanel);

        // Step dots
        JPanel dotsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        dotsPanel.setOpaque(false);
        stepDots = new JLabel[3];
        for (int i = 0; i < 3; i++) {
            stepDots[i] = new JLabel("●");
            stepDots[i].setFont(new Font("Segoe UI", Font.PLAIN, 12));
            stepDots[i].setForeground(tm.alt());
            dotsPanel.add(stepDots[i]);
        }
        animCard.add(dotsPanel);

        statusLabel = new JLabel("Press play to see how", SwingConstants.CENTER);
        statusLabel.setFont(tm.bold(12)); 
        statusLabel.setForeground(tm.sub());
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);
        animCard.add(statusLabel);
        
        addFull(animCard); 
        addGap(12);

        // Step instructions card
        RoundedPanel stepsCard = new RoundedPanel(18);
        stepsCard.setLayout(new BoxLayout(stepsCard, BoxLayout.Y_AXIS));
        stepsCard.setBorder(new EmptyBorder(14, 16, 14, 16));
        stepsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        letterTitle = label("How to write \"b\"", 14, true, tm.tx());
        letterTitle.setAlignmentX(LEFT_ALIGNMENT);
        stepsCard.add(letterTitle); 
        stepsCard.add(Box.createVerticalStrut(10));
        stepsCard.setName("stepsCard");
        
        addFull(stepsCard); 
        addGap(12);

        // Buttons
        playBtn = new RoundedButton("▶  Watch the animation", RoundedButton.Style.PRIMARY);
        playBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        playBtn.addActionListener(e -> {
            animPanel.play();
            statusLabel.setText("Drawing stroke 1...");
            statusLabel.setForeground(tm.ac());
            stepDots[0].setForeground(tm.ac());
        });

        nextBtn = new RoundedButton("✏️  Now I'll try it!", RoundedButton.Style.SUCCESS);
        nextBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        nextBtn.setVisible(false);
        nextBtn.addActionListener(e -> frame.showPanel(MainFrame.TRY));

        replayBtn = new RoundedButton("↩  Watch again", RoundedButton.Style.GHOST);
        replayBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        replayBtn.setVisible(false);
        replayBtn.addActionListener(e -> {
            animPanel.reset();
            for (JLabel d : stepDots) d.setForeground(tm.alt());
            statusLabel.setText("Press play to see how");
            statusLabel.setForeground(tm.sub());
            nextBtn.setVisible(false); 
            replayBtn.setVisible(false); 
            playBtn.setVisible(true);
        });

        addFull(playBtn); 
        addFull(nextBtn); 
        addFull(replayBtn); 
        addGap(8);

        // Sound button
        JButton soundBtn = new JButton("🔊 Hear the letter sound");
        soundBtn.setFont(tm.bold(13)); 
        soundBtn.setForeground(tm.ac());
        soundBtn.setOpaque(false); 
        soundBtn.setContentAreaFilled(false); 
        soundBtn.setBorderPainted(false);
        soundBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        soundBtn.setAlignmentX(CENTER_ALIGNMENT);
        soundBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "🔊 Playing phoneme for \"" + AppState.get().getSelectedLetter() + "\"\n(Sound playback requires device audio)",
            "Letter Sound", JOptionPane.INFORMATION_MESSAGE));
        
        addFull(soundBtn);
    }

    @Override
    public void refresh() {
        String letter = AppState.get().getSelectedLetter();
        if (letter == null) letter = "b";
        
        animPanel.reset();
        animPanel.setLetter(letter);
        ThemeManager tm = ThemeManager.get();
        for (JLabel d : stepDots) d.setForeground(tm.alt());
        
        statusLabel.setText("Press play to see how"); 
        statusLabel.setForeground(tm.sub());
        playBtn.setVisible(true); 
        nextBtn.setVisible(false); 
        replayBtn.setVisible(false);
        letterTitle.setText("How to write \"" + letter + "\"");
        
        contentArea.revalidate(); 
        contentArea.repaint();
    }

    private String[][] getSteps(String letter) {
        for (String[] s : STEPS) if (s[0].equals(letter)) return new String[][]{{s[1]}, {s[2]}, {s[3]}};
        return new String[][]{{"Step 1"}, {"Step 2"}, {"Step 3"}};
    }
}
