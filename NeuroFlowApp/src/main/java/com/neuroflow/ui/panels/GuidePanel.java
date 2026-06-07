package com.neuroflow.ui.panels;

import com.neuroflow.AppState;
import com.neuroflow.ui.MainFrame;
import com.neuroflow.ui.components.RoundedPanel;
import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class GuidePanel extends BasePanel {
    private JLabel wrongLetterLbl, correctLetterLbl;
    private JLabel hintLabel;

    public GuidePanel(MainFrame frame) {
        super(frame, "Let's adjust", false, true, MainFrame.TRY);
        buildUI();
    }

    private void buildUI() {
        ThemeManager tm = ThemeManager.get();
        addFull(label("Your gripper noticed something", 13, false, tm.sub())); 
        addGap(4);
        addFull(label("Try starting here →", 21, true, tm.tx())); 
        addGap(16);

        // Correction card
        RoundedPanel corrCard = new RoundedPanel(18);
        corrCard.setLayout(new BoxLayout(corrCard, BoxLayout.Y_AXIS));
        corrCard.setBorder(new EmptyBorder(16, 20, 16, 20));
        corrCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JPanel pair = new JPanel(new GridLayout(1, 2, 0, 0));
        pair.setOpaque(false); 
        pair.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        // Wrong letter
        JPanel wrongSide = new JPanel(); 
        wrongSide.setOpaque(false);
        wrongSide.setLayout(new BoxLayout(wrongSide, BoxLayout.Y_AXIS));
        
        wrongLetterLbl = new JLabel("d", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                // Strikethrough
                FontMetrics fm = g2.getFontMetrics(getFont());
                int tw = fm.stringWidth(getText()), ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.setColor(ThemeManager.get().er()); 
                g2.setStroke(new BasicStroke(3));
                g2.drawLine((getWidth() - tw) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getHeight() / 2,
                            (getWidth() + tw) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getHeight() / 2);
                g2.dispose(); 
                super.paintComponent(g);
            }
        };
        wrongLetterLbl.setFont(new Font("Georgia", Font.BOLD, 64));
        wrongLetterLbl.setForeground(tm.er()); 
        wrongLetterLbl.setOpaque(false);
        wrongLetterLbl.setAlignmentX(CENTER_ALIGNMENT);
        
        JLabel notLbl = label("Not this way", 11, true, tm.er()); 
        notLbl.setAlignmentX(CENTER_ALIGNMENT);
        wrongSide.add(wrongLetterLbl); 
        wrongSide.add(notLbl);

        // Correct letter
        JPanel rightSide = new JPanel(); 
        rightSide.setOpaque(false);
        rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.Y_AXIS));
        
        correctLetterLbl = new JLabel("b", SwingConstants.CENTER);
        correctLetterLbl.setFont(new Font("Georgia", Font.BOLD, 64));
        correctLetterLbl.setForeground(tm.ok()); 
        correctLetterLbl.setOpaque(false);
        correctLetterLbl.setAlignmentX(CENTER_ALIGNMENT);
        
        JLabel tryLbl = label("Try this ✓", 11, true, tm.ok()); 
        tryLbl.setAlignmentX(CENTER_ALIGNMENT);
        rightSide.add(correctLetterLbl); 
        rightSide.add(tryLbl);

        pair.add(wrongSide); 
        pair.add(rightSide);
        corrCard.add(pair); 
        corrCard.add(Box.createVerticalStrut(12));

        hintLabel = label("The bump goes to the RIGHT →", 13, true, tm.tx());
        hintLabel.setAlignmentX(CENTER_ALIGNMENT);
        JPanel hintPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.get().acl());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12)); 
                g2.dispose();
            }
        };
        hintPanel.setOpaque(false); 
        hintPanel.setBorder(new EmptyBorder(10, 14, 10, 14));
        hintPanel.add(hintLabel);
        
        corrCard.add(hintPanel); 
        addFull(corrCard); 
        addGap(14);

        // Encouragement box
        JPanel helpBox = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                ThemeManager t = ThemeManager.get();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(t.withAlpha(t.ok(), 15));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(t.withAlpha(t.ok(), 55)); 
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 14, 14));
                g2.dispose();
            }
        };
        helpBox.setOpaque(false); 
        helpBox.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
        helpBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        
        JLabel helpTxt = label("💙 No worries — b and d are the trickiest letters! You're doing great just by practising.", 13, false, tm.tx());
        helpTxt.setPreferredSize(new Dimension(380, 40));
        helpBox.add(helpTxt); 
        addFull(helpBox); 
        addGap(16);

        // Buttons
        var tryBtn = new com.neuroflow.ui.components.RoundedButton("Let's try again!", com.neuroflow.ui.components.RoundedButton.Style.SUCCESS);
        tryBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        tryBtn.addActionListener(e -> frame.showPanel(MainFrame.TRY));
        addFull(tryBtn); 
        addGap(10);

        var watchBtn = new com.neuroflow.ui.components.RoundedButton("↩  Watch the letter again", com.neuroflow.ui.components.RoundedButton.Style.GHOST);
        watchBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        watchBtn.addActionListener(e -> frame.showPanel(MainFrame.WATCH));
        addFull(watchBtn);
    }

    @Override
    public void refresh() {
        ThemeManager tm = ThemeManager.get();
        String target = AppState.get().getSelectedLetter();
        if (target == null) target = "b";

        String wrong = switch(target) {
            case "b" -> "d"; case "d" -> "b"; case "p" -> "q"; case "q" -> "p"; default -> "d";
        };
        String hint = switch(target) {
            case "b" -> "The bump goes to the RIGHT →";
            case "d" -> "The bump goes to the LEFT ←";
            case "p" -> "Stem goes DOWN, bump to the RIGHT →";
            case "q" -> "Stem goes DOWN, bump to the LEFT ←";
            default -> "Watch the stroke direction carefully";
        };

        wrongLetterLbl.setText(wrong); 
        correctLetterLbl.setText(target);
        hintLabel.setText(hint);
        
        contentArea.revalidate(); 
        contentArea.repaint();
    }
}
