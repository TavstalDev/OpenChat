package io.github.tavstaldev.openChat.util;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;

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
}