package indexer;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import parser.MiniDictionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class InvertedIndex implements Callable<HashMap<String, Pair<Integer, StringBuilder>>> {

    private ConcurrentHashMap<String, Term> invertedIndexDic = new ConcurrentHashMap<>();
    private ConcurrentLinkedDeque<MiniDictionary> m_miniDicList;

    public InvertedIndex() {
    }

    public InvertedIndex(ConcurrentLinkedDeque<MiniDictionary> minidic) {
        m_miniDicList = minidic;
    }

    public InvertedIndex(File file) {
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            while(line != null) {
                String[] curLine = line.split("\t");
                String terf = curLine[1].replace(",", "");
                String noa = curLine[2].replace(",", "");
                String postingLine = curLine[3].replace(",", "");
                Term cur = new Term(curLine[0], Integer.parseInt(terf), Integer.parseInt(noa), Integer.parseInt(postingLine));
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
        HashMap<String, Pair<Integer, StringBuilder>> postingForm = new HashMap<>();
        if (m_miniDicList != null) m_miniDicList.forEach(miniDic -> miniDic.dictionary.keySet().forEach(word -> {
            if (!postingForm.containsKey(word)) { //if the word doesn't exist
                int shows = miniDic.getFrequency(word);
                StringBuilder sb = new StringBuilder(miniDic.listData(word)+"|");
                Pair<Integer, StringBuilder> all = new Pair<>(shows, sb);
                postingForm.put(word, all);
            } else { //if the word already exists
                Pair<Integer, StringBuilder> all = postingForm.remove(word);
                int newShows = all.getKey()+miniDic.getFrequency(word);
                StringBuilder sb2 = all.getValue().append(miniDic.listData(word)).append("|");
                Pair<Integer, StringBuilder> newAll = new Pair<>(newShows, sb2);
                postingForm.put(word, newAll);
            }
        }));
        return postingForm;
    }

    public void addTerm(String term) {
        if (term.charAt(0) < 123)
            if (!invertedIndexDic.containsKey(term)) {//if the term doesn't exist in the inverted index
                Term first = new Term(term, 1, -1, -1);
                invertedIndexDic.put(term, first);
            } else
                invertedIndexDic.get(term).increaseTermFreq(1); // if the term exist in the inverted index increase number of freqenecy
    }

    public void deleteEntriesOfIrrelevant() {
        for(String s : invertedIndexDic.keySet()) {
            Term cur = invertedIndexDic.get(s);
            switch(cur.getNumOfAppearances()) {
                case -1:
                    int termFreqCur = cur.getTermFreq();
                    if (invertedIndexDic.get(s.toLowerCase()) != null)
                        invertedIndexDic.get(s.toLowerCase()).increaseTermFreq(termFreqCur);
                    invertedIndexDic.remove(s);
                    break;
            }
        }
    }

    public String getPostingLink(String word) {
        if (invertedIndexDic.get(word) == null)
            return "";
        return invertedIndexDic.get(word).getPostingLink();
    }

    public int getNumOfUniqueTerms() {
        return invertedIndexDic.size();
    }

    public void setPointer(String minTerm, int lineNumber) {
        if (invertedIndexDic.containsKey(minTerm))
            invertedIndexDic.get(minTerm).setPointer(lineNumber);
    }

    public void setNumOfAppearances(String term, int numOfAppearance) {
        if (invertedIndexDic.containsKey(term))
            invertedIndexDic.get(term).setNumOfAppearance(numOfAppearance);
    }

    public ObservableList<ShowDictionaryRecord> getRecord() {
        ObservableList<ShowDictionaryRecord> showDictionaryRecords = FXCollections.observableArrayList();
        TreeMap<String, Term> sorted = new TreeMap<>(invertedIndexDic);
        sorted.keySet().stream().map(s -> new ShowDictionaryRecord(s, invertedIndexDic.get(s).getNumOfAppearances())).forEach(showDictionaryRecords::add);
        return showDictionaryRecords;
    }

    @Override
    public String toString() {
        return invertedIndexDic.values().stream().map(Term::toString).collect(Collectors.joining());
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
        private String word;
        private int freq;  // idf
        private int numOfAppearancesTF; // tf
        private int postingLine;

        public Term(String word, int termFreq, int numOfAppearances, int postingLine) {
            this.word = word;
            this.freq = termFreq;
            this.numOfAppearancesTF = numOfAppearances;
            this.postingLine = postingLine;
        }

        public void increaseTermFreq(int termFreqCur) {
            freq += termFreqCur;
        }

        public int getTermFreq() {
            return freq;
        }

        public int getNumOfAppearances() {
            return numOfAppearancesTF;
        }

        public void setPointer(int postingLine) {
            this.postingLine = postingLine;
        }

        String getPostingLink() {
            return ""+postingLine;
        }

        public void setNumOfAppearance(int numOfAppearance) {
            this.numOfAppearancesTF = numOfAppearance;
        }

        @Override
        public String toString() {
            return MessageFormat.format("{0}\t{1}\t{2}\t{3}\n", word, freq, numOfAppearancesTF, postingLine);
        }
    }
}
