package Model.Engine;

import java.io.Serializable;

/**
 * This class represent document. here we collect all the data on File
 */
public class cDocument implements Serializable {
    private String docId;
    private String docTitle;
    private String docText;
    private String docLang;

    public cDocument(String docId, String docDate, String docTitle, String docText, String docLang) {
        this.docId = docId;
        this.docTitle = docTitle;
        this.docText = docText;
        this.docLang = docLang;
    }
    public String getDocId() {
        return docId;
    }
    public String getDocTitle() {
        return docTitle;
    }
    public String getDocText() {
        return docText;
    }
    public String getDocLang() {
        return docLang;
    }

}
