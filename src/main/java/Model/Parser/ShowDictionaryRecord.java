package Model.Parser;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ShowDictionaryRecord {
    private String term;
    private int count;
    private StringProperty termProperty;
    private IntegerProperty countProperty;

    public ShowDictionaryRecord(String term, int count) {
        this.term = term;
        this.count = count;
        this.termProperty = new SimpleStringProperty(term);
        this.countProperty = new SimpleIntegerProperty(count);
    }

    public StringProperty getTermProperty() {
        return termProperty;
    }

    public IntegerProperty getCountProperty() {
        return countProperty;
    }
}