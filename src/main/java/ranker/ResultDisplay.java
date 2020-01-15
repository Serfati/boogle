package ranker;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.LinkedList;

public class ResultDisplay extends RecursiveTreeObject<ResultDisplay> {

    String queryID;
    LinkedList<QueryDisplay> docNames;

    SimpleStringProperty sp_queryID;
    SimpleStringProperty sp_docNames;

    public ResultDisplay(String queryID, LinkedList<String> docNames) {
        this.queryID = queryID;
        this.docNames = toQueryResultList(docNames);
        this.sp_queryID = new SimpleStringProperty(queryID);
        this.sp_docNames = new SimpleStringProperty(docNames.toString());
    }

    private LinkedList<QueryDisplay> toQueryResultList(LinkedList<String> docNames) {
        LinkedList<QueryDisplay> l = new LinkedList<>();
        for(String s : docNames) {
            l.add(new QueryDisplay(s));
        }
        return l;
    }

    public String getSp_queryID() {
        return sp_queryID.get();
    }

    public StringProperty sp_queryIDProperty() {
        return sp_queryID;
    }

    public String getSp_docNames() {
        return sp_docNames.get();
    }

    public StringProperty sp_docNamesProperty() {
        return sp_docNames;
    }

    public LinkedList<QueryDisplay> getDocNames() {
        return docNames;
    }

    //----//
    public class QueryDisplay extends RecursiveTreeObject<QueryDisplay> {
        private StringProperty sp_docName;

        QueryDisplay(String docName) {
            sp_docName = new SimpleStringProperty(docName);
        }

        public String getSp_docName() {
            return sp_docName.get();
        }

        public StringProperty sp_docNameProperty() {
            return sp_docName;
        }
    }
}
