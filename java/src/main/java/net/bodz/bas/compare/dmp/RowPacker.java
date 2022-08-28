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
    public <T extends cell_t> LinesToCharsResult<T> pack(IRow<T> row1, IRow<T> row2) {
        List<IRow<T>> packArray = new ArrayList<IRow<T>>();
        Map<IRow<T>, Integer> packHash = new HashMap<IRow<T>, Integer>();
        // e.g. linearray[4] == "Hello\n"
        // e.g. linehash.get("Hello\n") == 4

        // "\x00" is a valid character, but various debuggers don't like it.
        // So we'll insert a junk entry to avoid generating a null character.
        packArray.add(new MutableRow<T>());

        // Allocate 2/3rds of the space for text1, the rest for text2.
        IRow<Integer> atoms1 = splitAndPack(row1, packArray, packHash, 40000);
        IRow<Integer> atoms2 = splitAndPack(row2, packArray, packHash, 65535);
        return new LinesToCharsResult<T>(atoms1, atoms2, packArray);
    }

    /**
     * Split a text into a list of strings. Reduce the texts to a string of hashes where each
     * Unicode character represents one line.
     *
     * @param row
     *            String to encode.
     * @param packArray
     *            List of unique strings.
     * @param packHash
     *            Map of strings to indices.
     * @param maxPacks
     *            Maximum length of lineArray.
     * @return Encoded string.
     */
    private <T extends cell_t> IRow<Integer> splitAndPack(IRow<T> row, List<IRow<T>> packArray,
            Map<IRow<T>, Integer> packHash, int maxPacks) {
        int packStart = 0;
        int packEnd = -1;
        IRow<T> pack;
        IntegerRow atoms = new IntegerRow();
        // Walk the text, pulling out a substring for each line.
        // text.split('\n') would would temporarily double our memory footprint.
        // Modifying text would create many large strings to garbage collect.
        while (packEnd < row.length() - 1) {
            packEnd = row.indexOf(separator, packStart);
            if (packEnd == -1) {
                packEnd = row.length() - 1;
            }
            pack = row.slice(packStart, packEnd + 1);

            if (packHash.containsKey(pack)) {
                atoms.append(packHash.get(pack));
            } else {
                if (packArray.size() == maxPacks) {
                    // Bail out at 65535 because
                    // String.valueOf((char) 65536).equals(String.valueOf(((char) 0)))
                    pack = row.slice(packStart);
                    packEnd = row.length();
                }
                packArray.add(pack);
                packHash.put(pack, packArray.size() - 1);
                atoms.append(packArray.size() - 1);
            }
            packStart = packEnd + 1;
        }
        return atoms;
    }

    /**
     * Rehydrate the text in a diff from a string of line hashes to real lines of text.
     *
     * @param changes
     *            List of Diff objects.
     * @param packArray
     *            List of unique strings.
     */
    public <T extends cell_t> ChangeList<cell_t> unpack(List<RowChangement<Integer>> changes,
            List<IRow<T>> packArray) {
        ChangeList<cell_t> result = new ChangeList<cell_t>(diff);
        for (RowChangement<Integer> change : changes) {
            MutableRow<T> row = new MutableRow<T>();
            for (int j = 0; j < change.row.length(); j++) {
                Integer index = change.row.cellAt(j);
                row.append(packArray.get(index));
            }
            result.addConst(new RowChangement<T>(change.operation, row));
        }
        return result;
    }

}
