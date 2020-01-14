package ranker;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ResultDisplay {

    String queryID;
    LinkedList<QueryDisplay> docNames;

    StringProperty sp_queryID;
    StringProperty sp_docNames;

    public ResultDisplay(String queryID, LinkedList<String> docNames) {
        this.queryID = queryID;
        this.docNames = toQueryResultList(docNames);
        this.sp_queryID = new SimpleStringProperty(queryID);
        this.sp_docNames = new SimpleStringProperty(docNames.toString());
    }

    private LinkedList<QueryDisplay> toQueryResultList(LinkedList<String> docNames) {
        AtomicReference<LinkedList<QueryDisplay>> l = new AtomicReference<>(docNames.stream().map(QueryDisplay::new).collect(Collectors.toCollection(LinkedList::new)));
        return l.get();
    }


    public StringProperty sp_queryIDProperty() {
        return sp_queryID;
    }

    public LinkedList<QueryDisplay> getDocNames() {
        return docNames;
    }
}
