package Model.Parser;

/**
 * contract for being a parser
 */

interface IParse {

    /**
     * parse by free text
     * @return a list of tokens
     */
    MiniDictionary parse();

    /**
     *
     * @return true if the parser is done
     */
    //boolean isDone();

    /**
     * free all the structures in the class
     */
    //void reset();
}
