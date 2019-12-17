package Engine;

import Parser.MiniDictionary;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class InvertedIndex implements Callable<HashMap<String, Pair<Integer, StringBuilder>>> {
    // term  | num Of appearance | pointer(path of posting file, line number in the posting)
    private ConcurrentHashMap<String, Term> invertedIndexDic = new ConcurrentHashMap<>();
    private ConcurrentLinkedDeque<MiniDictionary> m_miniDicList;

    public InvertedIndex(ConcurrentLinkedDeque<MiniDictionary> minidic) {
        m_miniDicList = minidic;
    }

    public InvertedIndex() {
    }


    public InvertedIndex(File file) {
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            while(line != null) {
                String[] curLine = line.split("\t");
                Term cur = new Term(curLine[0], Integer.parseInt(curLine[1]), Integer.parseInt(curLine[2]), Integer.parseInt(curLine[3]));
                invertedIndexDic.put(curLine[0], cur);
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public HashMap<String, Pair<Integer, StringBuilder>> call() {
        HashMap<String, Pair<Integer, StringBuilder>> toReturn = new HashMap<>();
        if (m_miniDicList != null) {
            for(MiniDictionary miniDic : m_miniDicList)
                miniDic.m_dictionary.keySet().forEach(word -> {
                    if (toReturn.containsKey(word)) { //if the word already exists
                        Pair<Integer, StringBuilder> all = toReturn.remove(word);
                        int newShows = all.getKey()+miniDic.getFrequency(word);
                        StringBuilder newSb = all.getValue().append(miniDic.listOfData(word)).append("|");
                        Pair<Integer, StringBuilder> newAll = new Pair<>(newShows, newSb);
                        toReturn.put(word, newAll);
                    } else { //if the word doesn't exist
                        int shows = miniDic.getFrequency(word);
                        StringBuilder sb = new StringBuilder(miniDic.listOfData(word)+"|");
                        Pair<Integer, StringBuilder> all = new Pair<>(shows, sb);
                        toReturn.put(word, all);
                    }
                });
        }
        return toReturn;
    }

    public void addTerm(String term) {
        if (term.charAt(0) < 123) {
            if (!invertedIndexDic.containsKey(term)) {//if the term doesn't exist in the inverted index
                Term first = new Term(term, 1, -1, -1);
                invertedIndexDic.put(term, first);
            } else {
                invertedIndexDic.get(term).increaseTermFreq(1); // if the term exist in the inverted index increase number of freqenecy
            }
        }
    }

    public void deleteEntriesOfIrrelevant() {
        for(String s : invertedIndexDic.keySet()) {
            Term cur = invertedIndexDic.get(s);
            if (cur.getNumOfAppearances() == -1) {
                int termFreqCur = cur.getTermFreq();
                if (invertedIndexDic.get(s.toLowerCase()) != null) {
                    invertedIndexDic.get(s.toLowerCase()).increaseTermFreq(termFreqCur);
                }
                invertedIndexDic.remove(s);
            }
        }
    }

    public int getNumOfUniqueTerms() {
        return invertedIndexDic.size();
    }

    public void setPointer(String minTerm, int lineNumber) {
        if (invertedIndexDic.containsKey(minTerm))
            invertedIndexDic.get(minTerm).setPointer(lineNumber);
    }

    public void setNumOfAppearance(String term, int numOfAppearance) {
        if (invertedIndexDic.containsKey(term))
            invertedIndexDic.get(term).setNumOfAppearance(numOfAppearance);
    }

    public ObservableList<ShowDictionaryRecord> getRecords() {
        ObservableList<ShowDictionaryRecord> showDictionaryRecords = FXCollections.observableArrayList();
        TreeMap<String, Term> sorted = new TreeMap<>(invertedIndexDic);
        sorted.keySet().stream().map(s -> new ShowDictionaryRecord(s, invertedIndexDic.get(s).getNumOfAppearances())).forEach(showDictionaryRecords::add);
        return showDictionaryRecords;
    }

    @Override
    public String toString() {
        StringBuilder toWrite = new StringBuilder();
        for(Term cur : invertedIndexDic.values()) {
            toWrite.append(cur.toString());
        }
        return toWrite.toString();
    }

    public class ShowDictionaryRecord {
        private StringProperty termProperty;
        private IntegerProperty countProperty;

        ShowDictionaryRecord(String term, int count) {
            this.termProperty = new SimpleStringProperty(term);
            this.countProperty = new SimpleIntegerProperty(count);
        }

        public StringProperty getTermProperty() {
            return termProperty;
        }

        public IntegerProperty getCountProperty() {
            return countProperty;
        }
    }

    public class Term {
        private String m_word;//the term
        private int m_termFreq;//number of documents the words appears in
        private int m_numOfAppearances;//number of times the word has appeared
        private int m_postingLine;//line number in the posting

        Term(String word, int termFreq, int numOfAppearances, int postingLine) {
            this.m_word = word;
            this.m_termFreq = termFreq;
            this.m_numOfAppearances = numOfAppearances;
            this.m_postingLine = postingLine;
        }

        void increaseTermFreq(int termFreqCur) {
            m_termFreq += termFreqCur;
        }

        int getTermFreq() {
            return m_termFreq;
        }

        int getNumOfAppearances() {
            return m_numOfAppearances;
        }

        void setPointer(int postingLine) {
            this.m_postingLine = postingLine;
        }

        void setNumOfAppearance(int numOfAppearance) {
            this.m_numOfAppearances = numOfAppearance;
        }

        @Override
        public String toString() {
            return m_word+"\t"+m_termFreq+"\t"+m_numOfAppearances+"\t"+m_postingLine+"\n";
        }
    }


}
