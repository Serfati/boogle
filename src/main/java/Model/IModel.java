package Model;

import Engine.InvertedIndex;

import java.io.IOException;

public interface IModel {

    /**
     * Starts the index process - whole program
     *
     * @param pathOfDocs - path of the corpus and stop words
     * @param destinationPath - path where the posting and other data should be written
     * @param useStemming - if stemming should be done
     */
    void startIndexing(String pathOfDocs, String destinationPath, boolean useStemming);

    /**
     * TManages the index process by separating it to a few bunches
     * @param invertedIndex - the inverted index
     * @param corpusPath - the path of the corpus
     * @param destinationPath - the path where the postings will be written
     * @param stem - if stemming should be done
     * @return returns data about the current run [num of documents, number of unique terms]
     * @throws Exception .
     */
    int[] mainLogicUnit(InvertedIndex invertedIndex, String corpusPath, String destinationPath, boolean stem) throws Exception;
    /**
     * Merges the temp postings to postings according to the letter of words
     * @param invertedIndex - the inverted index
     * @param tempPostingPath - the path of the temp postings
     * @param stem - if should be stemmed
     */
    void mergePosting(InvertedIndex invertedIndex, String tempPostingPath, boolean stem) throws IOException;

    /**
     * Loads the dictionary that is present in the path given according to the stem sign
     * @param path - the path to load from
     * @param useStemming - dictionary with stem or without
     */
    void loadDictionary(String path, boolean useStemming);

    /**
     * Deletes all the contents of the path given
     *
     * @param path the path where all the details should be deleted
     */
    void reset(String path);

    /**
     * Loads the inverted index and notify the view model
     */
    void showDictionary();


}
