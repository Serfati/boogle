package Model.Structures;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

public class MiniDictionary {

    private String docNo;
    private HashMap <String, LinkedList<Integer>> termsOfDoc;
    private int maxFreq;
    private String lang;
    private String maxFreqWord;
    private String m_title;

    public MiniDictionary(String name, String city, String title, String docLang) {
        docNo = name;
        termsOfDoc = new HashMap<>();
        maxFreq = 0;
        lang = docLang;
        maxFreqWord = "";
        m_title = title;
    }

    public MiniDictionary() {

    }

    void addWord(String word,int placeInText){
        LinkedList<Integer> currentPositions;
        //adds the word according to parsing rule 2
        int result = containsKey(word);
        if (result==0){
            if(Character.isLetter(word.charAt(0))) {
                if (Character.isUpperCase(word.charAt(0)))
                    word = word.toUpperCase();
                else
                    word = word.toLowerCase();
            }
            currentPositions = new LinkedList<>();
            currentPositions.add(placeInText);
            termsOfDoc.put(word, currentPositions);
        }
        else if (result==1){
            if (Character.isUpperCase(word.charAt(0))){
                currentPositions = termsOfDoc.get(word.toUpperCase());
                currentPositions.add(placeInText);
            }else {
                currentPositions = termsOfDoc.remove(word.toUpperCase());
                currentPositions.add(placeInText);
                termsOfDoc.put(word.toLowerCase(),currentPositions);
            }
        }
        else if(result==2){
            word=word.toLowerCase();
            currentPositions = termsOfDoc.get(word);
            currentPositions.add(placeInText);
        }
        else{
            currentPositions = termsOfDoc.get(word);
            currentPositions.add(placeInText);
        }
        //check if max freq has changed
        if (maxFreq < currentPositions.size()) {
            maxFreq = currentPositions.size();
            maxFreqWord = word;
        }
    }

    private LinkedList<Integer> getIndexesOfWord(String word){
        if (containsKey(word)!=0)
            return termsOfDoc.get(word);
        return null;
    }

    private String printIndexes(LinkedList<Integer> indexesOfWord) {
        StringBuilder s = new StringBuilder("[");
        for (Integer i: indexesOfWord) {
            s.append(i).append("&");
        }
        s.replace(s.length()-1,s.length(),"]");
        return s.toString();
    }

    public String listOfData(String word){
        return ""+docNo+","+getFrequency(word)+","+ printIndexes(getIndexesOfWord(word));
    }

    public int getFrequency(String word){
        if (size()>0 && containsKey(word)!=0)
            return termsOfDoc.get(word).size();
        return 0;
    }

    private int containsKey(String word) {
        String upper = word.toUpperCase();
        String lower = word.toLowerCase();
        if (termsOfDoc.containsKey(upper))
            return 1;
        if (termsOfDoc.containsKey(lower))
            return 2;
        if (!Character.isLetter(word.charAt(0)) && termsOfDoc.containsKey(word))
            return 3;
        return 0;
    }

    public Set<String> listOfWords() {
        return termsOfDoc.keySet();
    }

    private int size() {
        return termsOfDoc.size();
    }
}
