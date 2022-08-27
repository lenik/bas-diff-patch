package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.Nullables;
import net.bodz.bas.text.row.IRow;
import net.bodz.bas.text.row.Rows;

public class RowUtils {

    /**
     * Determine the common prefix of two strings
     *
     * @param text1
     *            First string.
     * @param text2
     *            Second string.
     * @return The number of characters common to the start of each string.
     */
    public static <cell_t> int commonPrefix(IRow<? extends cell_t> text1, IRow<? extends cell_t> text2) {
        // Performance analysis: https://neil.fraser.name/news/2007/10/09/
        int n = Math.min(text1.length(), text2.length());
        for (int i = 0; i < n; i++) {
            if (Nullables.notEquals(text1.cellAt(i), text2.cellAt(i))) {
                return i;
            }
        }
        return n;
    }

    /**
     * Determine the common suffix of two strings
     *
     * @param text1
     *            First string.
     * @param text2
     *            Second string.
     * @return The number of characters common to the end of each string.
     */
    public static <cell_t> int commonSuffix(IRow<? extends cell_t> text1, IRow<? extends cell_t> text2) {
        // Performance analysis: https://neil.fraser.name/news/2007/10/09/
        int text1_length = text1.length();
        int text2_length = text2.length();
        int n = Math.min(text1_length, text2_length);
        for (int i = 1; i <= n; i++) {
            if (Nullables.notEquals(text1.cellAt(text1_length - i), text2.cellAt(text2_length - i))) {
                return i - 1;
            }
        }
        return n;
    }

    /**
     * Determine if the suffix of one string is the prefix of another.
     *
     * @param text1
     *            First string.
     * @param text2
     *            Second string.
     * @return The number of characters common to the end of the first string and the start of the
     *         second string.
     */
    static <cell_t> int commonOverlap(IRow<cell_t> text1, IRow<cell_t> text2) {
        // Cache the text lengths to prevent multiple calls.
        int text1_length = text1.length();
        int text2_length = text2.length();
        // Eliminate the null case.
        if (text1_length == 0 || text2_length == 0) {
            return 0;
        }
        // Truncate the longer string.
        if (text1_length > text2_length) {
            text1 = text1.slice(text1_length - text2_length);
        } else if (text1_length < text2_length) {
            text2 = text2.slice(0, text1_length);
        }
        int text_length = Math.min(text1_length, text2_length);
        // Quick check for the worst case.
        if (text1.equals(text2)) {
            return text_length;
        }

        // Start by looking for a single character match
        // and increase length until no match is found.
        // Performance analysis: https://neil.fraser.name/news/2010/11/04/
        int best = 0;
        int length = 1;
        while (true) {
            IRow<? extends cell_t> pattern = text1.slice(text_length - length);
            int found = text2.indexOf(pattern);
            if (found == -1) {
                return best;
            }
            length += found;
            if (found == 0 || text1.slice(text_length - length).equals(text2.slice(0, length))) {
                best = length;
                length++;
            }
        }
    }

    /**
     * Do the two texts share a substring which is at least half the length of the longer text? This
     * speedup can produce non-minimal diffs.
     *
     * @param text1
     *            First string.
     * @param text2
     *            Second string.
     * @return Five element String array, containing the prefix of text1, the suffix of text1, the
     *         prefix of text2, the suffix of text2 and the common middle. Or null if there was no
     *         match.
     */
    public static <cell_t> HalfMatch<cell_t> halfMatch(Config config, IRow<cell_t> text1, IRow<cell_t> text2) {
        if (config.Diff_Timeout <= 0) {
            // Don't risk returning a non-optimal diff if we have unlimited time.
            return null;
        }
        IRow<cell_t> longtext = text1.length() > text2.length() ? text1 : text2;
        IRow<cell_t> shorttext = text1.length() > text2.length() ? text2 : text1;
        if (longtext.length() < 4 || shorttext.length() * 2 < longtext.length()) {
            return null; // Pointless.
        }

        // First check if the second quarter is the seed for a half-match.
        HalfMatch<cell_t> hm1 = halfMatchI(longtext, shorttext, (longtext.length() + 3) / 4);
        // Check again based on the third quarter.
        HalfMatch<cell_t> hm2 = halfMatchI(longtext, shorttext, (longtext.length() + 1) / 2);
        HalfMatch<cell_t> hm;
        if (hm1 == null && hm2 == null) {
            return null;
        } else if (hm2 == null) {
            hm = hm1;
        } else if (hm1 == null) {
            hm = hm2;
        } else {
            // Both matched. Select the longest.
            hm = hm1.common.length() > hm2.common.length() ? hm1 : hm2;
        }

        // A half-match was found, sort out the return data.
        if (text1.length() > text2.length()) {
            return hm;
            // return new String[]{hm[0], hm[1], hm[2], hm[3], hm[4]};
        } else {
            return new HalfMatch<cell_t>(hm.prefix2, hm.suffix2, hm.prefix1, hm.suffix1, hm.common);
        }
    }

    /**
     * Does a substring of shorttext exist within longtext such that the substring is at least half
     * the length of longtext?
     *
     * @param longtext
     *            Longer string.
     * @param shorttext
     *            Shorter string.
     * @param i
     *            Start index of quarter length substring within longtext.
     * @return Five element String array, containing the prefix of longtext, the suffix of longtext,
     *         the prefix of shorttext, the suffix of shorttext and the common middle. Or null if
     *         there was no match.
     */
    private static <cell_t> HalfMatch<cell_t> halfMatchI(IRow<cell_t> longtext, IRow<cell_t> shorttext, int i) {
        // Start with a 1/4 length substring at position i as a seed.
        IRow<cell_t> seed = longtext.slice(i, i + longtext.length() / 4);
        int j = -1;
        IRow<cell_t> best_common = Rows.empty();
        IRow<cell_t> best_longtext_a = Rows.empty(), best_longtext_b = Rows.empty();
        IRow<cell_t> best_shorttext_a = Rows.empty(), best_shorttext_b = Rows.empty();
        while ((j = shorttext.indexOf(seed, j + 1)) != -1) {
            int prefixLength = commonPrefix(longtext.slice(i, longtext.length()),
                    shorttext.slice(j, shorttext.length()));
            int suffixLength = commonSuffix(longtext.slice(0, i), shorttext.slice(0, j));
            if (best_common.length() < suffixLength + prefixLength) {
                best_common = shorttext.slice(j - suffixLength, j).concat(shorttext.slice(j, j + prefixLength));
                best_longtext_a = longtext.slice(0, i - suffixLength);
                best_longtext_b = longtext.slice(i + prefixLength, longtext.length());
                best_shorttext_a = shorttext.slice(0, j - suffixLength);
                best_shorttext_b = shorttext.slice(j + prefixLength, shorttext.length());
            }
        }
        if (best_common.length() * 2 >= longtext.length()) {
            return new HalfMatch<cell_t>(best_longtext_a, best_longtext_b, best_shorttext_a, best_shorttext_b,
                    best_common);
        } else {
            return null;
        }
    }

}
