package Model;

public class Term {

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
}