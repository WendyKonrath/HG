package org.hg.utils;


public class StringUtils {

    public static boolean isInteger(String s) {
        return isInteger(s, 10);
    }

    public static boolean isInteger(String s, int radix) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) return false;
                else continue;
            }
            if (Character.digit(s.charAt(i), radix) < 0) return false;
        }
        return true;
    }

    // ===========================
    //   Formatação de Tempo
    // ===========================
    public static String calculateTime(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;

        if (m == 0) return s + " segundos";
        if (s == 0) return m + " minutos";

        return m + " minutos e " + s + " segundos";
    }

    public static String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return String.format("%d:%02d", minutes, seconds);
    }

}
