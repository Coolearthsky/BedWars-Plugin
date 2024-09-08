package me.coolearth.coolearth.global;

public class GlobalVariables {
    private static boolean isOn = false;

    public static void gameStarted() {
        isOn = true;
    }
    public static void gameEnded() {
        isOn = false;
    }
    public static boolean isGameActive() {
        return isOn;
    }
}
