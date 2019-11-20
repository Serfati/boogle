package Structures;

import java.util.HashMap;
import java.util.LinkedList;

public class MiniDictionary {

    private String docNo;
    private HashMap <String, LinkedList<Integer>> termsOfDoc;
    private int maxFreq;
    private String lang;
    private String maxFreqWord;

    public MiniDictionary(String docNo, String lang) {
        this.termsOfDoc = new HashMap<>();
        this.docNo = docNo;
        this.lang = lang;

    }
}
