package net.bodz.bas.compare.dmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bodz.bas.text.row.IRow;
import net.bodz.bas.text.row.IntegerRow;
import net.bodz.bas.text.row.MutableRow;

public class RowPacker<cell_t> {

    Config config;
    DMPDiff<cell_t> diff;
    cell_t separator;

    public RowPacker(DMPDiff<cell_t> diff) {
        this.config = diff.config;
        this.diff = diff;
        this.separator = diff.separator();
    }

    /**
     * Split two texts into a list of strings. Reduce the texts to a string of hashes where each
     * Unicode character represents one line.
     *
     * @param row1
     *            First string.
     * @param row2
     *            Second string.
     * @return An object containing the encoded text1, the encoded text2 and the List of unique
     *         strings. The zeroth element of the List of unique strings is intentionally blank.
     */
    public <T extends cell_t> LinesToCharsResult<T> linesToChars(IRow<T> row1, IRow<T> row2) {
        List<IRow<T>> lineArray = new ArrayList<IRow<T>>();
        Map<IRow<T>, Integer> lineHash = new HashMap<IRow<T>, Integer>();
        // e.g. linearray[4] == "Hello\n"
        // e.g. linehash.get("Hello\n") == 4

        // "\x00" is a valid character, but various debuggers don't like it.
        // So we'll insert a junk entry to avoid generating a null character.
        lineArray.add(new MutableRow<T>());

        // Allocate 2/3rds of the space for text1, the rest for text2.
        IRow<Integer> chars1 = linesToCharsMunge(row1, lineArray, lineHash, 40000);
        IRow<Integer> chars2 = linesToCharsMunge(row2, lineArray, lineHash, 65535);
        return new LinesToCharsResult<T>(chars1, chars2, lineArray);
    }

    /**
     * Split a text into a list of strings. Reduce the texts to a string of hashes where each
     * Unicode character represents one line.
     *
     * @param text
     *            String to encode.
     * @param lineArray
     *            List of unique strings.
     * @param lineHash
     *            Map of strings to indices.
     * @param maxLines
     *            Maximum length of lineArray.
     * @return Encoded string.
     */
    private <T extends cell_t> IRow<Integer> linesToCharsMunge(IRow<T> text, List<IRow<T>> lineArray,
            Map<IRow<T>, Integer> lineHash, int maxLines) {
        int lineStart = 0;
        int lineEnd = -1;
        IRow<T> line;
        IntegerRow ints = new IntegerRow();
        // Walk the text, pulling out a substring for each line.
        // text.split('\n') would would temporarily double our memory footprint.
        // Modifying text would create many large strings to garbage collect.
        while (lineEnd < text.length() - 1) {
            lineEnd = text.indexOf(separator, lineStart);
            if (lineEnd == -1) {
                lineEnd = text.length() - 1;
            }
            line = text.slice(lineStart, lineEnd + 1);

            if (lineHash.containsKey(line)) {
                ints.append(lineHash.get(line));
            } else {
                if (lineArray.size() == maxLines) {
                    // Bail out at 65535 because
                    // String.valueOf((char) 65536).equals(String.valueOf(((char) 0)))
                    line = text.slice(lineStart);
                    lineEnd = text.length();
                }
                lineArray.add(line);
                lineHash.put(line, lineArray.size() - 1);
                ints.append(lineArray.size() - 1);
            }
            lineStart = lineEnd + 1;
        }
        return ints;
    }

    /**
     * Rehydrate the text in a diff from a string of line hashes to real lines of text.
     *
     * @param diffs
     *            List of Diff objects.
     * @param lineArray
     *            List of unique strings.
     */
    public <T extends cell_t> ChangeList<cell_t> charsToLines(List<RowChangement<Integer>> diffs,
            List<? extends IRow<T>> lineArray) {
        ChangeList<cell_t> result = new ChangeList<cell_t>(diff);
        for (RowChangement<Integer> diff : diffs) {
            MutableRow<T> text = new MutableRow<T>();
            for (int j = 0; j < diff.text.length(); j++) {
                Integer index = diff.text.cellAt(j);
                text.append(lineArray.get(index));
            }
            result.addConst(new RowChangement<T>(diff.operation, text));
        }
        return result;
    }

}
