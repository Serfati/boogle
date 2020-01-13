package ranker;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import rw.DatamuseAPI;
import rw.GloVe;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A class used to handle semantics.
 * given a word check for her vector in a glove file and calculate similarity between it and other words
 * bring back the  most relevant words
 */

public class SemanticHandler {
    public WordVectors wordVectors;
    public boolean useOffline;

    public SemanticHandler(boolean useOffline) {
        wordVectors = useOffline ? WordVectorSerializer.readWord2VecModel(new File("src/main/resources/glove.6B.50d.txt")) : null;
        this.useOffline = useOffline;
    }

    //-------------------------------------------------------------------//

    /**
     * this will return all the related words for a given query (2 for each word)
     *
     * @param originalQueryWords list of the query words
     * @return realted words for a given query
     */
    public StringBuilder getTwoBestMatches(List<String> originalQueryWords) throws IOException {
        StringBuilder allSynonyms = new StringBuilder();
        for(String entry : originalQueryWords)
            allSynonyms.append(useOffline ? new GloVe(wordVectors).synonyms(entry) : DatamuseAPI.synonyms(entry));
        return allSynonyms;
    }
}

