package Engine;

import java.text.MessageFormat;

public class DocumentIndex {
    private String docId;
    private int maxFreq_count;
    private int uniqueWords;
    private int docLength;
    private String docTI;
    private String maxFreq_word;


    public DocumentIndex(String docName, int maxFreq, int numOfUniWords, String maxFreqWord, int docLength, String title) {
        this.maxFreq_word = maxFreqWord;
        this.docLength = docLength;
        this.docTI = title;
        this.docId = docName;
        this.maxFreq_count = maxFreq;
        this.uniqueWords = numOfUniWords;
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0}\t{1}\t{2}\t{3}\t{4}\t{5}\t\n", docId, maxFreq_count, uniqueWords, maxFreq_word, docTI, docLength);
    }
}


