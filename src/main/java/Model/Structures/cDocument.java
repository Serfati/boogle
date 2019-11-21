package Model.Structures;


/**
 * This class represent document. here we collect all the data on File
 */
public class cDocument {
    private String docId = "";
    private String docTitle = "";
    private String docDate = "";
    private String docText = "";
    private String docOrgin = "";
    private String docAuthor = "";
    private String docLang = "";

    private int maxTF;
    private int uniqueWords;

    public cDocument(String docId, String docDate, String docTitle, String docText, String docOrgin, String docAuthor, String docLang) {
        this.docId = docId;
        this.docTitle = docTitle;
        this.docDate = docDate;
        this.docText = docText;
        this.docOrgin = docOrgin;
        this.docAuthor = docAuthor;
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

    public String getDocOrgin() {
        return docOrgin;
    }

    public String getDocAuthor() {
        return docAuthor;
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

    public void top5() {
    }

    @Override
    public String toString() {
        return "Doc{"+
                "docId='"+docId+'\''+
                ", docTitle='"+docTitle+'\''+
                ", docDate='"+docDate+'\''+
                ", docText='"+docText+'\''+
                ", docAuthor='"+docAuthor+'\''+
                '}';
    }
}
