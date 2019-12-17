package Engine;

public class DocumentIndex {
    private String m_docName;
    private int m_maxFreq;
    private String m_maxFreqWord;
    private int m_numOfUniWords;
    private int m_docLength;
    private String m_title;

    public DocumentIndex(String docName, int maxFreq, int numOfUniWords, String maxFreqWord, int docLength, String title) {
        this.m_docName = docName;
        this.m_maxFreq = maxFreq;
        this.m_numOfUniWords = numOfUniWords;
        this.m_maxFreqWord = maxFreqWord;
        this.m_docLength = docLength;
        this.m_title = title;

    }

    @Override
    public int hashCode() {
        return m_docName.hashCode();
    }

    @Override
    public String toString() {
        return m_docName+"\t"+m_maxFreq+"\t"+m_numOfUniWords+'\t'+m_maxFreqWord+"\t"+m_title+"\t"+m_docLength+"\t"+"\n";
    }
}


