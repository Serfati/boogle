package ranker;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class QueryDisplay {
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
