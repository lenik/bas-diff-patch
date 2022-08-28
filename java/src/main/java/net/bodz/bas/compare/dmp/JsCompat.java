package net.bodz.bas.compare.dmp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class JsCompat {

    /**
     * Unescape selected chars for compatability with JavaScript's encodeURI. In speed critical
     * applications this could be dropped since the receiving application will certainly decode
     * these fine. Note that this function is case-sensitive. Thus "%3f" would not be unescaped. But
     * this is ok because it is only called with the output of URLEncoder.encode which returns
     * uppercase hex.
     *
     * Example: "%3F" -> "?", "%24" -> "$", etc.
     *
     * @param str
     *            The string to escape.
     * @return The escaped string.
     */
    public static String encodeUri(String str) {
        String s;
        try {
            s = URLEncoder.encode(str, "UTF-8").replace('+', ' ');
        } catch (UnsupportedEncodingException e) {
            // Not likely on modern system.
            throw new Error("This system does not support UTF-8.", e);
        }
        StringBuilder sb = new StringBuilder();
        int n = s.length();
        int i = 0;
        while (i < n) {
            char ch = s.charAt(i);
            if (ch == '%' && i + 2 < n) {
                String la3 = s.substring(i, i + 3);
                String defined = excludeCharsFromEncode.get(la3);
                if (defined != null) {
                    sb.append(defined);
                    i += 3;
                    continue;
                }
            }
            sb.append(ch);
            i++;
        }
        sb.append(s.substring(i));
        return sb.toString();
    }

    static Map<String, String> excludeCharsFromEncode;
    static {
        excludeCharsFromEncode = new HashMap<String, String>();
        excludeCharsFromEncode.put("%21", "!");
        excludeCharsFromEncode.put("%7E", "~");
        excludeCharsFromEncode.put("%27", "'");
        excludeCharsFromEncode.put("%28", "(");
        excludeCharsFromEncode.put("%29", ")");
        excludeCharsFromEncode.put("%3B", ";");
        excludeCharsFromEncode.put("%2F", "/");
        excludeCharsFromEncode.put("%3F", "?");
        excludeCharsFromEncode.put("%3A", ":");
        excludeCharsFromEncode.put("%40", "@");
        excludeCharsFromEncode.put("%26", "&");
        excludeCharsFromEncode.put("%3D", "=");
        excludeCharsFromEncode.put("%2B", "+");
        excludeCharsFromEncode.put("%24", "$");
        excludeCharsFromEncode.put("%2C", ",");
        excludeCharsFromEncode.put("%23", "#");
    }

}
