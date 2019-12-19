package Model;

public interface IModel {

    void startIndexing(String pathOfDocs, String destinationPath, boolean stm);

    void reset(String path);

    void showDictionary();

    void loadDictionary(String path, boolean stem);
}
