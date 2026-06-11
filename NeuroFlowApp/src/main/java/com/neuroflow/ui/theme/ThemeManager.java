package com.neuroflow.ui.theme;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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

    // ── OS detection — done once at startup ───────────────────────
    private static final boolean IS_MAC = System.getProperty("os.name")
            .toLowerCase().contains("mac");

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

    // ── Colour tokens ─────────────────────────────────────────────

    public Color bg() {
        return switch (currentPreset) {
            case SOFT_CREAM -> new Color(0xF6F1E8);
            case BLUE_MIST  -> new Color(0xEEF4FA);
            case WARM_PAPER -> new Color(0xF7EFE3);
            case NIGHT      -> new Color(0x1E1E1E);
        };
    }

    public Color sf() {
        return switch (currentPreset) {
            case SOFT_CREAM -> new Color(0xFFFFFF);
            case BLUE_MIST  -> new Color(0xFFFFFF);
            case WARM_PAPER -> new Color(0xFFF8EF);
            case NIGHT      -> new Color(0x2A2A2A);
        };
    }

    public Color alt() {
        return switch (currentPreset) {
            case SOFT_CREAM -> new Color(0xEDE8DC);
            case BLUE_MIST  -> new Color(0xE0EAF5);
            case WARM_PAPER -> new Color(0xEEE0CE);
            case NIGHT      -> new Color(0x383838);
        };
    }

    public Color tx() {
        return switch (currentPreset) {
            case SOFT_CREAM -> new Color(0x1E1E1E);
            case BLUE_MIST  -> new Color(0x1B1F24);
            case WARM_PAPER -> new Color(0x202020);
            case NIGHT      -> new Color(0xE6E6E6);
        };
    }

    public Color sub() {
        return switch (currentPreset) {
            case SOFT_CREAM -> new Color(0x666666);
            case BLUE_MIST  -> new Color(0x4A5568);
            case WARM_PAPER -> new Color(0x6B4E3D);
            case NIGHT      -> new Color(0xA8A8A8);
        };
    }

    public Color ac() {
        return switch (currentPreset) {
            case SOFT_CREAM -> new Color(0x4E8FC5);
            case BLUE_MIST  -> new Color(0x5B8FB9);
            case WARM_PAPER -> new Color(0xC98F6B);
            case NIGHT      -> new Color(0x7BA8F0);
        };
    }

    public Color acl() {
        return switch (currentPreset) {
            case SOFT_CREAM -> new Color(0xD4E9F8);
            case BLUE_MIST  -> new Color(0xC4DAF0);
            case WARM_PAPER -> new Color(0xF0D9C8);
            case NIGHT      -> new Color(0x1A2D4C);
        };
    }

    public Color bd() {
        return currentPreset == Preset.NIGHT
                ? new Color(255, 255, 255, 25)
                : new Color(0, 0, 0, 23);
    }

    // ── Status colours ────────────────────────────────────────────

    public Color ok() {
        return currentPreset == Preset.NIGHT
                ? new Color(0x4DBB79) : new Color(0x3A9462);
    }

    public Color er() {
        return currentPreset == Preset.NIGHT
                ? new Color(0xF07550) : new Color(0xC85C3A);
    }

    public Color warn() {
        return currentPreset == Preset.NIGHT
                ? new Color(0xE09040) : new Color(0xD68B25);
    }

    // ── Font scaling ──────────────────────────────────────────────
    private float fontScale = 1.0f;

    public void setFontScale(float scale) {
        this.fontScale = scale;
        notifyListeners();
    }

    public float getFontScale() { return fontScale; }

    // ── Font helpers ──────────────────────────────────────────────
    // On Mac, "Segoe UI" does not exist — fall back to system fonts
    // that look equally clean on macOS (SF Pro via Dialog, or Helvetica Neue)

    private String uiFont() {
        return IS_MAC ? "Helvetica Neue" : "Segoe UI";
    }

    public Font bold(int size) {
        return new Font(uiFont(), Font.BOLD, scaledSize(size));
    }

    public Font regular(int size) {
        return new Font(uiFont(), Font.PLAIN, scaledSize(size));
    }

    public Font serif(int size) {
        return new Font("Georgia", Font.BOLD, scaledSize(size));
    }

    private int scaledSize(int base) {
        return Math.round(base * fontScale);
    }

    // ── Emoji font — cross-platform ───────────────────────────────
    /**
     * Returns the correct emoji font for the current OS.
     * "Apple Color Emoji" on Mac, "Segoe UI Emoji" on Windows.
     * Use this everywhere an emoji needs to be drawn via Graphics2D.
     */
    public static Font emojiFont(int size) {
        return IS_MAC
                ? new Font("Apple Color Emoji", Font.PLAIN, size)
                : new Font("Segoe UI Emoji",    Font.PLAIN, size);
    }

    // ── Utility ───────────────────────────────────────────────────

    public Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    // ── Reading mode ──────────────────────────────────────────────
    public enum ReadingMode {
        DEFAULT, DYSLEXIA, ADHD_FOCUS, SENSORY, LOW_VISION
    }

    private ReadingMode readingMode = ReadingMode.DEFAULT;

    public ReadingMode getReadingMode() { return readingMode; }

    public void setReadingMode(ReadingMode mode) {
        this.readingMode = mode;
        switch (mode) {
            case DEFAULT    -> { setPreset(Preset.SOFT_CREAM); setFontScale(1.0f); }
            case DYSLEXIA   -> { setPreset(Preset.WARM_PAPER); setFontScale(1.1f); }
            case ADHD_FOCUS -> { setPreset(Preset.BLUE_MIST);  setFontScale(1.0f); }
            case SENSORY    -> { setPreset(Preset.NIGHT);       setFontScale(1.0f); }
            case LOW_VISION -> { setPreset(Preset.SOFT_CREAM);  setFontScale(1.3f); }
        }
        notifyListeners();
    }

    // ── Haptic intensity ──────────────────────────────────────────
    public enum HapticLevel { LOW, MEDIUM }

    private HapticLevel hapticLevel = HapticLevel.MEDIUM;

    public HapticLevel getHapticLevel()              { return hapticLevel; }
    public void setHapticLevel(HapticLevel level)    { this.hapticLevel = level; }

    // ── Motion + sound toggles ────────────────────────────────────
    private boolean motionEnabled = true;
    private boolean soundEnabled  = true;

    public boolean isMotionEnabled()             { return motionEnabled; }
    public void setMotionEnabled(boolean v)      { motionEnabled = v; }

    public boolean isSoundEnabled()              { return soundEnabled; }
    public void setSoundEnabled(boolean v)       { soundEnabled = v; }
}