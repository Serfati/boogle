package parser;

import javafx.util.Pair;
import sun.awt.Mutex;

import java.text.MessageFormat;
import java.util.*;


public class MiniDictionary {
    public HashMap<String, LinkedList<Integer>> dictionary; // term ; TF
    private String docID;
    private int naxFreq_count;
    private String maxFreq_word;
    private String docTI;
    private Mutex mutex;
    private double rank;
    private Pair<String, Integer>[] places = new Pair[5];

    /**
     * cTor new MiniDictionary
     *
     * @param name name of the file and doc
     */
    MiniDictionary(String name, String title) {
        docID = name;
        dictionary = new HashMap<>();
        naxFreq_count = 0;
        maxFreq_word = "";
        docTI = title;
        this.mutex = new Mutex();
    }

    /**
     * adds a term to @MiniDictionary
     * @param word a term to add
     * @param placeInText  index of the term in text
     */
    void addWord(String word, int placeInText) {
        LinkedList<Integer> currentPositions;
        int result = containsKey(word);
        switch(result) {
            case 0:
                if (Character.isLetter(word.charAt(0))) if (Character.isUpperCase(word.charAt(0)))
                    word = word.toUpperCase();
                else
                    word = word.toLowerCase();
                currentPositions = new LinkedList<>();
                currentPositions.add(placeInText);
                dictionary.put(word, currentPositions);
                break;
            case 1:
                if (Character.isUpperCase(word.charAt(0))) {
                    currentPositions = dictionary.get(word.toUpperCase());
                    currentPositions.add(placeInText);
                } else {
                    currentPositions = dictionary.remove(word.toUpperCase());
                    currentPositions.add(placeInText);
                    dictionary.put(word.toLowerCase(), currentPositions);
                }
                break;
            case 2:
                word = word.toLowerCase();
                currentPositions = dictionary.get(word);
                currentPositions.add(placeInText);
                break;
            default:
                currentPositions = dictionary.get(word);
                currentPositions.add(placeInText);
                break;
        }
        //check if max freq has changed
        if (naxFreq_count < currentPositions.size()) {
            naxFreq_count = currentPositions.size();
            maxFreq_word = word;
        }
    }

    /**
     * checks if the term has been already added
     *
     * @param word the term to be checked
     * @return returns indicate
     */
    protected int containsKey(String word) {
        if (!Character.isLetter(word.charAt(0)) && dictionary.containsKey(word))
            return 3;
        String upper = word.toUpperCase();
        if (dictionary.containsKey(upper))
            return 1;
        String lower = word.toLowerCase();
        return dictionary.containsKey(lower) ? 2 : 0;

    }

    /**
     * returns indexes of the term (postions)
     *
     * @param word the term
     * @return the indexes of the term
     */
    private LinkedList<Integer> getIndexOfWord(String word) {
        return containsKey(word) != 0 ? dictionary.get(word) : null;
    }

    public void addRank(double rank) {
        this.mutex.lock();
        this.rank += rank;
        this.mutex.unlock();
    }

    public double getRank() {
        return rank;
    }

    /**
     * data about a certain term
     *
     * @param word the term
     * @return data of a term
     */
    public String listData(String word) {
        return MessageFormat.format("{0},{1},{2}", docID, getFrequency(word), printIndex(Objects.requireNonNull(getIndexOfWord(word))));
    }

    private String printIndex(LinkedList<Integer> indexesOfWord) {
        StringBuilder s = new StringBuilder("[");
        indexesOfWord.forEach(i -> s.append(i).append("&"));
        s.replace(s.length()-1, s.length(), "]");
        return s.toString();
    }

    public String getMaxFreqWord() {
        return maxFreq_word;
    }

    public String getName() {
        return docID;
    }

    /**
     *  size of the dictionary
     * @return  size
     */
    public int size() {
        return dictionary.size();
    }

    public int getFrequency(String word) {
        return size() > 0 && containsKey(word) != 0 ? dictionary.get(word).size() : 0;
    }

    public int getMaxFrequency() {
        return naxFreq_count;
    }

    public int getDocLength() {
        return dictionary.values().stream().mapToInt(LinkedList::size).sum();
    }

    public String getTitle() {
        return docTI;
    }

    public void setEntitiesList(String aNull) {

    }

    /**
     * sets the 5 primary words
     */
    public void setPrimaryWords() {
        TreeMap<String, LinkedList<Integer>> sorted = new TreeMap<>((o1, o2) -> o1.equals(o2) ? 0 : dictionary.get(o1).size() > dictionary.get(o2).size() ? -1 : 1);
        sorted.putAll(dictionary);
        int i = 0;
        for(Map.Entry<String, LinkedList<Integer>> first : ((Map<String, LinkedList<Integer>>) sorted).entrySet()) {
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


}
