package rw;

/**
 * encapsulates a query
 * <p>
 * has getters and setters
 */

public class Query {
    private String queryNum;
    private String queryText;
    private String queryDesc;

    public Query(String queryNum, String queryText, String queryDesc) {
        this.queryNum = queryNum;
        this.queryText = queryText;
        this.queryDesc = queryDesc;
    }

    public String getQueryNum() {
        return queryNum;
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    public String getQueryDesc() {
        return queryDesc;
    }
}
