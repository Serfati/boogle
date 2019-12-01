import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.Properties;
import java.util.stream.Collectors;

public class NERPipelineDemo {

    public static void main(String[] args) {
        // set up pipeline properties
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        // example customizations (these are commented out but you can uncomment them to see the results

        // disable fine grained ner
        // props.setProperty("ner.applyFineGrained", "false");

        // customize fine grained ner
        // props.setProperty("ner.fine.regexner.mapping", "example.rules");
        // props.setProperty("ner.fine.regexner.ignorecase", "true");

        // add additional rules, customize TokensRegexNER annotator
        // props.setProperty("ner.additional.regexner.mapping", "example.rules");
        // props.setProperty("ner.additional.regexner.ignorecase", "true");

        // add 2 additional rules files ; set the first one to be case-insensitive
        // props.setProperty("ner.additional.regexner.mapping", "ignorecase=true,example_one.rules;example_two.rules");

        // set document date to be a specific date (other options are explained in the document date section)
        // props.setProperty("ner.docdate.useFixedDate", "2019-01-01");

        // only run rules based NER
        //props.setProperty("ner.rulesOnly", "true");

        // only run statistical NER
        //props.setProperty("ner.statisticalOnly", "true");

        // set up pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // make an example document
        CoreDocument doc = new CoreDocument("At the beginning of the 20th century, the world’s tiger population was estimated at 100,000, even though they had been hunted for at least a thousand years. Tigers were prized as trophies and as a source of skins for expensive coats. They were also killed on the grounds that they posed a danger to humans. As the century drew to a close, only 5,000 to 7,500 were left in the wild, and captive tigers may now outnumber wild one");
        // annotate the document
        pipeline.annotate(doc);
        // view results
        System.out.println("---");
        System.out.println("entities found");
        for(CoreEntityMention em : doc.entityMentions())
            System.out.println("\tdetected entity: \t"+em.text()+"\t"+em.entityType());
        System.out.println("---");
        System.out.println("tokens and ner tags");
        String tokensAndNERTags = doc.tokens().stream().map(token -> "("+token.word()+","+token.ner()+")").collect(
                Collectors.joining(" "));
        System.out.println(tokensAndNERTags);
    }

}
