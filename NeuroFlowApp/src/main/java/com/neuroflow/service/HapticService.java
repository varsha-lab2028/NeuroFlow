package com.neuroflow.service;

public class HapticService {

    private static HapticService instance;
    private boolean connected = false;

    private HapticService() {}

    public static HapticService get() {
        if (instance == null) instance = new HapticService();
        return instance;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void triggerGentleBuzz() {
        if (!connected) {
            System.out.println("[HAPTIC] Would trigger gentle buzz — gripper not connected");
            return;
        }
        System.out.println("[HAPTIC] Gentle buzz triggered");
        // TODO: send buzz command to Arduino hardware
    }

    public void triggerCorrectionBuzz() {
        if (!connected) {
            System.out.println("[HAPTIC] Would trigger correction buzz — gripper not connected");
            return;
        }
        System.out.println("[HAPTIC] Correction buzz triggered");
        // TODO: send correction buzz command to Arduino hardware
    }

    public void stopBuzz() {
        if (!connected) {
            System.out.println("[HAPTIC] Would stop buzz — gripper not connected");
            return;
        }
        System.out.println("[HAPTIC] Buzz stopped");
        // TODO: send stop command to Arduino hardware
    }
}
