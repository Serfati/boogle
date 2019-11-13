package Parser;

import org.apache.commons.lang.math.NumberUtils;
import Engine.Stemmer;
import Structures.cDocument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Parse implements IParse {
    Stemmer stemmer;
    HashSet<String> stopWordSet;
    HashMap<String, String> replacements;
    HashMap<String, String> dates;
    cDocument currentCDocument;
    Boolean stem;
    String pathToWrite;


    public void parse(cDocument doc) {
        String doctext = doc.getDocText();
        String[] words = doctext.split("\\s");
        stemmer = new Stemmer();
        //stemmer.getStemmer();


        for(int i = 0; i < words.length-1; i++) {
            String word = words[i];
            String nextWord = words[i+1];
            String trdWord = words[i+2];
            if (word == null)
                return;

            if (stopWordSet.contains(word))
                return;

            else if (NumberUtils.isNumber(word)) {
                //check the next word
                if (dates.containsKey(nextWord))
                    months(nextWord, doc.getDocId());
                if (NumberUtils.isNumber(trdWord) && Integer.parseInt(trdWord) < 9999)
                    date(word+" "+nextWord+" "+trdWord, doc.getDocId());
                else {
                }

                double wordNumber = Double.parseDouble(word);
                if (wordNumber <= 999999 && wordNumber >= 1000)
                    KNumber(word, doc.getDocId());
                else if (wordNumber > 999999 && wordNumber < 1000000000) {
                    MNumber((word), doc.getDocId());
                }
            }


        }


    }

    public ArrayList<String> parse(String text) {
        return null;
    }


    public void loadStopWords(String path) {
    }

    public void loadDates() {
        dates.put("July", "07");
        dates.put("Feb", "02");
        dates.put("July", "07");
        dates.put("Feb", "02");
        dates.put("July", "07");
        dates.put("Feb", "02");
        dates.put("MAY", "05");
        dates.put("Feb", "02");
    }

    public void loadReplacements() {
        replacements.put(":", "");
        replacements.put(",", "");
        replacements.put(".", "");
    }

    public String months(String text, String DocID) {
        String val = "";
        if (dates.containsKey(text)) {
            val = dates.get(text);
        }
        return val;
    }

    public String date(String text, String DocID) {


        String val = "";

        return val;
    }

    public void units(String text, String DocID) {
    }

    public void percents(String text, String DocID) {
    }

    public void dollars(String text, String DocID) {
    }

    public void BNumber(String text, String DocID) {
    }

    public void MNumber(String text, String DocID) {
    }

    public void letters(String text, String DocID) {
    }

    public String KNumber(String text, String DocID) {
        double d = Double.parseDouble(text);
        d /= 1000;
        String s = d+"K";
        return s;

    }

    public void setDone(boolean done) {
    }

    public boolean isNumber(String n) {

        return false;
    }
    public boolean isDone() {
        return false;
    }

    public void initializeStopWordsTreeAndStrategies(String path) {
    }

    public void reset() {
    }

    public String getPathToWrite() {
        return pathToWrite;
    }

    public void setPathToWrite(String pathToWrite) {
        this.pathToWrite = pathToWrite;
    }
}
