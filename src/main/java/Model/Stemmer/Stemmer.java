package Model.Stemmer;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
/**
 * Implementing the Porter Stemming Algorithm 2
 *
 * The Stemmer class transforms a word into its root form.  The input
 * word can be provided a character at time (by calling add()), or at once
 * by calling one of the various stem(something) methods.
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
