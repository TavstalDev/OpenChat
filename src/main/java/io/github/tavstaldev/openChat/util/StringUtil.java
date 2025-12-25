package io.github.tavstaldev.openChat.util;

import io.github.tavstaldev.openChat.Patterns;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for string-related operations.
 */
public class StringUtil {
    // Instance of JaroWinklerSimilarity for calculating string similarity.
    private static final JaroWinklerSimilarity jaroWinkler = new JaroWinklerSimilarity();

    /**
     * Calculates the similarity between two strings using the Jaro-Winkler algorithm.
     *
     * @param str1 the first string to compare
     * @param str2 the second string to compare
     * @return a Double value representing the similarity score between 0.0 and 1.0,
     *         where 1.0 indicates identical strings
     */
    public static Double similarity(String str1, String str2) {
        return jaroWinkler.apply(str1, str2);
    }

    /**
     * Validates whether the given string is a valid hexadecimal color code.
     * <br>
     * A valid hexadecimal color code starts with a '#' followed by either
     * 3 or 6 hexadecimal characters (0-9, a-f, A-F).
     *
     * @param color the string to validate as a hexadecimal color code
     * @return true if the string matches the hexadecimal color pattern, false otherwise
     */
    public static boolean isValidHexColor(@NotNull String color) {
        return Patterns.hexColorPattern.matcher(color).matches();
    }
}