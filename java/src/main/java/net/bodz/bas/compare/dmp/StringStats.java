package net.bodz.bas.compare.dmp;

import java.util.regex.Pattern;

public class StringStats {

    /**
     * Given two strings, compute a score representing whether the internal boundary falls on
     * logical boundaries. Scores range from 6 (best) to 0 (worst).
     *
     * @param one
     *            First string.
     * @param two
     *            Second string.
     * @return The score.
     */
    public static int diff_cleanupSemanticScore(String one, String two) {
        if (one.length() == 0 || two.length() == 0) {
            // Edges are the best.
            return 6;
        }

        // Each port of this function behaves slightly differently due to
        // subtle differences in each language's definition of things like
        // 'whitespace'. Since this function's purpose is largely cosmetic,
        // the choice has been made to use each language's native features
        // rather than force total conformity.
        char char1 = one.charAt(one.length() - 1);
        char char2 = two.charAt(0);
        boolean nonAlphaNumeric1 = !Character.isLetterOrDigit(char1);
        boolean nonAlphaNumeric2 = !Character.isLetterOrDigit(char2);
        boolean whitespace1 = nonAlphaNumeric1 && Character.isWhitespace(char1);
        boolean whitespace2 = nonAlphaNumeric2 && Character.isWhitespace(char2);
        boolean lineBreak1 = whitespace1 && Character.getType(char1) == Character.CONTROL;
        boolean lineBreak2 = whitespace2 && Character.getType(char2) == Character.CONTROL;
        boolean blankLine1 = lineBreak1 && BLANKLINEEND.matcher(one).find();
        boolean blankLine2 = lineBreak2 && BLANKLINESTART.matcher(two).find();

        if (blankLine1 || blankLine2) {
            // Five points for blank lines.
            return 5;
        } else if (lineBreak1 || lineBreak2) {
            // Four points for line breaks.
            return 4;
        } else if (nonAlphaNumeric1 && !whitespace1 && whitespace2) {
            // Three points for end of sentences.
            return 3;
        } else if (whitespace1 || whitespace2) {
            // Two points for whitespace.
            return 2;
        } else if (nonAlphaNumeric1 || nonAlphaNumeric2) {
            // One point for non-alphanumeric.
            return 1;
        }
        return 0;
    }

    // Define some regex patterns for matching boundaries.
    private static Pattern BLANKLINEEND = Pattern.compile("\\n\\r?\\n\\Z", Pattern.DOTALL);
    private static Pattern BLANKLINESTART = Pattern.compile("\\A\\r?\\n\\r?\\n", Pattern.DOTALL);

}
