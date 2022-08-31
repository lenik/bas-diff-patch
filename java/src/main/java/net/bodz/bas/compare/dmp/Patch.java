package net.bodz.bas.compare.dmp;

import net.bodz.bas.text.row.IRow;

/**
 * Class representing one patch operation.
 */
public class Patch<cell_t> {

    Config config;
    DMPRowComparator<cell_t> diff;

    public EditList<cell_t> diffs;
    public int start1;
    public int start2;
    public int length1;
    public int length2;

    /**
     * Constructor. Initializes with an empty list of diffs.
     */
    public Patch(DMPRowComparator<cell_t> diff) {
        this.config = diff.config;
        this.diff = diff;
        this.diffs = new EditList<cell_t>(diff);
    }

    /**
     * Increase the context until it is unique, but don't let the pattern expand beyond
     * Match_MaxBits.
     *
     * @param patch
     *            The patch to grow.
     * @param text
     *            Source text.
     */
    public void addContext(IRow<cell_t> text) {
        if (text.length() == 0) {
            return;
        }
        IRow<cell_t> pattern = text.slice(this.start2, this.start2 + this.length1);
        int padding = 0;

        // Look for the first and last matches of pattern in text. If two different
        // matches are found, increase the pattern length.
        while (true) {
            int p1 = text.indexOf(pattern);
            int p2 = text.lastIndexOf(pattern);
            if (p1 == p2)
                break;
            if (pattern.length() >= config.Match_MaxBits - config.Patch_Margin - config.Patch_Margin)
                break;
            padding += config.Patch_Margin;
            int start = Math.max(0, this.start2 - padding);
            int end = Math.min(text.length(), this.start2 + this.length1 + padding);
            pattern = text.slice(start, end);
        }
        // Add one chunk for good luck.
        padding += config.Patch_Margin;

        // Add the prefix.
        IRow<cell_t> prefix = text.slice(Math.max(0, this.start2 - padding), this.start2);
        if (prefix.length() != 0) {
            this.diffs.prepend(new RowDifference<cell_t>(DifferenceType.MATCH, prefix));
        }
        // Add the suffix.
        IRow<cell_t> suffix = text.slice(this.start2 + this.length1,
                Math.min(text.length(), this.start2 + this.length1 + padding));
        if (suffix.length() != 0) {
            this.diffs.append(new RowDifference<cell_t>(DifferenceType.MATCH, suffix));
        }

        // Roll back the start points.
        this.start1 -= prefix.length();
        this.start2 -= prefix.length();
        // Extend the lengths.
        this.length1 += prefix.length() + suffix.length();
        this.length2 += prefix.length() + suffix.length();
    }

    /**
     * Emulate GNU diff's format. Header: @@ -382,8 +481,9 @@ Indices are printed as 1-based, not
     * 0-based.
     *
     * @return The GNU diff string.
     */
    @Override
    public String toString() {
        String coords1, coords2;
        if (this.length1 == 0) {
            coords1 = this.start1 + ",0";
        } else if (this.length1 == 1) {
            coords1 = Integer.toString(this.start1 + 1);
        } else {
            coords1 = (this.start1 + 1) + "," + this.length1;
        }
        if (this.length2 == 0) {
            coords2 = this.start2 + ",0";
        } else if (this.length2 == 1) {
            coords2 = Integer.toString(this.start2 + 1);
        } else {
            coords2 = (this.start2 + 1) + "," + this.length2;
        }
        StringBuilder text = new StringBuilder();
        text.append("@@ -").append(coords1).append(" +").append(coords2).append(" @@\n");
        // Escape the body of the patch with %xx notation.
        for (RowEdit<cell_t> aDiff : this.diffs) {
            switch (aDiff.operation) {
            case INSERTION:
                text.append('+');
                break;
            case REMOVAL:
                text.append('-');
                break;
            case MATCH:
                text.append(' ');
                break;
            }
            text.append(JsCompat.encodeUri(aDiff.getTextAsString())).append("\n");
        }
        return text.toString();
    }

}
