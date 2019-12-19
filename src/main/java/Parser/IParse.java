package Parser;

/**
 * contract for being a parser
 */

interface IParse {

    /**
     * parse by free text
     *
     * @return a list of tokens
     */
    MiniDictionary parse();
}