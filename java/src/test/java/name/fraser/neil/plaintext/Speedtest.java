// Copyright 2010 Google Inc. All Rights Reserved.

/**
 * Diff Speed Test
 *
 * Compile from diff-match-patch/java with:
 * javac -d classes src/name/fraser/neil/plaintext/diff_match_patch.java tests/name/fraser/neil/plaintext/Speedtest.java
 * Execute with:
 * java -classpath classes name/fraser/neil/plaintext/Speedtest
 *
 * @author fraser@google.com (Neil Fraser)
 */

package name.fraser.neil.plaintext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import net.bodz.bas.compare.dmp.diff_match_patch_compat;

public class Speedtest {

  public static void main(String args[]) throws IOException {
    String row1 = readFile("name/fraser/neil/plaintext/Speedtest1.txt");
    String row2 = readFile("name/fraser/neil/plaintext/Speedtest2.txt");

    diff_match_patch_compat dmp = new diff_match_patch_compat();
    dmp.config.Diff_Timeout = 0;

    // Execute one reverse diff as a warmup.
    dmp.diff_main(row2, row1, false);

    long start_time = System.nanoTime();
    dmp.diff_main(row1, row2, false);
    long end_time = System.nanoTime();
    System.out.printf("Elapsed time: %f\n", ((end_time - start_time) / 1000000000.0));
  }

  private static String readFile(String filename) throws IOException {
    // Read a file from disk and return the text contents.
    StringBuilder sb = new StringBuilder();
    URL resource = Speedtest.class.getClassLoader().getResource(filename);
    Reader input = new InputStreamReader(resource.openStream());
    BufferedReader bufRead = new BufferedReader(input);
    try {
      String line = bufRead.readLine();
      while (line != null) {
        sb.append(line).append('\n');
        line = bufRead.readLine();
      }
    } finally {
      bufRead.close();
      input.close();
    }
    return sb.toString();
  }
}
