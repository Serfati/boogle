package Model.Engine;

import Model.Parser.MiniDictionary;
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
        // adding to inverted index the term and the other data
        // AND adding to the map (temporary posting)
        HashMap<String, Pair<Integer, StringBuilder>> toReturn = new HashMap<>();
        if (m_miniDicList != null) m_miniDicList.forEach(miniDic -> miniDic.listOfWords().forEach(word -> {
            if (toReturn.containsKey(word)) { //if the word already exists
                Pair<Integer, StringBuilder> all = toReturn.remove(word);
                int newShows = all.getKey()+miniDic.getFrequency(word);
                StringBuilder newSb = all.getValue().append(miniDic.listOfData(word)).append("|");
                Pair<Integer, StringBuilder> newAll = new Pair<>(newShows, newSb);
                toReturn.put(word, newAll);
            } else { //if the word doesn't exist
                int shows = miniDic.getFrequency(word);
                StringBuilder sb = new StringBuilder(miniDic.listOfData(word)+"|");
                Pair<Integer, StringBuilder> all = new Pair<>(shows, sb);
                toReturn.put(word, all);
            }
        }));
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
                c1 = o1.toLowerCase().charAt(i);
                c2 = o2.toLowerCase().charAt(i);
                comparison = c1-c2;
                if (comparison != 0)
                    return comparison;
            }
            return Integer.compare(o1.length(), o2.length());
        }
}
}


