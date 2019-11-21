package Engine;

import Structures.MiniDictionary;
import javafx.util.Pair;

import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Indexer implements Callable<HashMap<String, Pair<Integer, StringBuilder>>> {
    private ConcurrentLinkedDeque<MiniDictionary> m_miniDicList;

    public Indexer(ConcurrentLinkedDeque<MiniDictionary> minidic) {
        m_miniDicList = minidic;
    }

    /**
     * this class creates a temporary posting in a HashMap containing all data of MiniDictionay's sent
     *
     * @return an hash map representing all data of mini dics
     */
    @Override
    public HashMap<String, Pair<Integer, StringBuilder>> call() {
        HashMap<String, Pair<Integer, StringBuilder>> toReturn = new HashMap<>();
        if (m_miniDicList != null) {
            for(MiniDictionary miniDic : m_miniDicList)
                for(String word : miniDic.listOfWords())
                    if (toReturn.containsKey(word)) {

                    } else {

                    }


        }
        return toReturn;
    }






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
