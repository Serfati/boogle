package Engine;

import Structures.Term;

import java.util.Comparator;
import java.util.TreeMap;

public class Indexer {

    private static TreeMap<String, Term> termsDictionary;
    private String pathForWriting;
    private boolean useStemming;

    public Indexer() {
        termsDictionary = new TreeMap<>(new StringComparator());
        pathForWriting = null;
        useStemming=false;
    }


    public void createInvertedIndex()throws Exception{}



    /**
     * compares between two strings while ignoring upper cases.
     * meaning, two string, one in upper case and the other in lower case, will be valuated as equals.
     */
    public static class StringComparator implements Comparator<String> {
        public int compare(String o1, String o2) {
            int comparison = 0;
            int c1, c2;
            for (int i = 0; i < o1.length() && i < o2.length(); i++) {
                c1 = (int) o1.toLowerCase().charAt(i);
                c2 = (int) o2.toLowerCase().charAt(i);
                comparison = c1 - c2;
                if (comparison != 0)
                    return comparison;
            }
            return Integer.compare(o1.length(), o2.length());
        }
    }
}
