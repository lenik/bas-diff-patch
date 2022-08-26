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

import name.fraser.neil.generic.diff_match_patch_compat;

public class Speedtest {

  public static void main(String args[]) throws IOException {
    String text1 = readFile("name/fraser/neil/plaintext/Speedtest1.txt");
    String text2 = readFile("name/fraser/neil/plaintext/Speedtest2.txt");

    diff_match_patch_compat dmp = new diff_match_patch_compat();
    dmp.core.Diff_Timeout = 0;

    // Execute one reverse diff as a warmup.
    dmp.diff_main(text2, text1, false);

    long start_time = System.nanoTime();
    dmp.diff_main(text1, text2, false);
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
