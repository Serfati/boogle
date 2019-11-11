package Model;


import javax.print.Doc;
import java.util.ArrayList;

/**
 * contract for being a parser
 */

interface IParser {
    /**
     * parse a document
     * @param doc given document to parse
     */
    void parse(Doc doc);

    /**
     * parse by free text
     * @param text text to be parsed
     * @return a list of tokens
     */
    ArrayList<String> parse(String text);

    /**
     * signal that the parser is done
     * @param done
     */
    void setDone(boolean done);

    /**
     *
     * @return true if the parser is done
     */
    boolean isDone();

    /**
     * initialize a tree of stop words from the given path
     * @param path path to the stop words file
     */
    void initializeStopWordsTreeAndStrategies(String path);

    /**
     * free all the structures in the class
     */
    void reset();
}
