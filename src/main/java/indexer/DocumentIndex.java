package indexer;

import javafx.util.Pair;

import java.text.MessageFormat;
import java.util.Arrays;

public class DocumentIndex {
    private String docId;
    private String docTI;
    private String maxFreq_word;
    private int maxFreq_count;
    private int docLength;
    private int uniqueWords;
    private Pair<String, Integer>[] mainEntities; //TODO

    public DocumentIndex(String docName, int maxFreq, int numOfUniWords, String maxFreqWord, int docLength, String title) {
        this.maxFreq_word = maxFreqWord;
        this.docLength = docLength;
        this.docTI = title;
        this.docId = docName;
        this.maxFreq_count = maxFreq;
        this.uniqueWords = numOfUniWords;
    }

    public int getDocLength() {
        return docLength;
    }

    public StringBuilder getFiveEntities() {
      StringBuilder pw = new StringBuilder();
      Arrays.stream(mainEntities).map(Pair::getKey).forEach(pw::append);
      return  pw;
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0}\t{1}\t{2}\t{3}\t{4}\t{5}\t\n", docId, maxFreq_count, uniqueWords, maxFreq_word, docTI, docLength);
    }
}


