package indexer;

import javafx.util.Pair;

import java.text.MessageFormat;

public class DocumentIndex {
    private String docId;
    private String docTI;
    private String maxFreq_word;
    private int maxFreq_count;
    private int docLength;
    private int uniqueWords;
    Pair<String, Integer>[] mainEntities;

    public DocumentIndex(String docName, int maxFreq, int numOfUniWords, String maxFreqWord, String title, int docLength, Pair<String, Integer>[] primaryWords) {
        this.maxFreq_word = maxFreqWord;
        this.docLength = docLength;
        this.docTI = title;
        this.docId = docName;
        this.maxFreq_count = maxFreq;
        this.uniqueWords = numOfUniWords;
        this.mainEntities = primaryWords;
    }

    public String getDocTI() {
        return docTI;
    }

    public int getDocLength() {
        return docLength;
    }

    public Pair<String, Integer>[] getFiveEntities() {
        return mainEntities;
    }

    public String get5words() {
        StringBuilder s = new StringBuilder();
        int i = 0;
        while(i < mainEntities.length) {
            if (mainEntities[i] == null)
                break;
            s.append(mainEntities[i].getValue()).append("\t").append(mainEntities[i].getKey()).append("\n");
            i++;
        }
        return s.toString();
    }

    @Override
    public String toString() {
        StringBuilder pw = new StringBuilder();
        if (mainEntities != null) {
            for(int i = 0; i < mainEntities.length-1; i++) {
                if (mainEntities[i] != null)
                    pw.append(mainEntities[i].getKey()).append("~").append(mainEntities[i].getValue()).append("#");
            }
            if (mainEntities[mainEntities.length-1] != null)
                pw.append(mainEntities[mainEntities.length-1].getKey()).append("~").append(mainEntities[mainEntities.length-1].getValue());
        }
        return MessageFormat.format("{0}\t{1}\t{2}\t{3}\t{4}\t{5}\t{6}\n", docId, maxFreq_count, uniqueWords, maxFreq_word, docTI, docLength, pw);
    }
}