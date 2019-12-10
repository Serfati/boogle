package Model;

import java.io.File;

public interface IModel {

    void startIndexing(String pathOfDocs, String destinationPath, boolean stm);

    void closeModel();

    void saveDic(File file);

    boolean isFinish();

    void showDictionary();

    void loadDictionary(String path, boolean stem);
}
