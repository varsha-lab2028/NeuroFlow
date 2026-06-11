package com.neuroflow.ui;

import javax.swing.*;
import java.awt.*;

public class ScreenUtils {

    private static final Dimension SCREEN =
            Toolkit.getDefaultToolkit().getScreenSize();

    public static final int SCREEN_W = SCREEN.width;
    public static final int SCREEN_H = SCREEN.height;

    // Phone-like initial window size
    public static final int INIT_W = 460;
    public static final int INIT_H = 820;

    // Base design values — everything is designed against these
    private static final int BASE_W = 460;
    private static final int BASE_H = 820;

    private ScreenUtils() {}

    // ── Live window dimensions ────────────────────────────────────

    public static int windowW(Component c) {
        Window w = SwingUtilities.getWindowAncestor(c);
        int val = (w != null) ? w.getWidth() : INIT_W;
        return Math.max(val, BASE_W);
    }

    public static int windowH(Component c) {
        Window w = SwingUtilities.getWindowAncestor(c);
        int val = (w != null) ? w.getHeight() : INIT_H;
        return Math.max(val, BASE_H);
    }

    // ── Scale factor — 1.0 at phone size, grows when maximised ───
    // Clamped to max 2.0 so things don't get absurdly large
    public static float scaleW(Component c) {
        return Math.min(2.0f, (float) windowW(c) / BASE_W);
    }

    public static float scaleH(Component c) {
        return Math.min(2.0f, (float) windowH(c) / BASE_H);
    }

    // Use the smaller of the two scales for most sizing
    // so nothing gets stretched more than the tighter dimension
    public static float scale(Component c) {
        return Math.min(scaleW(c), scaleH(c));
    }

    // ── Content width ─────────────────────────────────────────────
    public static int contentW(Component c) {
        return windowW(c) - pad(c) * 2;
    }

    // ── Fixed base values — scaled up when window grows ───────────

    public static int pad(Component c) {
        return scaled(c, 20);
    }

    public static int gap(Component c) {
        return scaled(c, 14);
    }

    public static int buttonH(Component c) {
        return scaled(c, 50);
    }

    public static int cardPad(Component c) {
        return scaled(c, 14);
    }

    public static int topBarH(Component c) {
        return scaled(c, 52);
    }

    public static int moduleH(Component c) {
        return scaled(c, 82);
    }

    public static int avatarSize(Component c) {
        return scaled(c, 38);
    }

    // ── Font sizing ───────────────────────────────────────────────
    // At phone size: exactly the base size
    // When maximised: grows with scale but never more than 1.5x
    public static int fontSize(Component c, int baseSize) {
        float s = Math.min(1.5f, scale(c));
        return Math.round(baseSize * s);
    }

    // ── Core scaling helper ───────────────────────────────────────
    // Takes a base pixel value designed for 460px wide phone
    // and scales it proportionally when the window is larger
    public static int scaled(Component c, int baseValue) {
        float s = scale(c);
        // Below 1.1 scale (close to phone size) use exact base value
        // so the phone view looks exactly as designed
        if (s < 1.1f) return baseValue;
        return Math.round(baseValue * s);
    }

    public static Window getWindow(Component c) {
        return SwingUtilities.getWindowAncestor(c);
    }
}