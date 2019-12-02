package Model.Structures;

import java.io.Serializable;

/**
 * This class represent document. here we collect all the data on File
 */
public class cDocument implements Serializable {
    private String docId;
    private String docTitle;
    private String docDate;
    private String docText;
    private String docLang;

    private int maxTF;
    private int uniqueWords;

    public cDocument(String docId, String docDate, String docTitle, String docText, String docLang) {
        this.docId = docId;
        this.docTitle = docTitle;
        this.docDate = docDate;
        this.docText = docText;
        this.docLang = docLang;
        maxTF = 0;
        uniqueWords = 0;
    }

    public String getDocId() {
        return docId;
    }

    public String getDocTitle() {
        return docTitle;
    }

    public String getDocDate() {
        return docDate;
    }

    public String getDocText() {
        return docText;
    }

    public String getDocLang() {
        return docLang;
    }

    public int getMaxFrequency() {
        return maxTF;
    }

    public int getNumOfUniqueTerms() {
        return uniqueWords;
    }
}
