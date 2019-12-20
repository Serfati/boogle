package Parser;
/*
    a single document in corpus
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

    public String getDocNum() {
        return m_docNum;
    }

    public String getDocTitle() {
        return m_docTitle;
    }

    public String getDocText() {
        return m_docText;
    }
}
