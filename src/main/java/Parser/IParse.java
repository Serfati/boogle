package Parser;


/**
 * contract for being a parser
 */

interface IParse {

    /**
     * parse by free text
     * @param text text to be parsed
     * @return a list of tokens
     */
    void parse(String text);

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
     * free all the structures in the class
     */
    void reset();
}
