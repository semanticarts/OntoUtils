/**
 * 
 */
package com.semanticarts.onto.util;

import java.util.HashMap;
import java.io.BufferedReader;

/**
 * @author thill
 *
 */
public class PrefixHashMap {
    HashMap<String, String> theMap;

    /**
     * The constructor reads lines from the br parameter, and constructs a hash map.
     * 
     * Each line is presumed to contain a namespace prefix string followed by a tab
     * and the IRI defining the prefix.
     * 
     * These pairs are loaded into a hash map, with the IRI as the key and the
     * prefix as the value.
     */
    public PrefixHashMap(BufferedReader br) {
        try {
            theMap = new HashMap<String, String>();

            String line;
            while ((line = br.readLine()) != null) {
                int xTab = line.indexOf('\t');
                if (xTab >= 0) {
                    theMap.put(line.substring(xTab + 1), line.substring(0, xTab));
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Looks for a known prefix in the hash map for the given IRI, and replaces the
     * prefix portion of the IRI with the prefix if found.
     * 
     * @param s
     *            IRI possibly including a fragment identifier (string following '#'
     *            or '/')
     * @return if the IRI has a known prefix, it is replaced by the prefix and ":"
     *         replacing the '#' or ':'; otherwise s is returned unchanged
     */
    public String replacePrefix(String s) {
        int xHash = s.lastIndexOf('#');
        if (xHash < 0) {
            xHash = s.lastIndexOf('/');
        }
        if (xHash < 0)
            return s;
        String Prefix = theMap.get(s.substring(0, xHash + 1));
        if (Prefix == null) {
            return s;
        }
        String repl = new String(Prefix + ":" + s.substring(xHash + 1));
        return repl;
    }

}
