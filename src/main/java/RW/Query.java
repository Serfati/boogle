package RW;

public class Query {
    String theQuery;
    String numer;
    String desc;
    String narr;

    public Query(String queryNumber, String queryDesc, String queryNarr) {
        numer=queryNumber;
        desc=queryDesc;
        narr= queryNarr;
    }

    public String getNumer() {
        return numer;
    }

    public void setNumer(String numer) {
        this.numer = numer;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getNarr() {
        return narr;
    }

    public void setNarr(String narr) {
        this.narr = narr;
    }

    public String getQuery() {
        return theQuery;
    }
}
