package ranker.algorithms;

import model.Model;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Set;

/* check if terms are at the title of the given document */
public class TitleAlgorithm extends ARankingAlgorithm {

    public TitleAlgorithm(double weight) {
        super(weight);
    }

    public double rank(String docName, Set<String> wordsInQuery) {
        //go throw all words of title and check if words appear in the query
        double rank = 0;
        String title = Model.documentDictionary.get(docName).getDocTI().toLowerCase();
        if (!title.equals("")) {
            String[] split = StringUtils.split(title, " ~;!?=#&^*+\\|:\"(){}[]<>\n\r\t");
            rank = Arrays.stream(split).filter(wordsInQuery::contains).mapToDouble(wordTitle -> weight).sum();
        }
        return rank * weight;
    }
}
