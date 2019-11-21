import Model.IO.ReadFile;
import Model.Structures.cDocument;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * A class to demonstrate the functionality of the library.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class forTest {

    private static final String USAGE = "Usage: Examples [properties file]";
    private static final Set<String> HELP_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "--help", "-help", "/help", "--?", "-?", "?", "/?"
    )));
    private final static String MORPH_PHRASE = "running-away";
    private final Dictionary dictionary;
    private IndexWord ACCOMPLISH;
    private IndexWord DOG;
    private IndexWord CAT;
    private IndexWord FUNNY;
    private IndexWord DROLL;

    public forTest(Dictionary dictionary) throws JWNLException {
        this.dictionary = dictionary;
        ACCOMPLISH = dictionary.getIndexWord(POS.VERB, "accomplish");
        DOG = dictionary.getIndexWord(POS.NOUN, "dog");
        CAT = dictionary.lookupIndexWord(POS.NOUN, "cat");
        FUNNY = dictionary.lookupIndexWord(POS.ADJECTIVE, "funny");
        DROLL = dictionary.lookupIndexWord(POS.ADJECTIVE, "droll");
    }

    public static void main(String[] args) throws JWNLException {
        Dictionary dictionary = null;
        dictionary = Dictionary.getDefaultResourceInstance();

        String cPath = "/home/serfati/Desktop/IRproject/corpus";
        java.util.concurrent.BlockingQueue<cDocument> bq = new ArrayBlockingQueue<cDocument>(50000);
        ReadFile rf = new ReadFile(cPath, bq);
        rf.readFiles();
    }

}