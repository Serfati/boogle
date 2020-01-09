package rw;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

public class GloVe {
    public static WordVectors wordVectors;


    public static void main(String[] args) {
        new GloVe().sample();
    }

    /* this will read  from the glove file and load the word vectors into wordsVectors*/
    public static void readGloveFile() {
        wordVectors = WordVectorSerializer.readWord2VecModel(new File("src/main/resources/glove.6B.50d.txt"));
    }

    private void sample() {
        readGloveFile();
        //prints the 10 closest words to "israel"
        System.out.println(wordVectors.wordsNearest("israel", 3));
        //prints the 10 closest words to "kitten" - "cat" + "dog"
        System.out.println(wordVectors.wordsNearest(Arrays.asList("kitten", "dog"), Collections.singletonList("cat"), 10));
        //prints the closest word to "berlin" - "germany" + "france"
        System.out.println(wordVectors.wordsNearest(Arrays.asList("berlin", "france"), Collections.singletonList("germany"), 1));
    }
}
