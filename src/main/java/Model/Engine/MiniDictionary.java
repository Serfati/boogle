package Model.Engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;

public class MiniDictionary {
    protected HashMap<String, LinkedList<Integer>> m_dictionary; //string - the term ; int - TF in the doc
    String m_maxFreqWord;
    private int m_maxFreq;
    private String m_name;

    /**
     * create new MiniDictionary
     *
     * @param name name of the file and doc
     */
    public MiniDictionary(String name) {
        m_name = name;
        m_dictionary = new HashMap<>();
        m_maxFreq = 0;
    }

    /**
     * checks if the term has been added in any case
     *
     * @param word the term to be checked
     * @return returns 1 if exists in upper case, 2 if exists in lower case and 0 if doesnt exist
     */
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

    /**
     * returns the data about a certain term
     *
     * @param word the term
     * @return the data about a certain term
     */
    public String listOfData(String word) {
        StringBuilder s = new StringBuilder("[");
        Objects.requireNonNull(getIndexesOfWord(word)).forEach(i -> s.append(i).append("&"));
        s.replace(s.length()-1, s.length(), "]");
        return ""+m_name+","+getFrequency(word)+","+s.toString();
    }

    /**
     * returns the indexes of the term
     *
     * @param word the term
     * @return the indexes of the term
     */
    private LinkedList<Integer> getIndexesOfWord(String word) {
        return containsKey(word) != 0 ? m_dictionary.get(word) : null;
    }

    /**
     * returns the freq of a certain term
     *
     * @param word the term
     * @return the freq of a certain term
     */
    public int getFrequency(String word) {
        return m_dictionary.size() > 0 && containsKey(word) != 0 ? m_dictionary.get(word).size() : 0;
    }

    /**
     * adds a term to the dictionary
     *
     * @param word        the term to be added
     * @param placeInText the index of the term in the text
     */
    public void addWord(String word, int placeInText) {
        LinkedList<Integer> currentPositions;
        //adds the word according to parsing rule 2
        int result = containsKey(word);
        switch(result) {
            case 0:
                if (Character.isLetter(word.charAt(0)))
                    word = Character.isUpperCase(word.charAt(0)) ? word.toUpperCase() : word.toLowerCase();
                currentPositions = new LinkedList<>();
                currentPositions.add(placeInText);
                m_dictionary.put(word, currentPositions);
                break;
            case 1:
                if (Character.isUpperCase(word.charAt(0))) {
                    currentPositions = m_dictionary.get(word.toUpperCase());
                    currentPositions.add(placeInText);
                } else {
                    currentPositions = m_dictionary.remove(word.toUpperCase());
                    currentPositions.add(placeInText);
                    m_dictionary.put(word.toLowerCase(), currentPositions);
                }
                break;
            case 2:
                word = word.toLowerCase();
                currentPositions = m_dictionary.get(word);
                currentPositions.add(placeInText);
                break;
            default:
                currentPositions = m_dictionary.get(word);
                currentPositions.add(placeInText);
                break;
        }
        //check if max freq has changed
        if (m_maxFreq < currentPositions.size()) {
            m_maxFreq = currentPositions.size();
            m_maxFreqWord = word;
        }
    }
}




