package Parser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;

public class MiniDictionary {
    public HashMap<String, LinkedList<Integer>> dictionary; // term ; TF
    private String docID;
    private int naxFreq_count;
    private String maxFreq_word;
    private String docTI;


    MiniDictionary(String name, String title) {
        docID = name;
        dictionary = new HashMap<>();
        naxFreq_count = 0;
        maxFreq_word = "";
        docTI = title;
    }

    void addWord(String word, int placeInText) {
        LinkedList<Integer> currentPositions;
        //adds the word according to parsing rule 2
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

    private int containsKey(String word) {
        String upper = word.toUpperCase();
        String lower = word.toLowerCase();
        if (dictionary.containsKey(upper))
            return 1;
        if (dictionary.containsKey(lower))
            return 2;
        if (!Character.isLetter(word.charAt(0)) && dictionary.containsKey(word))
            return 3;
        return 0;
    }

    public String listOfData(String word) {
        return ""+docID+","+getFrequency(word)+","+printIndexes(Objects.requireNonNull(getIndexesOfWord(word)));
    }

    private String printIndexes(LinkedList<Integer> indexesOfWord) {
        StringBuilder s = new StringBuilder("[");
        indexesOfWord.forEach(i -> s.append(i).append("&"));
        s.replace(s.length()-1, s.length(), "]");
        return s.toString();
    }

    public int size() {
        return dictionary.size();
    }

    private LinkedList<Integer> getIndexesOfWord(String word) {
        return containsKey(word) != 0 ? dictionary.get(word) : null;
    }

    public String getMaxFreqWord() {
        return maxFreq_word;
    }

    public String getName() {
        return docID;
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
}
