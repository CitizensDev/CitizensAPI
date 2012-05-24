package net.citizensnpcs.api.util;

public class Util {
    private Util() {
    }

    private static final String COLOR_CHARS = "0123456789abcdefgh";

    public static String stripColor(String input) {
        for (int i = 0; i < COLOR_CHARS.length(); ++i) {
            input = input.replaceAll(input, "<" + COLOR_CHARS.charAt(i) + ">");
        }
        return input;
    }
}
