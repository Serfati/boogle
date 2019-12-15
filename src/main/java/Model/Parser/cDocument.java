package Model.Parser;

/*
this class represents a single document in the corpus containing all the data about the document
 */

public class cDocument {
    private String m_fileName;
    private String m_docNum;
    private String m_docDate;
    private String m_docTitle;
    private String m_docText;

    public cDocument(String fileName, String docNum, String docDate, String docTitle, String docText) {
        this.m_fileName = fileName;
        this.m_docNum = docNum;
        this.m_docDate = docDate;
        this.m_docTitle = docTitle;
        this.m_docText = docText;
    }

    public String getFileName() {
        return m_fileName;
    }

    public String getDocText() {
        return m_docText;
    }

}
