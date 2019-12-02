package Model.Structures;

import javafx.util.Pair;

import java.util.*;

public class MiniDictionary {
    private String m_name; //name of the doc that past pars
    private HashMap<String, LinkedList<Integer>> m_dictionary; //string - the term ; int - TF in the doc
    private int m_maxFreq;
    private String m_lang;
    private String m_maxFreqWord;
    private String m_title;
    private Pair[] places = new Pair[5];

    /**
     * create new MiniDictionary
     *
     * @param name    name of the file and doc
     * @param docLang document language
     */
    public MiniDictionary(String name, String title, String docLang) {
        m_name = name;
        m_dictionary = new HashMap<>();
        m_maxFreq = 0;
        m_lang = docLang;
        m_maxFreqWord = "";
        m_title = title;
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
        }
        else if (result==1){
            if (Character.isUpperCase(word.charAt(0))) {
                currentPositions = m_dictionary.get(word.toUpperCase());
                currentPositions.add(placeInText);
            } else {
                currentPositions = m_dictionary.remove(word.toUpperCase());
                currentPositions.add(placeInText);
                m_dictionary.put(word.toLowerCase(),currentPositions);
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
     * returns all the terms in the dictionary
     *
     * @return all the terms in the dictionary
     */
    public Set<String> listOfWords() {
        return m_dictionary.keySet();
    }

    /**
     * returns the data about a certain term
     *
     * @param word the term
     * @return the data about a certain term
     */
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

    /**
     * returns the size of the dictionary
     *
     * @return the size of the dictionary
     */
    public int size() {
        return m_dictionary.size();
    }

    /**
     * returns the indexes of the term
     *
     * @param word the term
     * @return the indexes of the term
     */
    private LinkedList<Integer> getIndexesOfWord(String word) {
        if (containsKey(word) != 0)
            return m_dictionary.get(word);
        return null;
    }

    /**
     * returns the word that has the max freq in the doc
     *
     * @return the word that has the max freq in the doc
     */
    public String getMaxFreqWord() {
        return m_maxFreqWord;
    }

    /**
     * returns the name of the doc
     *
     * @return the name of the doc
     */
    public String getName() {
        return m_name;
    }

    /**
     * returns the freq of a certain term
     *
     * @param word the term
     * @return the freq of a certain term
     */
    public int getFrequency(String word) {
        if (size() > 0 && containsKey(word) != 0)
            return m_dictionary.get(word).size();
        return 0;
    }

    /**
     * returns the max freq exists in a doc
     *
     * @return the max freq exists in a doc
     */
    public int getMaxFrequency() {
        return m_maxFreq;
    }

    /**
     * returns the document length
     *
     * @return document length
     */
    public int getDocLength() {
        return m_dictionary.values().stream().mapToInt(LinkedList::size).sum();
    }

    /**
     * sets the 5 primary words
     */
    public void setPrimaryWords() {
        Map<String, LinkedList<Integer>> inPlace = sorted(m_dictionary);
        int i = 0;
        for(Map.Entry<String, LinkedList<Integer>> first : inPlace.entrySet()) {
            if (Character.isUpperCase(first.getKey().charAt(0))) {
                LinkedList<Integer> cur = first.getValue();
                if (cur != null)
                    places[i] = new Pair<>(first.getKey(), cur.size());
            }
            if (places[i] == null)
                i--;
            i++;
            if (places[4] != null) break;
        }
    }

    /**
     * returns the primary words
     *
     * @return the primary words
     */
    public Pair[] getPrimaryWords() {
        return places;
    }

    /**
     * sorts a map
     *
     * @param toSort which map should be sorted
     * @return a sorted map
     */
    private Map<String, LinkedList<Integer>> sorted(Map<String, LinkedList<Integer>> toSort) {
        TreeMap<String, LinkedList<Integer>> sorted = new TreeMap<>((o1, o2) -> {
            if (o1.equals(o2)) return 0;
            if (m_dictionary.get(o1).size() > m_dictionary.get(o2).size())
                return -1;
            else return 1;
        });
        sorted.putAll(toSort);
        return sorted;
    }

    /**
     * returns doc title
     *
     * @return doc title
     */
    public String getTitle() {
        return m_title;
    }

    /**
     * returns document language
     *
     * @return document language
     */
    public String getDocLang() {
        return m_lang;
    }

    /**
     * returns number of appearnces of words
     *
     * @return number of appearnces of words
     */
    public HashMap<String, Integer> countAppearances() {
        HashMap<String, Integer> result = new HashMap<>();
        for(Map.Entry<String, LinkedList<Integer>> entry : m_dictionary.entrySet()) {
            result.put(entry.getKey(), entry.getValue().size());
        }
        return result;
    }
}




