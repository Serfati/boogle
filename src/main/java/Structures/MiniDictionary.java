package Structures;

import java.util.HashMap;
import java.util.LinkedList;

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
}
