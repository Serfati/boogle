package Model;

import java.io.File;

public interface IModel {
    void closeModel();

    void loadDic(File file);

    void saveDic(File file);

    boolean isFinish();

}
