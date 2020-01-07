package ranker;

import model.Model;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A class used to handle semantics.
 * given a word check for her vector in a glove file and calculate similarity between it and other words
 * bring back the 3 most relevant words
 */
public class SemanticHandler {

    /* wordVectors collection */
    public static WordVectors wordVectors;
    //corpus path is set on engine run or load
    public static String corpusPath;
    public static String gloveFile;
    public static boolean includeSemantics = false;
    private Model model;


    public static void main(String[] args) {
        new SemanticHandler().sample();
    }

    /* this will read  from the glove file and load the word vectors into wordsVectors*/
    public static void readGloveFile() {
        wordVectors = WordVectorSerializer.readWord2VecModel(new File("src/main/resources/glove.6B.50d.txt"));
    }

    private void sample() {
        readGloveFile();
        //prints the 10 closest words to "israel"
        System.out.println(wordVectors.wordsNearest("israel", 10));
        //prints the 10 closest words to "kitten" - "cat" + "dog"
        System.out.println(wordVectors.wordsNearest(Arrays.asList("kitten", "dog"), Collections.singletonList("cat"), 10));
        //prints the closest word to "berlin" - "germany" + "france"
        System.out.println(wordVectors.wordsNearest(Arrays.asList("berlin", "france"), Collections.singletonList("germany"), 1));
    }


    //-------------------------------------------------------------------//

    /**
     * this will return all the related words for a given query (3 for each word)
     *
     * @param originalQueryWords list of the query words
     * @return realted words for a given query
     */
    public static ArrayList<String> getRelatedWords(List<String> originalQueryWords) {
        ArrayList<String> ans = new ArrayList<>();
        for(String word : originalQueryWords) {
            ArrayList<String> wordRelatedWords = getThreeBestMatches(word);
            ans.addAll(wordRelatedWords);
        }
        return ans;
    }

    /**
     * return words this maximum simalirty over all the corpus
     *
     * @param unProcessedWord 1 query word
     * @return 3 related words by GLOVE model
     */
    public static ArrayList<String> getThreeBestMatches(String unProcessedWord) {
        return null;
    }

    /* calcualting  similarity*/
    private static List<String> getSimilarity(String unprocessedWord) {
        List<String> list = new ArrayList<>();
        //list.addAll(model.vocab.dict.keySet());
        list.sort((String arg0, String arg1) -> {
            if (wordVectors.similarity(arg0, unprocessedWord) < wordVectors.similarity(arg1, unprocessedWord)) return 1;
            return -1;
        });
        return list.subList(1, 6);
    }

    /**
     * use it to clear the memory
     */
    public static void clearWordsVecs() {
        wordVectors = null;
    }

    private double avgWeight(List<String> closest) {
        Mean mean = new Mean();
        closest.stream().mapToDouble(this::weight).forEach(mean::increment);
        return mean.getResult();
    }

    private double weight(String word) {
        //return Math.log10(model.vocab.getCount(word, 0)) * model.power.get(word);
        return 0;
    }
}
