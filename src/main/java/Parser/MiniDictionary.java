package Parser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;

public class MiniDictionary {
    public HashMap<String, LinkedList<Integer>> m_dictionary; //string - the term ; int - TF in the doc
    private String m_name;
    private int m_maxFreq;
    private String m_maxFreqWord;
    private String m_title;


    MiniDictionary(String name, String title) {
        m_name = name;
        m_dictionary = new HashMap<>();
        m_maxFreq = 0;
        m_maxFreqWord = "";
        m_title = title;
    }

    void addWord(String word, int placeInText) {
        LinkedList<Integer> currentPositions;
        //adds the word according to parsing rule 2
        int result = containsKey(word);
        if (result == 0) {
            if (Character.isLetter(word.charAt(0))) {
                if (Character.isUpperCase(word.charAt(0)))
                    word = word.toUpperCase();
                else
                    word = word.toLowerCase();
            }
            currentPositions = new LinkedList<>();
            currentPositions.add(placeInText);
            m_dictionary.put(word, currentPositions);
        } else if (result == 1) {
            if (Character.isUpperCase(word.charAt(0))) {
                currentPositions = m_dictionary.get(word.toUpperCase());
                currentPositions.add(placeInText);
            } else {
                currentPositions = m_dictionary.remove(word.toUpperCase());
                currentPositions.add(placeInText);
                m_dictionary.put(word.toLowerCase(), currentPositions);
            }
        } else if (result == 2) {
            word = word.toLowerCase();
            currentPositions = m_dictionary.get(word);
            currentPositions.add(placeInText);
        } else {
            currentPositions = m_dictionary.get(word);
            currentPositions.add(placeInText);
        }
        //check if max freq has changed
        if (m_maxFreq < currentPositions.size()) {
            m_maxFreq = currentPositions.size();
            m_maxFreqWord = word;
        }
    }

    private int containsKey(String word) {
        String upper = word.toUpperCase();
        String lower = word.toLowerCase();
        if (m_dictionary.containsKey(upper))
            return 1;
        if (m_dictionary.containsKey(lower))
            return 2;
        if (!Character.isLetter(word.charAt(0)) && m_dictionary.containsKey(word))
            return 3;
        return 0;
    }

    public String listOfData(String word) {
        return ""+m_name+","+getFrequency(word)+","+printIndexes(Objects.requireNonNull(getIndexesOfWord(word)));
    }

    private String printIndexes(LinkedList<Integer> indexesOfWord) {
        StringBuilder s = new StringBuilder("[");
        for(Integer i : indexesOfWord) {
            s.append(i).append("&");
        }
        s.replace(s.length()-1, s.length(), "]");
        return s.toString();
    }

    public int size() {
        return m_dictionary.size();
    }

    private LinkedList<Integer> getIndexesOfWord(String word) {
        if (containsKey(word) != 0)
            return m_dictionary.get(word);
        return null;
    }

    public String getMaxFreqWord() {
        return m_maxFreqWord;
    }

    public String getName() {
        return m_name;
    }

    public int getFrequency(String word) {
        if (size() > 0 && containsKey(word) != 0)
            return m_dictionary.get(word).size();
        return 0;
    }

    public int getMaxFrequency() {
        return m_maxFreq;
    }

    public int getDocLength() {
        int count = 0;
        for(LinkedList l : m_dictionary.values()) {
            count += l.size();
        }
        return count;
    }

    public String getTitle() {
        return m_title;
    }
}
