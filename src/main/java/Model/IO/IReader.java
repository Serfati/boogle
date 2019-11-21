package Model.IO;

public interface IReader {
    /**
     * start the reading process on a given path
     */
    void read();

    /**
     * @return if there are no more files to be read
     */
    boolean getDone();

    /**
     * free the memory of the structures inside
     */
    void reset();
}
