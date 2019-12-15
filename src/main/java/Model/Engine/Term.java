package Model.Engine;

//--------------------------------------//
class Term {

    private String m_word; //the term
    private int m_termFreq; //number of documents the words appears in
    private int m_numOfAppearances; //number of times the word has appeared
    private int m_postingLine; //line number in the posting

    Term(String word, int termFreq, int numOfAppearances, int postingLine) {
        this.m_word = word;
        this.m_termFreq = termFreq;
        this.m_numOfAppearances = numOfAppearances;
        this.m_postingLine = postingLine;
    }

    /**
     * increase the number of times word has appeared in docs
     *
     * @param termFreqCur number of appearances to be added
     */
    void increaseTermFreq(int termFreqCur) {
        m_termFreq += termFreqCur;
    }

    int getTermFreq() {
        return m_termFreq;
    }

    int getNumOfAppearances() {
        return m_numOfAppearances;
    }

    void setPointer(int postingLine) {
        this.m_postingLine = postingLine;
    }

    void setNumOfAppearance(int numOfAppearance) {
        this.m_numOfAppearances = numOfAppearance;
    }

    @Override
    public String toString() {
        return m_word+"\t"+m_termFreq+"\t"+m_numOfAppearances+"\t"+m_postingLine+"\n";
    }
}
