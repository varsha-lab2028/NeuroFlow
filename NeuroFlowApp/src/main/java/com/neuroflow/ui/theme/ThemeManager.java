package com.neuroflow.ui.theme;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Central theme manager. Holds all colours and fonts used across the app.
 * Components register as ThemeListeners to repaint when the theme changes.
 */
public class ThemeManager {

    public interface ThemeListener {
        void onThemeChanged();
    }

    // ── Singleton ─────────────────────────────────────────────────
    private static ThemeManager instance;

    public static ThemeManager get() {
        if (instance == null) instance = new ThemeManager();
        return instance;
    }

    // ── Listeners ─────────────────────────────────────────────────
    private final List<ThemeListener> listeners = new ArrayList<>();

    public void addListener(ThemeListener l) {
        if (!listeners.contains(l)) listeners.add(l);
    }

    public void removeListener(ThemeListener l) {
        listeners.remove(l);
    }

    private void notifyListeners() {
        for (ThemeListener l : listeners) l.onThemeChanged();
    }

    // ── Theme presets ─────────────────────────────────────────────
    public enum Preset { SOFT_CREAM, BLUE_MIST, WARM_PAPER, NIGHT }

    private Preset currentPreset = Preset.SOFT_CREAM;

    public Preset getPreset() { return currentPreset; }

    public void setPreset(Preset preset) {
        this.currentPreset = preset;
        notifyListeners();
    }

    // ── Colour tokens (change with preset) ────────────────────────

    public Color bg() {
        return switch (currentPreset) {
            case SOFT_CREAM  -> new Color(0xF6F1E8);
            case BLUE_MIST   -> new Color(0xEEF4FA);
            case WARM_PAPER  -> new Color(0xF7EFE3);
            case NIGHT       -> new Color(0x1E1E1E);
        };
    }

    public Color sf() {
        return switch (currentPreset) {
            case SOFT_CREAM  -> new Color(0xFFFFFF);
            case BLUE_MIST   -> new Color(0xFFFFFF);
            case WARM_PAPER  -> new Color(0xFFF8EF);
            case NIGHT       -> new Color(0x2A2A2A);
        };
    }

    public Color alt() {
        return switch (currentPreset) {
            case SOFT_CREAM  -> new Color(0xEDE8DC);
            case BLUE_MIST   -> new Color(0xE0EAF5);
            case WARM_PAPER  -> new Color(0xEEE0CE);
            case NIGHT       -> new Color(0x383838);
        };
    }

    public Color tx() {
        return switch (currentPreset) {
            case SOFT_CREAM  -> new Color(0x1E1E1E);
            case BLUE_MIST   -> new Color(0x1B1F24);
            case WARM_PAPER  -> new Color(0x202020);
            case NIGHT       -> new Color(0xE6E6E6);
        };
    }

    public Color sub() {
        return switch (currentPreset) {
            case SOFT_CREAM  -> new Color(0x666666);
            case BLUE_MIST   -> new Color(0x4A5568);
            case WARM_PAPER  -> new Color(0x6B4E3D);
            case NIGHT       -> new Color(0xA8A8A8);
        };
    }

    public Color ac() {
        return switch (currentPreset) {
            case SOFT_CREAM  -> new Color(0x4E8FC5);
            case BLUE_MIST   -> new Color(0x5B8FB9);
            case WARM_PAPER  -> new Color(0xC98F6B);
            case NIGHT       -> new Color(0x7BA8F0);
        };
    }

    public Color acl() {
        return switch (currentPreset) {
            case SOFT_CREAM  -> new Color(0xD4E9F8);
            case BLUE_MIST   -> new Color(0xC4DAF0);
            case WARM_PAPER  -> new Color(0xF0D9C8);
            case NIGHT       -> new Color(0x1A2D4C);
        };
    }

    public Color bd() {
        return switch (currentPreset) {
            case NIGHT -> new Color(255, 255, 255, 25);
            default    -> new Color(0, 0, 0, 23);
        };
    }

    // ── Status colours (fixed across all themes) ──────────────────

    public Color ok()   {
        return currentPreset == Preset.NIGHT
                ? new Color(0x4DBB79) : new Color(0x3A9462);
    }

    public Color er()   {
        return currentPreset == Preset.NIGHT
                ? new Color(0xF07550) : new Color(0xC85C3A);
    }

    public Color warn() {
        return currentPreset == Preset.NIGHT
                ? new Color(0xE09040) : new Color(0xD68B25);
    }

    // ── Font size scaling ─────────────────────────────────────────
    private float fontScale = 1.0f;  // 1.0 = medium, 0.85 = small, 1.25 = large

    public void setFontScale(float scale) {
        this.fontScale = scale;
        notifyListeners();
    }

    public float getFontScale() { return fontScale; }

    // ── Font helpers ──────────────────────────────────────────────

    public Font bold(int size) {
        return new Font("Segoe UI", Font.BOLD, scaled(size));
    }

    public Font regular(int size) {
        return new Font("Segoe UI", Font.PLAIN, scaled(size));
    }

    public Font serif(int size) {
        return new Font("Georgia", Font.BOLD, scaled(size));
    }

    private int scaled(int size) {
        return Math.round(size * fontScale);
    }

    // ── Utility ───────────────────────────────────────────────────

    /**
     * Returns a copy of the colour with a specific alpha (0–255).
     */
    public Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    // ── Reading mode ──────────────────────────────────────────────
    public enum ReadingMode { DEFAULT, DYSLEXIA, ADHD_FOCUS, SENSORY, LOW_VISION }

    private ReadingMode readingMode = ReadingMode.DEFAULT;

    public ReadingMode getReadingMode() { return readingMode; }

    public void setReadingMode(ReadingMode mode) {
        this.readingMode = mode;
        // Apply preset + font scale based on mode
        switch (mode) {
            case DEFAULT       -> { setPreset(Preset.SOFT_CREAM); setFontScale(1.0f); }
            case DYSLEXIA      -> { setPreset(Preset.WARM_PAPER); setFontScale(1.1f); }
            case ADHD_FOCUS    -> { setPreset(Preset.BLUE_MIST);  setFontScale(1.0f); }
            case SENSORY       -> { setPreset(Preset.NIGHT);       setFontScale(1.0f); }
            case LOW_VISION    -> { setPreset(Preset.SOFT_CREAM);  setFontScale(1.3f); }
        }
        notifyListeners();
    }

    // ── Haptic intensity ──────────────────────────────────────────
    public enum HapticLevel { LOW, MEDIUM }

    private HapticLevel hapticLevel = HapticLevel.MEDIUM;

    public HapticLevel getHapticLevel() { return hapticLevel; }
    public void setHapticLevel(HapticLevel level) { this.hapticLevel = level; }

    // ── Motion + sound toggles ────────────────────────────────────
    private boolean motionEnabled = true;
    private boolean soundEnabled  = true;

    public boolean isMotionEnabled() { return motionEnabled; }
    public void setMotionEnabled(boolean v) { motionEnabled = v; }

    public boolean isSoundEnabled() { return soundEnabled; }
    public void setSoundEnabled(boolean v) { soundEnabled = v; }
}