package Parser;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
/**
 * This class represent the stemmer.
 */

public class Stemmer {

    SnowballStemmer stemmer;

    public Stemmer() {
        this.stemmer = new englishStemmer();
    }

    public SnowballStemmer getStemmer() {
        return stemmer;
    }
}
