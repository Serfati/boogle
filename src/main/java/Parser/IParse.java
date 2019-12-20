package Parser;

/**
 * contract for being a parser
 */

interface IParse {

    /**
     * parse by free text
     * @return MiniDictionary with all term of current document.
     */
    MiniDictionary parse();
}
