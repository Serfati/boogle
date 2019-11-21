package Structures;

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

    public int size() {
        return termsOfDoc.size();
    }
}
