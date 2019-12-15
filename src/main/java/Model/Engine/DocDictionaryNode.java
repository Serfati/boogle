package Model.Engine;

import javafx.util.Pair;

/**
 * represents a document
 */
public class DocDictionaryNode {
    private String m_docName;
    private int m_maxFreq;
    private String m_maxFreqWord;
    private int m_numOfUniWords;
    private int m_docLength;
    private String m_title;
    private Pair<String, Integer>[] m_primaryWords;

    public DocDictionaryNode(String docName, int maxFreq, int numOfUniWords, String maxFreqWord, int docLength, String title, Pair<String, Integer>[] primaryWords) {
        this.m_docName = docName;
        this.m_maxFreq = maxFreq;
        this.m_numOfUniWords = numOfUniWords;
        this.m_maxFreqWord = maxFreqWord;
        this.m_docLength = docLength;
        this.m_title = title;
        this.m_primaryWords = primaryWords;

    }

    public String getDocName() {
        return m_docName;
    }

    public String getTitle() {
        return m_title;
    }

    public int getDocLength() {
        return m_docLength;
    }

    @Override
    public int hashCode() {
        return m_docName.hashCode();
    }

    @Override
    public String toString() {
        String pw = "";
        if (m_primaryWords != null) {
            for(int i = 0; i < m_primaryWords.length-1; i++) {
                if (m_primaryWords[i] != null)
                    pw += m_primaryWords[i].getKey()+"~"+m_primaryWords[i].getValue()+"#";
            }
            if (m_primaryWords[m_primaryWords.length-1] != null)
                pw += m_primaryWords[m_primaryWords.length-1].getKey()+"~"+m_primaryWords[m_primaryWords.length-1].getValue();
        }
        return m_docName+"\t"+m_maxFreq+"\t"+m_numOfUniWords+"\t"+m_maxFreqWord+"\t"+m_title+"\t"+m_docLength+"\t"+pw+"\n";

    }

    /**
     * returnd 5 strongest entities  in the document
     *
     * @return string of 5 entities
     */
    public String get5words() {
        StringBuilder s = new StringBuilder();
        for(Pair<String, Integer> m_primaryWord : m_primaryWords) {
            if (m_primaryWord == null)
                break;
            s.append(m_primaryWord.getValue()).append("\t").append(m_primaryWord.getKey()).append("\n");
        }
        return s.toString();
    }

    public Pair<String, Integer>[] getPrimaryWords() {
        return m_primaryWords;
    }

    public int getMaxFreq() {
        return m_maxFreq;
    }

    public int getNumOfUniWords() {
        return m_numOfUniWords;
    }
}


