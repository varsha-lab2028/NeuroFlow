package com.neuroflow.service;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.util.Random;

public class MLService {
    private static final String SERVER_URL = "http://192.168.1.9:5000";
    private static final int TIMEOUT_MS = 3000;
    private static boolean serverAvailable = false;
    private static boolean checked = false;

    public static class ClassifyResult {
        public final String detectedLetter;
        public final boolean isCorrect;
        public final double confidence;
        public final boolean buzz;
        public final boolean simulated;

        public ClassifyResult(String detected, boolean correct, double conf, boolean buzz, boolean sim) {
            this.detectedLetter = detected;
            this.isCorrect = correct;
            this.confidence = conf;
            this.buzz = buzz;
            this.simulated = sim;
        }
    }

    public static boolean checkServer() {
        try {
            URL url = new URL(SERVER_URL + "/health");
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setConnectTimeout(TIMEOUT_MS);
            c.setReadTimeout(TIMEOUT_MS);
            c.setRequestMethod("GET");
            serverAvailable = (c.getResponseCode() == 200);
        } catch (Exception e) {
            serverAvailable = false;
        }
        checked = true;
        System.out.println("[ML] Server " + (serverAvailable ? "ONLINE" : "OFFLINE (simulation mode)") + " at " + SERVER_URL);
        return serverAvailable;
    }

    public static boolean isServerAvailable() {
        if (!checked) checkServer();
        return serverAvailable;
    }

    public static ClassifyResult classify(double[][] window, String targetLetter) {
        if (isServerAvailable()) return callServer(window, targetLetter);
        return simulate(targetLetter);
    }

    private static ClassifyResult callServer(double[][] window, String targetLetter) {
        try {
            JSONArray arr = new JSONArray();
            for (double[] row : window) {
                JSONArray r = new JSONArray();
                for (double v : row) r.put(v);
                arr.put(r);
            }
            JSONObject body = new JSONObject();
            body.put("window", arr);
            body.put("target_letter", targetLetter);
            
            URL url = new URL(SERVER_URL + "/classify");
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setConnectTimeout(TIMEOUT_MS);
            c.setReadTimeout(TIMEOUT_MS);
            c.setRequestMethod("POST");
            c.setRequestProperty("Content-Type", "application/json");
            c.setDoOutput(true);
            c.getOutputStream().write(body.toString().getBytes("UTF-8"));
            
            if (c.getResponseCode() == 200) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                }
                JSONObject resp = new JSONObject(sb.toString());
                return new ClassifyResult(
                        resp.getString("detected_letter"),
                        resp.getBoolean("is_correct"),
                        resp.getDouble("confidence"),
                        resp.getBoolean("buzz"),
                        false
                );
            }
        } catch (Exception e) {
            serverAvailable = false;
        }
        return simulate(targetLetter);
    }

    private static ClassifyResult simulate(String targetLetter) {
        Random rnd = new Random();
        String detected;
        double confidence;
        if (rnd.nextDouble() < 0.72) {
            detected = targetLetter;
            confidence = 0.88 + rnd.nextDouble() * 0.12;
        } else {
            String[] r = switch (targetLetter) {
                case "b" -> new String[]{"d"};
                case "d" -> new String[]{"b"};
                case "p" -> new String[]{"q"};
                case "q" -> new String[]{"p"};
                default -> new String[]{"b"};
            };
            detected = r[rnd.nextInt(r.length)];
            confidence = 0.78 + rnd.nextDouble() * 0.18;
        }
        boolean correct = detected.equals(targetLetter);
        return new ClassifyResult(detected, correct, confidence, !correct && confidence > 0.60, true);
    }

    public static double[][] syntheticWindow(String letter) {
        double[][] win = new double[100][6];
        Random rnd = new Random();
        double gySign = (letter.equals("b") || letter.equals("p")) ? 1.0 : -1.0;
        for (int t = 0; t < 100; t++) {
            double phase = (double) t / 100.0;
            win[t][0] = 6443 + rnd.nextGaussian() * 200;
            win[t][1] = -3514 + rnd.nextGaussian() * 150;
            win[t][2] = -258 + rnd.nextGaussian() * 80;
            win[t][3] = -24 + rnd.nextGaussian() * 50;
            win[t][4] = gySign * (200 * Math.sin(Math.PI * phase) + rnd.nextGaussian() * 30);
            win[t][5] = 12 + rnd.nextGaussian() * 40;
        }
        return win;
    }
}