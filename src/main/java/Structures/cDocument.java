package Structures;


/**
 * This class represent document. here we collect all the data on File
 */
public class cDocument {
    private String docId;
    private String docTitle;
    private String docDate;
    private String docText;
    private String docAuthor;
    private String docType;
    private String docSubject;
    private String docLang;
    private int length;
    private int maxTF;
    private int uniqueWords;

    public cDocument(String docId, String docDate, String docTitle, String docText, String docAuthor, String docLang) {
        this.docId = docId;
        this.docTitle = docTitle;
        this.docDate = docDate;
        this.docText = docText;
        this.docType = docType;
        this.docSubject = docSubject;
        this.docLang = docLang;
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

    public String getDocAuthor() {
        return docAuthor;
    }

    public String getDocType() {
        return docType;
    }

    public String getDocSubject() {
        return docSubject;
    }

    public String getDocLang() {
        return docLang;
    }

    public void top5() {
        this.docLang = docLang;
    }


}
