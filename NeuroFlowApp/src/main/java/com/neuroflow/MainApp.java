package com.neuroflow;

import com.neuroflow.ui.MainFrame;
import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}
