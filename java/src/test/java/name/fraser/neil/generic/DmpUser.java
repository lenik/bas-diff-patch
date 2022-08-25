package name.fraser.neil.generic;

import java.util.LinkedList;

import org.junit.Assert;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;
import name.fraser.neil.plaintext.diff_match_patch.Patch;

public class DmpUser
        extends Assert {

    public static void main(String[] args) {
        diff_match_patch dmp = new diff_match_patch();
        String a = "-a;b;c;d;e".replace(';', '\n');
        String b = "-aY;b;d;xx".replace(';', '\n');

        System.out.println("--------- a: " + a);
        System.out.println("--------- b: " + b);

        LinkedList<Diff> diffs = dmp.diff_main(a, b);
        String delta = dmp.diff_toDelta(diffs);
        System.out.println(delta);
        System.out.println();

        LinkedList<Diff> diffs2 = dmp.diff_fromDelta(a, delta);
        String delta2 = dmp.diff_toDelta(diffs2);
        assertEquals(delta, delta2);
        String html = dmp.diff_prettyHtml(diffs2);
        System.out.println(html);

        int index = dmp.match_main("hi, baz the b-ar whats baR damn it.", "bar", 0);
        System.out.println(index);

        LinkedList<Patch> patches = dmp.patch_make(a, b);
        String patchText = dmp.patch_toText(patches);

        System.out.println(patchText);
        String c = "add mores;foo;bar;;;;b;c;d;e".replace(';', '\n');
        Object[] d = dmp.patch_apply(patches, c);

    }

}
