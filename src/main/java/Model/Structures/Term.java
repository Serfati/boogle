package Model.Structures;

import java.io.Serializable;

public class Term implements IData, Serializable, Comparable<Term> {

    private String name;
    private int df; // number of documents in which the term appeared in corpus.
    private int corpusTf; // total number of occurrence in the corpus
    private long ptrToPostingLine;

    //constructor
    public Term(String term_name) {
        this.name = term_name;
        this.df = 0;
        this.corpusTf = 0;
        this.ptrToPostingLine = -1;
    }

    public Term(String term_name, int df, int corpusTf, long ptr) {
        this.name = term_name;
        this.df = df;
        this.corpusTf = corpusTf;
        this.ptrToPostingLine = ptr;
    }

    /**
     * Getter for the term's df.
     *
     * @return the document frequency of the term.
     */
    public int getDf() {
        return this.df;
    }

    /**
     * Setter for the term's df.
     *
     * @param deltaDf -The number of documents to be added to the term's df.
     */
    public void setDf(int deltaDf) {

        this.df = this.df+deltaDf;
    }

    /**
     * @return the term's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets a new name for the term.
     *
     * @param new_name - the new name we wish to set for the term.
     */
    public void setName(String new_name) {
        this.name = new_name;
    }

    /**
     * Returns a pointer to the relevant posting file.
     * The pointer contains the line only. The file path can be concluded from the prefix of the term.
     *
     * @return the line in which the term appears in it's relevant posting file.
     */
    public long getPtr() {
        return this.ptrToPostingLine;
    }

    /**
     * sets the pointer of the term to given pointer.
     *
     * @param ptr - the new ptr to be set to the term.
     */
    public void setPtr(long ptr) {
        this.ptrToPostingLine = ptr;
    }

    /**
     * Returns the number of occurrence of the term in the corpus.
     *
     * @return the term frequency in the corpus.
     */
    public int getCorpusTf() {
        return corpusTf;
    }

    /**
     * Setter for the term's total frequency in the corpus.
     *
     * @param tf- the new tf to be setted to the given term.
     */
    public void setCorpusTf(int tf) {
        this.corpusTf = tf;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(Term o) {
        return Integer.compare(o.df, this.df);
    }
}
